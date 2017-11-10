package net.pykaso.zonkystats.zonkystats.repository;

import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import net.pykaso.zonkystats.zonkystats.AppExecutors;
import net.pykaso.zonkystats.zonkystats.api.ApiResponse;
import net.pykaso.zonkystats.zonkystats.api.LoansResponse;
import net.pykaso.zonkystats.zonkystats.api.ZonkyService;
import net.pykaso.zonkystats.zonkystats.db.Loans;
import net.pykaso.zonkystats.zonkystats.db.LoansDao;
import net.pykaso.zonkystats.zonkystats.misc.Prefs;

import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;


/**
 * Repository act as 'single source of truth'
 * Fetch the data from network if needed, in other case returns cached data from local database.
 */
@Singleton
public class ZonkyRepository {

    private ZonkyService service;
    private LoansDao loansDao;
    private AppExecutors executors;
    private Prefs prefsUtils;

    @Inject
    public ZonkyRepository(ZonkyService service, AppExecutors executors, LoansDao loansDao, Prefs prefs) {
        this.service = service;
        this.loansDao = loansDao;
        this.executors = executors;
        this.prefsUtils = prefs;
    }

    public LiveData<Resource<List<Loans>>> getLoansDB(OffsetDateTime from, OffsetDateTime to) {
        return new NetworkBoundResource<List<Loans>, List<LoansResponse>>(executors) {
            @Override
            protected void saveCallResult(@NonNull List<LoansResponse> items) {
                List<Loans> agregated = convertApiResponse(items);
                loansDao.insert(agregated);
            }

            @Override
            protected boolean shouldFetch(@Nullable List<Loans> data) {
                if (data == null || data.size() == 0)
                    return true;

                OffsetDateTime lastFetchDate = prefsUtils.getLastFetchDate();
                Loans fg = loansDao.getForegoing(from.format(DateTimeFormatter.ISO_LOCAL_DATE));
                Loans fw = loansDao.getFollowing(to.withDayOfMonth(1).format(DateTimeFormatter.ISO_LOCAL_DATE));
                if (fg == null || fw == null) {
                    return true;
                } else if (to.getYear() == OffsetDateTime.now().getYear() &&
                        lastFetchDate != null && to.isAfter(lastFetchDate)) {
                    return true;
                }
                return false;
            }

            @NonNull
            @Override
            protected LiveData<List<Loans>> loadFromDb() {
                return loansDao.getForPeriod(from.format(DateTimeFormatter.ISO_LOCAL_DATE), to.format(DateTimeFormatter.ISO_LOCAL_DATE));
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<List<LoansResponse>>> createCall() {
                // Na API se ptame intervalem <from, to), pricteme den aby se do filtru dostal
                // posledni zaznam predchoziho dne
                prefsUtils.setLastFetchDate(to);
                return service.getLoans(from.toString(), to.plusDays(1).toString(), "covered,datePublished");
            }

            @Override
            protected void onFetchFailed() {
                Timber.e("Fetch failed...");
            }
        }.asLiveData();
    }


    @VisibleForTesting
    private Loans findInList(List<Loans> haystack, String needle) {
        for (Loans h : haystack) {
            if (h.period.equals(needle))
                return h;
        }
        return null;
    }

    @VisibleForTesting
    private List<Loans> convertApiResponse(List<LoansResponse> data) {
        List<Loans> result = new ArrayList<>();
        String lastKey = "";
        Loans active = null;
        for (LoansResponse item : data) {
            String key = item.datePublished.substring(0, 7) + "-01";
            // parsovani OffsetDateTime neni v tomto pripade potreba, zruseno v POJO,
            // jen to zpomaluje a vysledek se nikde nevyuzije
            // String key = String.format("%1$4d.%2$02d", item.getDatePublished().getYear(), item.getDatePublished().getMonthValue());

            if (!lastKey.equals(key)) {
                Loans l = findInList(result, key);
                if (l != null) {
                    active = l;
                } else {
                    active = new Loans(key, 0, 0);
                    result.add(active);
                }
                lastKey = key;
            }
            if (active != null) {
                active.published += 1;

                if (item.covered) {
                    active.covered += 1;
                }
            }
        }
        return result;
    }
}

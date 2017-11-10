package net.pykaso.zonkystats.zonkystats.repository;

import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;

import net.pykaso.zonkystats.zonkystats.api.ApiResponse;
import net.pykaso.zonkystats.zonkystats.api.LoansResponse;
import net.pykaso.zonkystats.zonkystats.api.ZonkyService;
import net.pykaso.zonkystats.zonkystats.db.AppDB;
import net.pykaso.zonkystats.zonkystats.db.Loans;
import net.pykaso.zonkystats.zonkystats.db.LoansDao;
import net.pykaso.zonkystats.zonkystats.misc.Prefs;
import net.pykaso.zonkystats.zonkystats.utils.InstantAppExecutors;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;
import retrofit2.Response;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;


@SuppressWarnings("unchecked")
@RunWith(JUnit4.class)
public class ZonkyRepositoryTest {
    private ZonkyRepository repository;
    private LoansDao dao;
    private ZonkyService service;
    protected Prefs prefs;
    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();


    @Before
    public void init() {
        prefs = mock(Prefs.class);
        dao = mock(LoansDao.class);
        service = mock(ZonkyService.class);
        AppDB db = mock(AppDB.class);
        when(db.loansDao()).thenReturn(dao);
        repository = new ZonkyRepository(service, new InstantAppExecutors(), dao, prefs);
    }

    @Test
    public void getLoansDB() throws Exception {
        OffsetDateTime from = OffsetDateTime.of(2017,1,1,0,0,0,0, ZoneOffset.ofHours(1));
        OffsetDateTime to = from.plusMonths(1);
        String strFrom = from.format(DateTimeFormatter.ISO_LOCAL_DATE);
        String strTo = to.format(DateTimeFormatter.ISO_LOCAL_DATE);

        MutableLiveData<List<Loans>> dbData = new MutableLiveData<>();
        when(dao.getForPeriod(strFrom, strTo)).thenReturn(dbData);

        List<LoansResponse> repo = new ArrayList<>();
        repo.add(new LoansResponse("2017-01-01", true, true));
        repo.add(new LoansResponse("2017-02-01", true, false));

        MutableLiveData<ApiResponse<List<LoansResponse>>> call = new MutableLiveData<>();
        Response<List<LoansResponse>> resp = Response.success(repo, Headers.of("x-total","2"));
        ApiResponse ar = new ApiResponse<>(resp);
        call.setValue(ar);
        when(service.getLoans(from.toString(), to.plusDays(1).toString(), "covered,datePublished")).thenReturn(call);

        LiveData<Resource<List<Loans>>> data = repository.getLoansDB(from, to);
        verify(dao).getForPeriod(strFrom, strTo);
        verifyNoMoreInteractions(service);

        Observer observer = mock(Observer.class);
        data.observeForever(observer);
        verifyNoMoreInteractions(service);
        verify(observer).onChanged(Resource.loading(null));
        MutableLiveData<List<Loans>> updatedDbData = new MutableLiveData<>();
        when(dao.getForPeriod(strFrom, strTo)).thenReturn(updatedDbData);

        List<Loans> loans = new ArrayList<>();
        loans.add(new Loans("2017-01-01", 1, 1));
        loans.add(new Loans("2017-02-01", 1, 0));

        dbData.postValue(null);
        verify(service).getLoans(from.toString(), to.plusDays(1).toString(), "covered,datePublished");
        verify(dao).insert(loans);
//
//        updatedDbData.postValue(repo);
//        verify(observer).onChanged(Resource.success(repo));

        repository.getLoansDB(from, to);
    }

}
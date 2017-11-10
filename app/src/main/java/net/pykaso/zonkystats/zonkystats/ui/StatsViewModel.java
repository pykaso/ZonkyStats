package net.pykaso.zonkystats.zonkystats.ui;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;

import net.pykaso.zonkystats.zonkystats.db.Loans;
import net.pykaso.zonkystats.zonkystats.repository.Resource;
import net.pykaso.zonkystats.zonkystats.repository.ZonkyRepository;

import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.ZoneOffset;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.PointValue;

public class StatsViewModel extends ViewModel {

    private ZonkyRepository repository;

    private final LiveData<Resource<List<Loans>>> result;
    private final MutableLiveData<OffsetDateTime[]> range = new MutableLiveData<>();

    private final MutableLiveData<Interval> actualInterval = new MutableLiveData<>();

    public enum Interval {
        LAST_3_MONTHS,
        LAST_12_MONTHS,
        YEAR_LAST,
        YEAR_ACTUAL
    }

    @Inject
    public StatsViewModel(ZonkyRepository repository) {
        this.repository = repository;
        this.actualInterval.setValue(null);

        result = Transformations.switchMap(range, search -> {
            if (search == null || search.length == 0) {
                return null;
            } else {
                return this.repository.getLoansDB(search[0], search[1]);
            }
        });
    }

    public static List<PointValue> toPoints(List<Loans> data, String attr) {
        List<PointValue> values = new ArrayList<>();
        int pos = 0;
        for (Loans entry : data) {
            PointValue v = new PointValue(pos, entry.get(attr));

            values.add(v);
            pos++;
        }
        return values;
    }



    public static List<AxisValue> toLabels(List<Loans> data) {
        List<AxisValue> axisValues = new ArrayList<AxisValue>();
        int pos = 0;
        for (Loans entry : data) {
            axisValues.add(new AxisValue(pos)
                    .setLabel(entry.period.substring(2, 7).replace('-', '/')));
            pos++;
        }
        return axisValues;
    }


    public LiveData<Resource<List<Loans>>> getLoans() {
        return this.result;
    }


    public void setInterval(Interval interval) {
        if (this.actualInterval.getValue() != interval) {
            this.actualInterval.setValue(interval);
            range.setValue(getRangeFromInterval(interval));
        }
    }

    public LiveData<Interval> getActualInterval() {
        return this.actualInterval;
    }

    private OffsetDateTime getNormalizedDate(int year, int month) {
        return OffsetDateTime.of(year, month, 1, 0, 0, 0, 0, ZoneOffset.ofHours(1));
    }

    private OffsetDateTime[] getRangeFromInterval(Interval interval) {
        OffsetDateTime now = OffsetDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);

        switch (interval) {
            case YEAR_ACTUAL:
                return new OffsetDateTime[]{
                        getNormalizedDate(now.getYear(), 1),
                        now};
            case YEAR_LAST:
                return new OffsetDateTime[]{
                        getNormalizedDate(now.minusYears(1).getYear(), 1),
                        getNormalizedDate(now.minusYears(1).getYear(), 12).withDayOfMonth(31)};
            case LAST_3_MONTHS:
                return new OffsetDateTime[]{
                        getNormalizedDate(now.getYear(), now.getMonthValue()).minusMonths(2),
                        now
                };
            case LAST_12_MONTHS:
                return new OffsetDateTime[]{
                        getNormalizedDate(now.getYear(), now.getMonthValue()).minusMonths(12),
                        now
                };
        }
        return null;
    }
}

package net.pykaso.zonkystats.zonkystats.ui;

import android.app.Application;
import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.arch.lifecycle.Observer;
import android.content.res.AssetManager;

import com.jakewharton.threetenabp.AndroidThreeTen;

import net.pykaso.zonkystats.zonkystats.db.Loans;
import net.pykaso.zonkystats.zonkystats.repository.ZonkyRepository;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.PointValue;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
@RunWith(JUnit4.class)
public class StatsViewModelTest {

    StatsViewModel viewModel;

    private ZonkyRepository repository;

    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    @Before
    public void init() {
        Application app = mock(Application.class);
        AssetManager am = mock(AssetManager.class);
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("TZDB.dat");
        when(app.getAssets()).thenReturn(am);
        try {
            when(am.open("org/threeten/bp/TZDB.dat")).thenReturn(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        AndroidThreeTen.init(app);
        repository = mock(ZonkyRepository.class);
        viewModel = new StatsViewModel(repository);
    }

    @Test
    public void toPoints() throws Exception {
        List<Loans> data = new ArrayList<>();
        data.add(new Loans("2017-01-01", 1, 1));
        data.add(new Loans("2017-02-01", 2, 2));
        data.add(new Loans("2017-03-01", 3, 2));

        List<PointValue> result = StatsViewModel.toPoints(data, "published");
        assertThat(result.size(), is(3));
        assertThat(result.get(0).getX(), is(0.0F));
        assertThat(result.get(0).getY(), is(1.0F));
        assertThat(result.get(2).getX(), is(2.0F));
        assertThat(result.get(2).getY(), is(3.0F));

        List<PointValue> result2 = StatsViewModel.toPoints(data, "covered");
        assertThat(result2.size(), is(3));
        assertThat(result2.get(0).getX(), is(0.0F));
        assertThat(result2.get(0).getY(), is(1.0F));
        assertThat(result2.get(2).getX(), is(2.0F));
        assertThat(result2.get(2).getY(), is(2.0F));
    }

    @Test
    public void toLabels() throws Exception {
        List<Loans> data = new ArrayList<>();
        data.add(new Loans("2017-01-01", 1, 1));
        data.add(new Loans("2017-02-01", 2, 2));
        data.add(new Loans("2017-03-01", 3, 2));

        List<AxisValue> result = StatsViewModel.toLabels(data);
        assertThat(result.size(), is(3));
        assertThat(result.get(0).getLabelAsChars(), is("17/01"));
        assertThat(result.get(2).getLabelAsChars(), is("17/03"));
    }

    @Test
    public void getLoans() throws Exception {
        Observer observer = mock(Observer.class);
        viewModel.getLoans().observeForever(observer);
        verifyNoMoreInteractions(repository);
        verify(observer).onChanged(null);
    }

    @Test
    public void getActualInterval() throws Exception {
        Observer observer = mock(Observer.class);
        viewModel.getActualInterval().observeForever(observer);
        viewModel.setInterval(StatsViewModel.Interval.LAST_3_MONTHS);
        verify(observer).onChanged(StatsViewModel.Interval.LAST_3_MONTHS);
        verify(repository).getLoansDB(null, null);
    }

}
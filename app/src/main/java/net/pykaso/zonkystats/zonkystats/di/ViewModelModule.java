package net.pykaso.zonkystats.zonkystats.di;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import net.pykaso.zonkystats.zonkystats.ZonkyViewModelFactory;
import net.pykaso.zonkystats.zonkystats.ui.StatsViewModel;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
abstract class ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(StatsViewModel.class)
    abstract ViewModel bindStatsViewModel(StatsViewModel repoViewModel);

    @Binds
    abstract ViewModelProvider.Factory bindViewModelFactory(ZonkyViewModelFactory factory);
}

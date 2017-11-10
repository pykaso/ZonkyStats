package net.pykaso.zonkystats.zonkystats.di;

import net.pykaso.zonkystats.zonkystats.ui.StatsFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class FragmentBuildersModule {
    @ContributesAndroidInjector
    abstract StatsFragment contributeMarketplaceStatsFragment();
}

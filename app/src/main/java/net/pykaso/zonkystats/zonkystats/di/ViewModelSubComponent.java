package net.pykaso.zonkystats.zonkystats.di;

import net.pykaso.zonkystats.zonkystats.ui.StatsViewModel;

import dagger.Subcomponent;

@Subcomponent
public interface ViewModelSubComponent {
    @Subcomponent.Builder
    interface Builder {
        ViewModelSubComponent build();
    }

    StatsViewModel statsViewModel();
}
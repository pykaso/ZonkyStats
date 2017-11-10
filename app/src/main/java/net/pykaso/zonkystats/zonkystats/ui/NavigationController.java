package net.pykaso.zonkystats.zonkystats.ui;

import android.support.v4.app.FragmentManager;

import net.pykaso.zonkystats.zonkystats.MainActivity;
import net.pykaso.zonkystats.zonkystats.R;

import javax.inject.Inject;

public class NavigationController {
    private final int containerId;
    private final FragmentManager fragmentManager;

    @Inject
    public NavigationController(MainActivity mainActivity) {
        this.containerId = R.id.container;
        this.fragmentManager = mainActivity.getSupportFragmentManager();
    }

    public void navigateToStats() {
        StatsFragment statsFragment = new StatsFragment();
        fragmentManager.beginTransaction()
                .replace(containerId, statsFragment)
                .commitAllowingStateLoss();
    }
}

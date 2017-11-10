package net.pykaso.zonkystats.zonkystats;


import android.app.Activity;
import android.app.Application;

import com.jakewharton.threetenabp.AndroidThreeTen;

import net.pykaso.zonkystats.zonkystats.di.AppInjector;

import javax.inject.Inject;

import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;

import timber.log.Timber;

public class ZonkyApp extends Application implements HasActivityInjector {

        @Inject
        DispatchingAndroidInjector<Activity> dispatchingAndroidInjector;

        @Override
        public void onCreate() {
            super.onCreate();
            if (BuildConfig.DEBUG) {
                Timber.plant(new Timber.DebugTree());
            }
            AndroidThreeTen.init(this);
            AppInjector.init(this);
        }

        @Override
        public DispatchingAndroidInjector<Activity> activityInjector() {
            return dispatchingAndroidInjector;
        }
    }

package net.pykaso.zonkystats.zonkystats.misc;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.threeten.bp.OffsetDateTime;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class Prefs {

    private Application application;
    private static final String PREF_DATE = "last_fetch_date";

    @Inject
    public Prefs(Application app) {
        this.application = app;
    }

    public void setLastFetchDate(OffsetDateTime date) {
        date = date.withMinute(0).withSecond(0).withNano(0);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(application);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PREF_DATE, date.toString());
        editor.apply();
    }

    public OffsetDateTime getLastFetchDate() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(application);
        String strDate = preferences.getString(PREF_DATE, null);
        if (strDate == null)
            return null;
        return OffsetDateTime.parse(strDate);
    }
}

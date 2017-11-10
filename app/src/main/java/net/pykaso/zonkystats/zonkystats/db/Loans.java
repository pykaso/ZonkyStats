package net.pykaso.zonkystats.zonkystats.db;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/**
 * Entity used for storing aggregated data do local database
 */
@Entity
public class Loans {
    @PrimaryKey
    @NonNull
    public String period;
    public int published;
    public int covered;

    public Loans(String period, int published, int covered){
        this.period = period;
        this.published = published;
        this.covered = covered;

    }

    public int get(String prop) {
        if (prop.equals("published"))
            return published;
        else if (prop.equals("covered"))
            return covered;
        return 0;
    }
}

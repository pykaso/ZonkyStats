package net.pykaso.zonkystats.zonkystats.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface LoansDao {

    @Query("SELECT * FROM loans WHERE date(period) BETWEEN date(:date_from) AND date(:date_to) ORDER BY period")
    LiveData<List<Loans>> getForPeriod(String date_from, String date_to);

    @Query("SELECT * FROM loans WHERE date(period) <= date(:date_param) LIMIT 1")
    Loans getForegoing(String date_param);

    @Query("SELECT * FROM loans WHERE date(period) >= date(:date_param) LIMIT 1")
    Loans getFollowing(String date_param);


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Loans loans);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] insert(List<Loans> loans);
}

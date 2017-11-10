package net.pykaso.zonkystats.zonkystats.db;


import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = {Loans.class}, version=1)
public abstract class AppDB extends RoomDatabase {
    public abstract LoansDao loansDao();
}

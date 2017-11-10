package net.pykaso.zonkystats.zonkystats.di;

import android.app.Application;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.ncornette.cache.OkCacheControl;

import net.pykaso.zonkystats.zonkystats.api.ZonkyService;
import net.pykaso.zonkystats.zonkystats.db.AppDB;
import net.pykaso.zonkystats.zonkystats.db.LoansDao;
import net.pykaso.zonkystats.zonkystats.misc.LiveDataRetrofitAdapterFactory;

import java.io.File;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;
import timber.log.Timber;

import static java.util.concurrent.TimeUnit.MINUTES;

@Module(includes = ViewModelModule.class)
class AppModule {

    @Singleton
    @Provides
    ZonkyService provideZonkyService(Application app) {

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor(message ->
                Timber.tag("OkHttp").d(message));
        interceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);

        int Mibs = 1024 * 1024;
        int cacheSizeInMibs = 50;
        Cache cache = null;
        File httpCacheDirectory = new File(app.getCacheDir(), "network_responses");
        try {
            httpCacheDirectory.mkdirs();
            cache = new Cache(httpCacheDirectory, cacheSizeInMibs * Mibs );
            int length = httpCacheDirectory.listFiles().length;
            Timber.i("cache files in cache %s", length);
        } catch (Exception e) {
            e.printStackTrace();
            Timber.d("Could not create Cache!" );
        }

        OkHttpClient client = OkCacheControl.on(new OkHttpClient.Builder())
                .overrideServerCachePolicy(30, MINUTES)
                .forceCacheWhenOffline(() -> isNetworkAvailable(app))
                .apply()
                .cache(cache)
                .addInterceptor(interceptor).build();

        return new Retrofit.Builder()
                .baseUrl("https://api.zonky.cz/")
                .addConverterFactory(MoshiConverterFactory.create())
                .addCallAdapterFactory(new LiveDataRetrofitAdapterFactory())
                .client(client)
                .build()
                .create(ZonkyService.class);
    }

    private static boolean isNetworkAvailable(Application app) {
        ConnectivityManager connectivityMgr = (ConnectivityManager) app.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityMgr != null ? connectivityMgr.getActiveNetworkInfo() : null;
        return networkInfo != null && networkInfo.isConnected();
    }

    @Singleton @Provides
    AppDB provideDb(Application app) {
        return Room.databaseBuilder(app, AppDB.class,"zonky.db").build();
    }

    @Singleton @Provides
    LoansDao provideUserDao(AppDB db) {
        return db.loansDao();
    }
}
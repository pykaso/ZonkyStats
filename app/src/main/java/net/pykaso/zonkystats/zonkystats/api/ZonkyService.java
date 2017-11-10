package net.pykaso.zonkystats.zonkystats.api;


import android.arch.lifecycle.LiveData;

import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

/**
 * Zonky REST API
 * Retrofit
 */
public interface ZonkyService {

    // Api doesn't provide agregated data.
    // One option is to set the X-Size to a large enough number to retrieve complete data
    // for the given interval and then calculate the aggregation. (this case)
    // Another Option - separate request for Every Month. It uses the API feature which provides
    // information about the total number of filters matching the filter in a header (X-Total).
    @Headers({
            "X-Page: 0",
            "X-Size: 999999",
            "X-Sort: datePublished"
    })
    @GET("loans/marketplace")
    LiveData<ApiResponse<List<LoansResponse>>> getLoans(
            @Query("datePublished__gte") String fromDate,
            @Query("datePublished__lt") String toDate,
            @Query("fields") String fields);
}

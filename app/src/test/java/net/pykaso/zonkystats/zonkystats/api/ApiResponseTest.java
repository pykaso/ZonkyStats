package net.pykaso.zonkystats.zonkystats.api;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Response;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

@RunWith(JUnit4.class)
public class ApiResponseTest {
    @Test
    public void exception() {
        Exception exception = new Exception("what the hell");
        ApiResponse<String> apiResponse = new ApiResponse<>(exception);
        assertThat(apiResponse.body, nullValue());
        assertThat(apiResponse.code, is(500));
        assertThat(apiResponse.errorMessage, is("what the hell"));
    }

    @Test
    public void success() {
        ApiResponse<String> apiResponse = new ApiResponse<>(Response.success("body", Headers.of("x-total", "999")));
        assertThat(apiResponse.errorMessage, nullValue());
        assertThat(apiResponse.code, is(200));
        assertThat(apiResponse.body, is("body"));
        assertThat(apiResponse.total, is(999));
    }

    @Test
    public void error() {
        ApiResponse<String> response = new ApiResponse<String>(Response.error(401,
                ResponseBody.create(MediaType.parse("application/txt"), "Houston, we have a problem!")));
        assertThat(response.code, is(401));
        assertThat(response.errorMessage, is("Houston, we have a problem!"));
    }
}
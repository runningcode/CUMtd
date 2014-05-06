package com.osacky.cumtd.api;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.octo.android.robospice.retrofit.RetrofitGsonSpiceService;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.converter.Converter;
import retrofit.converter.GsonConverter;

public class CUMTDApiService extends RetrofitGsonSpiceService {

    private static final String SERVER_URL = "https://developer.cumtd.com/api/v2.2/json";
    private static final String API_KEY = "d0c958a61534448c808f35a8b64a6cb9";
    private static final int THREAD_COUNT = 2;

    private static final RequestInterceptor requestInterceptor = new RequestInterceptor() {
        @Override
        public void intercept(RequestFacade request) {
            request.addQueryParam("key", API_KEY);
        }
    };

    private static final Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    @Override
    protected String getServerUrl() {
        return SERVER_URL;
    }

    @Override
    public int getThreadCount() {
        return THREAD_COUNT;
    }

    @Override
    protected Converter createConverter() {
        return new GsonConverter(gson);
    }

    @Override
    protected RestAdapter.Builder createRestAdapterBuilder() {
        return new RestAdapter.Builder()
                .setConverter(createConverter())
                .setRequestInterceptor(requestInterceptor)
                .setEndpoint(getServerUrl())
                .setLogLevel(RestAdapter.LogLevel.BASIC);
    }
}

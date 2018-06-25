package com.demo.test;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.interceptors.HttpLoggingInterceptor;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;

public class DemoApplication extends Application {
  private File httpCacheDirectory;

  @Override
  public void onCreate() {
    super.onCreate();
    HttpLoggingInterceptor.Level logLevel = HttpLoggingInterceptor.Level.BODY;
    HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
    interceptor.setLevel(logLevel);
    httpCacheDirectory = new File(this.getCacheDir(), "responses");
    int cacheSize = 10 * 1024 * 1024; // 10 MiB
    Cache cache = new Cache(httpCacheDirectory, cacheSize);

    OkHttpClient httpClient = new OkHttpClient.Builder()
      .addNetworkInterceptor(REWRITE_RESPONSE_INTERCEPTOR)
      .retryOnConnectionFailure(false)
      .connectTimeout(6, TimeUnit.MINUTES)
      .readTimeout(6, TimeUnit.MINUTES)
      .writeTimeout(6, TimeUnit.MINUTES)
      .addInterceptor(new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
          Request request = chain.request();

          if (!isOnline(DemoApplication.this)) {

            int maxStale = 60 * 60 * 24 * 28; // tolerate 4-weeks stale
            request = request.newBuilder()
              .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
              .build();
          }

          return chain.proceed(request);
        }
      }).addInterceptor(interceptor)
      .protocols(Arrays.asList(Protocol.HTTP_1_1))
      .cache(cache)
      .build();
    AndroidNetworking.initialize(this, httpClient);
  }

  private static final Interceptor REWRITE_RESPONSE_INTERCEPTOR = chain -> {
    Response originalResponse = chain.proceed(chain.request());
    String cacheControl = originalResponse.header("Cache-Control");

    if (cacheControl == null || cacheControl.contains("no-store") || cacheControl.contains("no-cache") ||
      cacheControl.contains("must-revalidate") || cacheControl.contains("max-age=0")) {
      return originalResponse.newBuilder()
        .header("Cache-Control", "public, max-age=" + 10)
        .build();
    } else {
      return originalResponse;
    }
  };


  public static boolean isOnline(Context context) {
    ConnectivityManager cm = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo netInfo = cm.getActiveNetworkInfo();
    return netInfo != null && netInfo.isConnectedOrConnecting();
  }
}

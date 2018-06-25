package com.demo.test;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.OkHttpResponseListener;
import com.demo.test.models.MediaList;
import com.demo.test.models.Page;
import com.demo.test.utils.PaginationScrollListener;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Response;


public class MainActivity extends AppCompatActivity {

  private static final String TAG = "MainActivity";

  PaginationAdapter adapter;
  LinearLayoutManager linearLayoutManager;
  List<Page> pages;
  RecyclerView rv;
  ProgressBar progressBar;

  private static final int PAGE_START = 100;
  private boolean isLoading = false;
  private boolean isLastPage = false;
  // limiting to 5 for this tutorial, since total pages in actual API is very large. Feel free to modify.
  private int TOTAL_PAGES = 10;
  private int currentPage = PAGE_START;

  private String searchText = "A";


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    pages = new ArrayList<>();
    rv = (RecyclerView) findViewById(R.id.main_recycler);
    progressBar = (ProgressBar) findViewById(R.id.main_progress);


    linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
    rv.setLayoutManager(linearLayoutManager);

    rv.setItemAnimator(new DefaultItemAnimator());


    rv.addOnScrollListener(new PaginationScrollListener(linearLayoutManager) {
      @Override
      protected void loadMoreItems() {
        isLoading = true;
        currentPage += 1;
        Log.i("values", String.valueOf(currentPage));
        // mocking network delay for API call
        new Handler().postDelayed(new Runnable() {
          @Override
          public void run() {
            loadNextItem();
          }
        }, 1000);
      }

      @Override
      public int getTotalPageCount() {
        return TOTAL_PAGES;
      }

      @Override
      public boolean isLastPage() {
        return isLastPage;
      }

      @Override
      public boolean isLoading() {
        return isLoading;
      }
    });

    loadFirstPage();

  }

  private void loadNextItem() {
    Log.d(TAG, "loadFirstPage: ");
    AndroidNetworking.get("https://en.wikipedia.org//w/api.php?action=query&format=json&prop=pageimages%7Cpageterms&generator=prefixsearch&redirects=1&formatversion=2&piprop=thumbnail&pithumbsize=1000&pilimit=10&wbptterms=description&gpssearch=" + searchText + "&gpslimit=" + currentPage)
      .setPriority(Priority.IMMEDIATE)
      .setTag("test")
      .build().getAsOkHttpResponse(new OkHttpResponseListener() {
      @Override
      public void onResponse(Response response) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        try {
          MediaList busSearchModel = objectMapper.readValue(response.body().byteStream(), MediaList.class);
          List<Page> results = busSearchModel.getQuery().getPages();
          progressBar.setVisibility(View.GONE);
          pages.clear();
          pages.addAll(results);
          TOTAL_PAGES=+1;
          adapter.notifyDataSetChanged();
          adapter.addLoadingFooter();
        } catch (IOException e) {
          e.printStackTrace();
        }
//
      }

      @Override
      public void onError(ANError anError) {
        Toast.makeText(MainActivity.this,"OPPS Someting went wrong",Toast.LENGTH_LONG).show();

        Log.i("valies", anError.getErrorDetail());

      }
    });

  }


  private void loadFirstPage() {
    Log.d(TAG, "loadFirstPage: ");
    AndroidNetworking.get("https://en.wikipedia.org//w/api.php?action=query&format=json&prop=pageimages%7Cpageterms&generator=prefixsearch&redirects=1&formatversion=2&piprop=thumbnail&pithumbsize=1000&pilimit=10&wbptterms=description&gpssearch=" + searchText + "&gpslimit=" + currentPage)
      .setPriority(Priority.IMMEDIATE)
      .setTag("test")
      .build().getAsOkHttpResponse(new OkHttpResponseListener() {
      @Override
      public void onResponse(Response response) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        try {
          MediaList busSearchModel = objectMapper.readValue(response.body().byteStream(), MediaList.class);
          List<Page> results = busSearchModel.getQuery().getPages();
          progressBar.setVisibility(View.GONE);
          pages.clear();
          pages.addAll(results);
          adapter = new PaginationAdapter(MainActivity.this, pages);
          rv.setAdapter(adapter);
          adapter.notifyDataSetChanged();
          adapter.addLoadingFooter();
        } catch (IOException e) {
          e.printStackTrace();
        }
//
      }

      @Override
      public void onError(ANError anError) {
        progressBar.setVisibility(View.GONE);

        Log.i("valies", anError.getErrorDetail());

      }
    });

  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);

    MenuItem search_item = menu.findItem(R.id.mi_search);

    android.support.v7.widget.SearchView searchView = (android.support.v7.widget.SearchView) search_item.getActionView();
    searchView.setFocusable(true);
    searchView.setQueryHint("Search");

    searchView.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener() {
      @Override
      public boolean onQueryTextSubmit(String query) {
        searchText = query;
        loadFirstPage();
        return false;
      }

      @Override
      public boolean onQueryTextChange(String newText) {
        searchText = newText;
        if (!TextUtils.isEmpty(newText)) {
          loadFirstPage();
        }
        return false;
      }
    });


    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    return super.onOptionsItemSelected(item);
  }

}

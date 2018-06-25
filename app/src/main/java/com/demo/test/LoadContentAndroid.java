package com.demo.test;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;
import android.widget.ProgressBar;

public class LoadContentAndroid extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_load_content_android);
    ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress);
    WebView webView = (WebView) findViewById(R.id.webView);

  }
}

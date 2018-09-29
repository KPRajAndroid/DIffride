package com.diff.user.app.Activities;

import android.graphics.Bitmap;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.diff.user.app.Helper.URLHelper;
import com.diff.user.app.Models.AccessDetails;
import com.diff.app.R;

public class ActivityPrivacyPolicy extends AppCompatActivity {

    private WebView termsView;
    private ProgressBar progressBar;
    private AlertDialog progressDialog;
    private ImageView backArrow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);

//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        termsView = (WebView) findViewById(R.id.help_view);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        backArrow = (ImageView) findViewById(R.id.backArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


        termsView.setWebViewClient(new myWebClient());
        termsView.getSettings().setJavaScriptEnabled(true);
        termsView.loadUrl(URLHelper.PRIVACY);

    }

    public class myWebClient extends WebViewClient
    {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            progressBar.setVisibility(View.VISIBLE);
            //progressDialog.show();
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            progressBar.setVisibility(View.GONE);
            //progressDialog.dismiss();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home)
            onBackPressed();
        return super.onOptionsItemSelected(item);
    }

}

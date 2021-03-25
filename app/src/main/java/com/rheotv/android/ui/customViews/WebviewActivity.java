package com.rheotv.android.ui.customViews;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.URLUtil;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.rheotv.android.R;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;
import com.segment.analytics.Properties;

import java.net.URISyntaxException;
import java.util.HashMap;

public class WebviewActivity extends AppCompatActivity {

    private WebView webView;
    String url;

    @SuppressLint("SetJavaScriptEnabled")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() != null && getIntent().getStringExtra("URL") != null) {
            setContentView(R.layout.activity_webview);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            webView = (WebView) findViewById(R.id.webView1);
            url = getIntent().getStringExtra("URL");

            webView.getSettings().setJavaScriptEnabled(true);
            webView.setWebViewClient(new WebViewClient() {
                String currentUrl;

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    currentUrl = url;

                    if (url.startsWith("http") || url.startsWith("https")) {
                        return false;
                    }
                    if (url.startsWith("intent") && url.contains("instagram")) {//handled only for Instagram
                        if (!isPackageInstalled(WebviewActivity.this, "com.instagram.android")) {
                            Toast.makeText(WebviewActivity.this, "Instagram is not installed", Toast.LENGTH_SHORT).show();
                            return false;
                        }
                        try {
                            String[] urlParts = url.split(":");
                            if (urlParts.length > 1 && urlParts[1] != null && urlParts[1] != "" && !urlParts[1].contains("http")) {
                                url = "https:" + urlParts[1];
                                Uri uri = Uri.parse(url);
                                Intent likeIng = new Intent(Intent.ACTION_VIEW, uri);

                                likeIng.setPackage("com.instagram.android");
                                startActivity(likeIng);
                                return true;
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return true;
                    }
                    return true;
                }
            });
            webView.loadUrl(getIntent().getStringExtra("URL"));
        } else {
            finish();
        }

        if (getIntent() != null && getIntent().hasExtra(AppConstants.SCREEN_SOURCE)) {
            HashMap<String, Object> properties = new HashMap<>();
            properties.put(AppConstants.SCREEN_NAME, SegmentConstants.SCREEN_WEB_VIEW);
            properties.put(AppConstants.SCREEN_SOURCE, getIntent().getStringExtra(AppConstants.SCREEN_SOURCE));
            SegmentTracker.getInstance(this).recordScreenName(SegmentConstants.SCREEN_WEB_VIEW, properties);

        }
    }

    public static boolean isPackageInstalled(Context c, String targetPackage) {
        PackageManager pm = c.getPackageManager();
        try {
            PackageInfo info = pm.getPackageInfo(targetPackage, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_webview, menu);
        if (url != null && url.contains("facebook")) {
            MenuItem menuItem = menu.findItem(R.id.instaBtn);
            menuItem.setVisible(false);
        }
        else if (url != null && url.contains("instagram")) {
            MenuItem menuItem = menu.findItem(R.id.facebookBtn);
            menuItem.setVisible(false);
        }
        else
        {
            MenuItem menuItemFacebook = menu.findItem(R.id.facebookBtn);
            menuItemFacebook.setVisible(false);
            MenuItem menuItemInsta = menu.findItem(R.id.instaBtn);
            menuItemInsta.setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    public String getFacebookPageURL(Context context) {
        PackageManager packageManager = context.getPackageManager();
        try {
            int versionCode = packageManager.getPackageInfo("com.facebook.katana", 0).versionCode;
            if (versionCode >= 3002850) { //newer versions of fb app
                return "fb://facewebmodal/f?href=" + url;
            } else { //older versions of fb app
                return "fb://page/" + "getrheotv";
            }
        } catch (PackageManager.NameNotFoundException e) {
            return url; //normal web url
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.facebookBtn) {
            if (isPackageInstalled(this, "com.facebook.katana")) {
                Intent facebookIntent = new Intent(Intent.ACTION_VIEW);
                String facebookUrl = getFacebookPageURL(this);
                facebookIntent.setData(Uri.parse(facebookUrl));
                try {
                    startActivity(facebookIntent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else
                Toast.makeText(this, "Facebook is not installed", Toast.LENGTH_SHORT).show();
        }
        if (id == R.id.instaBtn) {
            if (isPackageInstalled(this, "com.instagram.android")) {
                Uri uri = Uri.parse(url);
                Intent likeIng = new Intent(Intent.ACTION_VIEW, uri);
                likeIng.setPackage("com.instagram.android");
                try {
                    startActivity(likeIng);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://instagram.com/getrheotv")));
                }
            } else
                Toast.makeText(WebviewActivity.this, "Instagram is not installed", Toast.LENGTH_SHORT).show();

        } else if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}

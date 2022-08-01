package com.example.flickrviewer;

/*
    This class creates the FlickrWebpage activity. It loads the url of a given url, this is from one
        of the images in the MainActivity that is clicked.
 */

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.webkit.WebView;

public class FlickrWebpage extends AppCompatActivity {

    /**
     * This method overrides the onCreate method. It sets up values that will be used in the
     *      activity. It sets up a webview with a url string passed from the extras.
     * Takes in a single bundle object for a parameter.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flickr_webpage);
        WebView browser = findViewById(R.id.webview);
        Bundle bundle = getIntent().getExtras();
        String url = (String) bundle.get("url");
        if (url != null) {
            browser.loadUrl(url);
        }
    }
}
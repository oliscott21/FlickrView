package com.example.flickrviewer;

/*
    This class acts as an IntentService. It allows the app to continuously change the content of the
        ImageViews every ten seconds. The app will use this service in the case where it is in the
        onStop() section of the lifecycle. onResume() and onDestroy() stop the service.
 */

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class FlickrFetcherService extends IntentService {

    Handler handler;
    public static volatile boolean shouldContinue;

    public FlickrFetcherService() {
        super("FlickrFetcherService");
    }

    /**
     * This method overrides the onCreate method. It sets up values that will be used in the
     *      activity.
     * Takes in a single bundle object for a parameter.
     */
    @Override
    public void onCreate() {
        handler = new Handler();
        shouldContinue = true;
        super.onCreate();
    }

    /**
     * This method is the performs the background duties of the IntentService. It uses the Flickr
     *      api to get two images. It makes a JSONObject of each image, and adds these to an Object
     *      array. It then converts the two JSONObjects to bitmaps and adds them to a Bitmap array.
     *      That array is then added to the Object array. That array is sent as a msg via a
     *      Messenger. After sending, it sleeps for 10 seconds. This runs until shouldContinue is
     *      change to false.
     * It takes in a single Object array for a parameter.
     * It returns a bitmap array with the two bitmaps created.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle == null) {
            return;
        }

        Messenger messenger = (Messenger) bundle.get("MESSENGER");

        while (true) {

            if (!shouldContinue) {
                break;
            }

            Object[] data = new Object[3];
            Message msg = Message.obtain();
            Bitmap[] bmArr = new Bitmap[3];
            try {
                StringBuilder jsonStr = new StringBuilder();
                String line;
                URL url = new URL(
                        "https://www.flickr.com/services/rest/?method=flickr.photos." +
                                "getRecent&api_key=c68268937007bf1cc8ac4be2d67276b0&extras=" +
                                "url_c,original_format,date_taken,tags&per_page=2&page=1" +
                                "&format=json&nojsoncallback=1");
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                while ((line = in.readLine()) != null) {
                    jsonStr.append(line);
                }
                in.close();
                JSONObject json = new JSONObject(String.valueOf(jsonStr)).getJSONObject("photos");
                JSONArray jsonArr = json.getJSONArray("photo");
                JSONObject picOne = jsonArr.getJSONObject(0);
                JSONObject picTwo = jsonArr.getJSONObject(1);
                data[1] = picOne;
                data[2] = picTwo;
                if (!picOne.isNull("url_c")) {
                    bmArr[0] = getBitmapFromURL((String) picOne.get("url_c"));
                } else {
                    bmArr[0] = BitmapFactory.decodeResource(getResources(), R.drawable.flickr);
                }
                if (!picTwo.isNull("url_c")) {
                    bmArr[1] = getBitmapFromURL((String) picTwo.get("url_c"));
                } else {
                    bmArr[1] = BitmapFactory.decodeResource(getResources(), R.drawable.flickr);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            data[0] = bmArr;

            msg.obj = data;
            try {
                messenger.send(msg);
                Thread.sleep(10000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This method takes in a String that represents a URL. It connects to the URL and gets
     *      the image there. It then creates a bitmap of that image.
     * It takes in a single String as a parameter.
     * It returns a Bitmap
     */
    private Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}


package com.example.flickrviewer;

/*
    author: Oliver Lester
    description: This activity is responsible for the acting as the main activity for the app. It
        displays two images gotten from the Flickr api. It allows the user to change the images
        displayed and refreshing the page. It does this with an AsyncTask. It also allows for the
        user to click one of the images which takes the user to the FlickrWebsite activity.

    Links used for calendar, tags, and file icons:
        https://icons8.com/icon/1395/document
        https://icons8.com/icon/77/info
        https://icons8.com/icon/set/calendar/ios
 */

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    public static MainActivity itself = null;
    private String creatorOne;
    private String idOne;
    private String uploadOne;
    private String fileOne;
    private String ownerOne;
    private String creatorTwo;
    private String idTwo;
    private String uploadTwo;
    private String fileTwo;
    private String ownerTwo;
    private int pageNum;
    private Intent intentService;

    /**
     * This method overrides the onCreate method. It sets up values that will be used in the
     *      activity. It also starts an async task which allows for images on the activity view to
     *      be changed. It also sets up the UI.
     * Takes in a single bundle object for a parameter.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        creatorOne = "";
        idOne = "";
        uploadOne = "";
        fileOne = "";
        ownerOne = "";
        creatorTwo = "";
        idTwo = "";
        uploadTwo = "";
        fileTwo = "";
        ownerTwo = "";
        pageNum = 1;
        itself = this;
        intentService = new Intent(this, FlickrFetcherService.class);
        new FlickrDownloadTask().execute();
    }

    /**
     * This function overrides the onStop method. It makes a handler, and adds that to the extra of
     *      an intent. The intent is an intent service. That service is started.
     */
    @Override
    protected void onStop() {
        super.onStop();
        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                try {
                    Object[] data = (Object[]) msg.obj;
                    Bitmap[] bmArr = (Bitmap[]) data[0];
                    JSONObject pic1 = (JSONObject) data[1];
                    JSONObject pic2 = (JSONObject) data[2];
                    setViews(bmArr);
                    setData(pic1, pic2);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        intentService.putExtra("MESSENGER", new Messenger(handler));
        startService(intentService);
    }

    /**
     * This function overrides the onStop onResume. It sets a value in the service to false which
     *      allows for the onHandleIntent to complete. It then stops the service.
     */
    @Override
    protected void onResume() {
        super.onResume();
        FlickrFetcherService.shouldContinue = false;
        stopService(intentService);
    }

    /**
     * This function overrides the onStop onDestroy. It sets a value in the service to false which
     *      allows for the onHandleIntent to complete. It then stops the service.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        FlickrFetcherService.shouldContinue = false;
        stopService(intentService);
    }

    /**
     * This method is used as an onclick for img_1. It starts a new intent which starts an activity
     *      that makes a webview with the url associated with the image in the view.
     * Takes in a single view object for a parameter.
     */
    public void imgOneClick(View view) {
        String url = "https://flickr.com/photos/" + creatorOne + "/" + idOne;
        Intent intent = new Intent(this, FlickrWebpage.class);
        intent.putExtra("url", url);
        startActivity(intent);
    }

    /**
     * This method is used as an onclick for img_2. It starts a new intent which starts an activity
     *      that makes a webview with the url associated with the image in the view.
     * Takes in a single view object for a parameter.
     */
    public void imgTwoClick(View view) {
        String url = "https://flickr.com/photos/" + creatorTwo + "/" + idTwo;
        Intent intent = new Intent(this, FlickrWebpage.class);
        intent.putExtra("url", url);
        startActivity(intent);
    }

    /**
     * This method is used as an onclick for the prev button. It decrements the pageNum value by 1.
     * Takes in a single view object for a parameter.
     */
    public void prevClick(View view) {
        if (pageNum > 1) {
            pageNum -= 1;
            itself = this;
            new FlickrDownloadTask().execute();
        }
    }

    /**
     * This method is used as an onclick for the refresh button. It sets that value of pageNum to 1.
     * Takes in a single view object for a parameter.
     */
    public void refreshClick(View view) {
        pageNum = 1;
        itself = this;
        new FlickrDownloadTask().execute();
    }

    /**
     * This method is used as an onclick for the next button. It increments the pageNum value by 1.
     * Takes in a single view object for a parameter.
     */
    public void nextClick(View view) {
        pageNum += 1;
        itself = this;
        new FlickrDownloadTask().execute();
    }

    private final class FlickrDownloadTask extends AsyncTask<Object, Void, Bitmap[]> {

        /**
         * This method is the performs the background duties of the AsyncTask. It uses the Flickr
         *      api to get two images. It sets app data based on the image and creates two bitmaps
         *      based on these images. Placing the bitmaps in an array.
         * It takes in a single Object array for a parameter.
         * It returns a bitmap array with the two bitmaps created.
         */
        @Override
        protected Bitmap[] doInBackground(Object[] objects) {
            try {
                Bitmap[] bmArr = new Bitmap[2];
                StringBuilder jsonStr = new StringBuilder();
                String line;
                URL url = new URL(
                        "https://www.flickr.com/services/rest/?method=flickr.photos." +
                                "getRecent&api_key=c68268937007bf1cc8ac4be2d67276b0&extras=" +
                                "url_c,original_format,date_taken,tags&per_page=2&page=" +
                                pageNum + "&format=json&nojsoncallback=1");
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                while ((line = in.readLine()) != null) {
                    jsonStr.append(line);
                }
                in.close();
                JSONObject json = new JSONObject(String.valueOf(jsonStr)).getJSONObject("photos");
                JSONArray jsonArr = json.getJSONArray("photo");
                JSONObject picOne = jsonArr.getJSONObject(0);
                JSONObject picTwo = jsonArr.getJSONObject(1);
                setData(picOne,picTwo);
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
                return bmArr;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * This method runs after the task is complete, it calls the setViews method.
         * It takes in a single Bitmap array for a parameter.
         */
        @Override
        protected void onPostExecute(Bitmap[] bmArr) {
            try {
                setViews(bmArr);
            } catch (Exception e) {
                e.printStackTrace();
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

    /**
     * This method takes in two JSONObjects and sets data for the app.
     * Takes in two JSONObject for a parameter.
     */
    public void setData(JSONObject picOne, JSONObject picTwo) throws JSONException {
        creatorOne = picOne.getString("owner");
        idOne = picOne.getString("id");
        uploadOne = picOne.getString("datetaken");
        if (!picOne.isNull("originalformat")) {
            fileOne = picOne.getString("originalformat");
        }
        ownerOne = picOne.getString("tags");
        creatorTwo = picTwo.getString("owner");
        idTwo = picTwo.getString("id");
        uploadTwo = picTwo.getString("datetaken");
        if (!picTwo.isNull("originalformat")) {
            fileTwo = picTwo.getString("originalformat");
        }
        ownerTwo = picTwo.getString("tags");
    }

    /**
     * This method takes in a Bitmap array and changes the view based on the bitmaps in the array.s
     * Takes in a single Bitmap array for a parameter.
     */
    public void setViews(Bitmap[] bmArr) {
        ImageView viewOne = findViewById(R.id.img_1);
        viewOne.setImageBitmap(bmArr[0]);
        TextView uploadOneTV = findViewById(R.id.upload_1);
        uploadOneTV.setText(uploadOne);
        TextView typeOneTV = findViewById(R.id.type_1);
        typeOneTV.setText(fileOne);
        TextView ownerOneTV = findViewById(R.id.username_1);
        ownerOneTV.setText(ownerOne);
        ImageView viewTwo = findViewById(R.id.img_2);
        viewTwo.setImageBitmap(bmArr[1]);
        TextView uploadTwoTV = findViewById(R.id.upload_2);
        uploadTwoTV.setText(uploadTwo);
        TextView typeTwoTV = findViewById(R.id.type_2);
        typeTwoTV.setText(fileTwo);
        TextView ownerTwoTV = findViewById(R.id.username_2);
        ownerTwoTV.setText(ownerTwo);
    }

}
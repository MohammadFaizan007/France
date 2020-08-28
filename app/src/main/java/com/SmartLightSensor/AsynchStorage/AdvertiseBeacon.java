package com.SmartLightSensor.AsynchStorage;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class AdvertiseBeacon extends AsyncTask<String, Void, String>
{
    private Exception exception;
    @Override
    protected String doInBackground(String... strings) {
        try {
            URL url = new URL(strings[0]);
            Log.w("AdvertiseBeacon", "The url is: " + strings[0]);
            try {

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.connect();
                int response = conn.getResponseCode();
                Log.w("AdvertiseBeacon", "The response is: " + response);
//                        is = conn.getInputStream();
                conn.disconnect();
            } catch (IOException e){
                Log.e("AdvertiseBeacon error12", e.toString());}
        } catch (MalformedURLException m){
            Log.e("AdvertiseBeacon error22", m.toString());}

        return null;
    }
    protected void onPostExecute(String string) {
        Log.w("AdvertiseBeacon",string+"");
        // TODO: check this.exception
        // TODO: do something with the feed
    }
}

package com.github.danielwojciechowski.ttmobile;

import android.os.AsyncTask;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.List;

class ApiFetcher extends AsyncTask<URL, Integer, Void> {

    private static final HttpTransport HTTP_TRANSPORT = AndroidHttp.newCompatibleTransport();
    private static final JsonFactory JSON_FACTORY = new JacksonFactory();
    private static final HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
        @Override
        public void initialize(HttpRequest request) {
            request.setParser(new JsonObjectParser(JSON_FACTORY));
        }
    });
    private static final GenericUrl url = new GenericUrl("http://ttserver.cfapps.io/travels");

    private int req;

    public ApiFetcher(int req) {
        this.req = req;
    }

    @Override
    protected Void doInBackground(URL... params) {

        try {
            switch (req){
                case 1: {
                    /*fetchAllTravels();*/
                    break;
                }
                case 2: {
                    break;
                }
                case 3: {
                    postTravel();
                    break;
                }
            }

        }catch(IOException | JSONException e){
            e.printStackTrace();
        }
        return null;
    }

    protected void onPostExecute(Void result) {
        switch (req){
            case 1: {
//                Tab1.setTextView();
                break;
            }
            case 2: {
//                Tab1.setTextView();
                break;
            }
            case 3: {
                TTMainActivity.showToast();
                break;
            }
        }

    }

    private void postTravel() throws IOException, JSONException {
        HttpRequest request = requestFactory.buildPostRequest(url, ByteArrayContent.fromString("application/json", prepareFullTravelObject().toString()));
        HttpResponse httpResponse = request.execute();
        final String location = httpResponse.getHeaders().getLocation();
        TTMainActivity.setCurrentTravelUri(location);
    }



    private JSONObject prepareFullTravelObject() throws JSONException {
        List<GeoPoint> points = TTMainActivity.getPoints();
        JSONObject trace = new JSONObject();
        JSONArray list = new JSONArray();
        JSONArray images = new JSONArray();

        for(GeoPoint geoPoint : points) {
            JSONObject point = new JSONObject();
            point.put("latitude", geoPoint.getLatitude());
            point.put("longitude", geoPoint.getLongitude());
            point.put("date", geoPoint.getDate().getTime());
            list.put(point);
        }
        trace.put("trace", list);

        trace.put("userUID", TTMainActivity.getUserUID());

        for (JSONObject image : TTMainActivity.getImages()){
            images.put(image);
        }
        trace.put("photos", images);

        return trace;
    }

/*    private void fetchAllTravels() throws IOException {
        HttpRequest request = requestFactory.buildGetRequest(url);
        HttpResponse httpResponse = request.execute();
    }*/
}

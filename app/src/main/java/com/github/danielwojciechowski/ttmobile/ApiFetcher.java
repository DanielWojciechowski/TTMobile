package com.github.danielwojciechowski.ttmobile;

import android.location.Location;
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
import java.util.Date;
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
                    fetchAllTravels();
                    break;
                }
                case 2: {
                    createTravel();
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
                Tab1.setTextView();
                break;
            }
            case 2: {
                Tab1.setTextView();
                break;
            }
            case 3: {
                Tab1.setTextView();
                break;
            }
        }

    }

    private void createTravel() throws IOException, JSONException {
        HttpRequest request = requestFactory.buildPostRequest(url, ByteArrayContent.fromString("application/json", prepareDataObject().toString()));
        HttpResponse httpResponse = request.execute();
        TTMainActivity.setCurrentTravelUri(httpResponse.getHeaders().getLocation());
        Tab1.setTextVal(httpResponse.getHeaders().getLocation());
    }

    private void postTravel() throws IOException, JSONException {
        HttpRequest request = requestFactory.buildPostRequest(url, ByteArrayContent.fromString("application/json", prepareFullTravelObject().toString()));
        HttpResponse httpResponse = request.execute();
        TTMainActivity.setCurrentTravelUri(httpResponse.getHeaders().getLocation());
        Tab1.setTextVal(httpResponse.getHeaders().getLocation());
    }

    private void createCheckPoint() throws IOException, JSONException {
        HttpRequest request = requestFactory.buildPutRequest(new GenericUrl(TTMainActivity.getCurrentTravelUri()), ByteArrayContent.fromString("application/json", prepareDataObject().toString()));
        HttpResponse httpResponse = request.execute();
        TTMainActivity.setCurrentTravelUri(httpResponse.getHeaders().getLocation());
        Tab1.setTextVal(httpResponse.getHeaders().getLocation());
    }

    private JSONObject prepareDataObject() throws JSONException {
        Location location = TTMainActivity.getInstance().getLocation();
        JSONObject trace = new JSONObject();
        JSONArray list = new JSONArray();
        JSONObject point = new JSONObject();
        point.put("latitude", location.getLatitude());
        point.put("longitude", location.getLongitude());
        point.put("date", new Date().getTime());
        list.put(point);
        trace.put("trace", list);
        return trace;
    }

    private JSONObject prepareFullTravelObject() throws JSONException {
        List<GeoPoint> points = TTMainActivity.getInstance().getPoints();
        JSONObject trace = new JSONObject();
        JSONArray list = new JSONArray();
        for(GeoPoint geoPoint : points) {
            JSONObject point = new JSONObject();
            point.put("latitude", geoPoint.getLatitude());
            point.put("longitude", geoPoint.getLongitude());
            point.put("date", geoPoint.getDate().getTime());
            list.put(point);
        }
        trace.put("trace", list);
        return trace;
    }

    private void fetchAllTravels() throws IOException {
        HttpRequest request = requestFactory.buildGetRequest(url);
        HttpResponse httpResponse = request.execute();
        Tab1.setTextVal(httpResponse.parseAsString());
    }
}

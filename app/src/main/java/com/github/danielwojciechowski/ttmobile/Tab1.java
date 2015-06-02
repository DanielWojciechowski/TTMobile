package com.github.danielwojciechowski.ttmobile;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;

import java.util.Date;

public class Tab1 extends Fragment {

    private static TextView myTextView;
    private static String textVal;
    private ToggleButton toggleButton;

    private static final HttpTransport HTTP_TRANSPORT = AndroidHttp.newCompatibleTransport();
    private static final JsonFactory JSON_FACTORY = new JacksonFactory();
    HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
        @Override
        public void initialize(HttpRequest request) {
            request.setParser(new JsonObjectParser(JSON_FACTORY));
        }
    });

    private static Button btpic, btnup;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v =inflater.inflate(R.layout.tab_1,container,false);
        myTextView = (TextView)v.findViewById(R.id.apiWindow);
        myTextView.setText(textVal);

        toggleButton = (ToggleButton) v.findViewById(R.id.toggleButton);
        prepareToogleButton();


        btpic = (Button) v.findViewById(R.id.cpic);
        btpic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TTMainActivity.getInstance().clickpic();
            }
        });

        btnup = (Button) v.findViewById(R.id.up);
        btnup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TTMainActivity.getInstance().upload();
            }
        });


        return v;
    }

    private void prepareToogleButton() {
        toggleButton.setText(getString(R.string.switch_button_off));
        toggleButton.setTextOff(getString(R.string.switch_button_off));
        toggleButton.setTextOn(getString(R.string.switch_button_on));

        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(toggleButton.isChecked()) {
                    TTMainActivity.setTrackingMode(true);
                    TTMainActivity.getPoints().clear();
                    Tab2.getMap().clear();
                    TTMainActivity.getPoints().add(new GeoPoint(TTMainActivity.getInstance().getLocation().getLatitude(), TTMainActivity.getInstance().getLocation().getLongitude(), new Date()));
                    Tab2.getMap().addMarker(new MarkerOptions().position(new LatLng(TTMainActivity.getInstance().getLocation().getLatitude(), TTMainActivity.getInstance().getLocation().getLongitude())));
                    /*new ApiFetcher(2).execute();*/
                } else {
                    TTMainActivity.setTrackingMode(false);
                    new ApiFetcher(3).execute();
                }
            }
        });
    }

    public static void setTextView(){
        myTextView.setText(textVal);
    }

    public static void setTextVal(String textVal) {
        Tab1.textVal = textVal;
    }
}
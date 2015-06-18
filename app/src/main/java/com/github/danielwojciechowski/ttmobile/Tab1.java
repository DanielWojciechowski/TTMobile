package com.github.danielwojciechowski.ttmobile;

import android.os.Bundle;
import android.os.Handler;
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
import java.util.concurrent.TimeUnit;

public class Tab1 extends Fragment {

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
    private static TextView durationValText;
    private static TextView distanceValText;
    private long init,now, millis,paused;
    private Handler handler;
    private Runnable updater;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tab_1,container,false);

        initializeControls(v);
        prepareToogleButton();
        prepareTimer();
        preparePicButtons(v);

        return v;
    }

    private void initializeControls(View v) {
        toggleButton = (ToggleButton) v.findViewById(R.id.toggleButton);
        durationValText = (TextView) v.findViewById(R.id.durationVal);
        distanceValText = (TextView) v.findViewById(R.id.distanceVal);
    }

    private void preparePicButtons(View v) {
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
    }

    private void prepareTimer() {
        handler = new Handler();
        updater = new Runnable() {
            @Override
            public void run() {
                if (toggleButton.isChecked()) {
                    now=System.currentTimeMillis();
                    millis =now-init;
                    long h = TimeUnit.MILLISECONDS.toHours(millis);
                    long m = TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(h);
                    long s = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(m);
                    String val = h + ":" + (m < 10 ? "0" + m : m) + ":" + (s < 10 ? "0" + s : s);
                    durationValText.setText(val);
                    handler.postDelayed(this, 30);
                }
            }
        };
    }

    @Override
    public void onPause() {
        super.onPause();
        paused = System.currentTimeMillis();
    }

    @Override
    public void onResume() {
        super.onResume();
        init += System.currentTimeMillis() - paused;
    }

    private void prepareToogleButton() {
        toggleButton.setText(getString(R.string.switch_button_off));
        toggleButton.setTextOff(getString(R.string.switch_button_off));
        toggleButton.setTextOn(getString(R.string.switch_button_on));

        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                init = System.currentTimeMillis();
                handler.post(updater);
                if (toggleButton.isChecked()) {
                    TTMainActivity.setTrackingMode(true);
                    TTMainActivity.getPoints().clear();
                    TTMainActivity.getPoints().add(new GeoPoint(TTMainActivity.getInstance().getLocation().getLatitude(), TTMainActivity.getInstance().getLocation().getLongitude(), new Date()));
                    Tab2.getMap().addMarker(new MarkerOptions().position(new LatLng(TTMainActivity.getInstance().getLocation().getLatitude(), TTMainActivity.getInstance().getLocation().getLongitude())));
                    /*new ApiFetcher(2).execute();*/
                } else {
                    TTMainActivity.setTrackingMode(false);
                    new ApiFetcher(3).execute();
                    if(TTMainActivity.getImageView() != null) {
                        TTMainActivity.getImageView().setImageBitmap(null);
                    }
                    Tab2.getMap().clear();
                }
            }
        });
    }

    public static TextView getDistanceValText() {
        return distanceValText;
    }
}
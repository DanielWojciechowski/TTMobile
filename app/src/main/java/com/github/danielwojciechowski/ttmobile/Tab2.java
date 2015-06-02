package com.github.danielwojciechowski.ttmobile;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;

public class Tab2 extends Fragment {

    private static TextView locationTv;
    private static SupportMapFragment supportMapFragment;
    private static GoogleMap googleMap;
    private static View view;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.tab_2, container, false);
        locationTv = (TextView) view.findViewById(R.id.latlongLocation);

        googleMap = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.googleMap)).getMap();
        googleMap.setMyLocationEnabled(true);
        MapsInitializer.initialize(view.getContext());

        TTMainActivity mainActivity = TTMainActivity.getInstance();
        mainActivity.setGoogleMap(googleMap);
        mainActivity.prepareGPS();

        return view;
    }



    public static void updateLocationText(double latitude, double longitude) {
        if(locationTv != null) {
            locationTv.setText("Latitude:" + latitude + ", Longitude:" + longitude);
        }
    }

    public static GoogleMap getMap(){
        return googleMap;
    }

    public static View getMapView() {
        return view;
    }
}
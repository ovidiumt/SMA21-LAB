package com.upt.cti.googlemaps;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.ui.IconGenerator;
import com.upt.cti.googlemaps.databinding.ActivityMapsBinding;

import java.util.Collections;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private final int REQ_PERMISSION = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if(checkPermission())
            mMap.setMyLocationEnabled(true);
        else askPermission();

        // Add a marker in Sydney and move the camera
        LatLng facultate = new LatLng(45.7450284, 21.2275766);
        mMap.addMarker(new MarkerOptions().position(facultate).title("Marker UPT"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(facultate));

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        IconGenerator iconGenerator = new IconGenerator(this);
        iconGenerator.setColor(ContextCompat.getColor(this, android.R.color.holo_blue_dark));
        iconGenerator.setTextAppearance(R.color.black);
        mMap.addMarker(new MarkerOptions().position(facultate).icon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon("Facultate"))));

        drawPolyLineOnMap(Collections.singletonList(facultate), mMap);

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if(marker.getPosition().equals(facultate))
                    Toast.makeText(MapsActivity.this, "I'm studying", Toast.LENGTH_LONG).show();
                return false;
            }
        });

    }

    private boolean checkPermission(){
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_DENIED);
    }

    private void askPermission(){
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQ_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case REQ_PERMISSION:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED){
                    if(checkPermission()) mMap.setMyLocationEnabled(true);
                    else return;
                }
                break;
        }
    }

    public void drawPolyLineOnMap(List<LatLng> list, GoogleMap googleMap){

        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.GREEN);
        polylineOptions.width(8);
        polylineOptions.addAll(list);
        googleMap.addPolyline(polylineOptions);

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for(LatLng latLng : list)
            builder.include(latLng);

        builder.build();
    }
}
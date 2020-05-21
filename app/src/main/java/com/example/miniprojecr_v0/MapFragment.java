package com.example.miniprojecr_v0;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.SettingInjectorService;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationManager locationManager, markerManager;
    private LocationListener locationListener, markerListener;
    private LatLng currentLoc = new LatLng(-34, 151);;
    private Marker myLocation;
    private Button button;
    private String url = "https://geocode.xyz/-36.86290,174.78184?json=1";
    private RequestQueue requestQueue;




    public MapFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_map, container, false);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        button = (Button) view.findViewById(R.id.MakeMark);
        button.setBackgroundColor(Color.YELLOW);
        requestQueue = Volley.newRequestQueue(getContext());

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_0);
        mapFragment.getMapAsync(this);


        locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);


        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            return;
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = button.getText().toString();
                if(text.equals("Make marks")){
                    JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                JSONObject alt = response.getJSONObject("alt");
                                JSONArray jsonArray = alt.getJSONArray("loc");
                                for(int i = 0; i < jsonArray.length(); i++){
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    LatLng latLng = new LatLng(Double.parseDouble(jsonObject.getString("latt")), Double.parseDouble(jsonObject.getString("longt")));
                                    mMap.addMarker(new MarkerOptions().position(latLng).title("location from API"));

                                }


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("error", error.getMessage());
                        }
                    });
                    requestQueue.add(request);
                    button.setText("Clear Marks");


                }
                else {
                    button.setText("Make marks");
                    mMap.clear();
                }



            }
        });


    }




    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        myLocation = mMap.addMarker(new MarkerOptions().position(currentLoc));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLoc));

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                myLocation.remove();
                currentLoc = new LatLng(location.getLatitude(), location.getLongitude());
                myLocation = mMap.addMarker(new MarkerOptions().position(currentLoc).title("Where I am."));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLoc));
                Log.i("LOG", String.valueOf(location.getLongitude()));
                Log.i("LAT", String.valueOf(location.getLatitude()));



            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);

            }
        };
        try {
            //refesh the currest location every 2 seconds
            locationManager.requestLocationUpdates("gps", 2000, 0, locationListener);
        }catch (SecurityException E){
            E.printStackTrace();
        }


    }


}

package com.example.sveid.uoftmap;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;


import org.json.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnInfoWindowClickListener {

    private GoogleMap mMap;
    private final static int MY_PERMISSIONS_FINE_LOCATION = 101;
    private JSONArray jsonarray;
    private JSONArray jsonarray1;

    private Map<String, Marker> markers = new HashMap<>(); //stores map markers
    private ArrayList points = new ArrayList(); //stores names and codes of markers in strings
    private Marker lastMarker; //last marker searched by the user
    private PopupWindow pw;
    private ArrayList pois = new ArrayList<PoI>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        lastMarker = null;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
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
        try {
            jsonarray = new JSONArray(loadJSONFromAsset("buildingsList.Json"));

            for (int i = 0; i < jsonarray.length(); i++) {

                //parsing JSON - thanks COBALT API for the data
                JSONObject jsonobject = jsonarray.getJSONObject(i);
                String code = jsonobject.getString("code");
                String name = jsonobject.getString("name");
                String lat = jsonobject.getString("lat");
                String lng = jsonobject.getString("lng");
                String campus = jsonobject.getString("campus");
                String address = jsonobject.getJSONObject("address").getString("street");
                String postal = jsonobject.getJSONObject("address").getString("postal");

                PoI poi = new PoI(code, name, postal, address);

                //creating markers
                if (campus.equals("UTSG") && !lat.equals("0")) {
                    points.add(code + " " + name);
                    LatLng cords = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
                    Marker marker = mMap.addMarker(new MarkerOptions().snippet(address).position(cords).title(code + "  " + name).icon(BitmapDescriptorFactory.fromResource(R.mipmap.buidling_off)));
                    markers.put(code, marker);
                    pois.add(poi);

                    //points.add(code + " " + name);

                }
            }

            jsonarray1 = new JSONArray(loadJSONFromAsset("foodList.json"));

            for (int i = 0; i < jsonarray1.length(); i++) {

                //parsing JSON - thanks COBALT API for the data
                JSONObject jsonobject = jsonarray1.getJSONObject(i);
                String code = "" + i;
                if (i < 10) {
                    code = "0" + code;
                }
                String name = jsonobject.getString("name");
                String lat = jsonobject.getString("lat");
                String lng = jsonobject.getString("lng");
                String campus = jsonobject.getString("campus");
                String address = jsonobject.getString("address");
                String url = jsonobject.getString("url");
                String description = jsonobject.getString("description");
                PoI poi = new PoI(code, name, address, url, description);

                //creating markers
                if (campus.equals("UTSG") && !lat.equals("0")) {
                    points.add(code + " " + name);
                    LatLng cords = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
                    Marker marker = mMap.addMarker(new MarkerOptions().snippet(address).position(cords).title(name).icon(BitmapDescriptorFactory.fromResource(R.mipmap.food_off)));
                    markers.put(code, marker);
                    pois.add(poi);
                    //points.add(code + " " + name);

                }
            }


            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(43.661922, -79.395312)));
            mMap.animateCamera(CameraUpdateFactory.zoomIn());
            // Zoom out to zoom level 10, animating with a duration of 2 seconds.
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);

            //Search suggestions - WIP
            final FloatingSearchView mSearchView = (FloatingSearchView) findViewById(R.id.floating_search_view);
            mSearchView.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener() {
                @Override
                public void onSearchTextChanged(String oldQuery, final String newQuery) {

                    //get suggestions based on newQuery
                    //pass them on to the search vie
                    ArrayList suggestions = new ArrayList<SearchSuggestion>();

                    mSearchView.swapSuggestions(suggestions);
                }


            });

            //Listens for search entered
            mSearchView.setOnSearchListener(new FloatingSearchView.OnSearchListener() {

                //WIP
                @Override
                public void onSuggestionClicked(SearchSuggestion searchSuggestion) {
                    centerMarker(searchSuggestion.getBody());
                }


                @Override
                public void onSearchAction(String c) {
                    //check if the marker search was done before. If so, change last marker back to default colour
                    if (lastMarker != null && lastMarker.getTitle().substring(0, 1).matches("[a-zA-Z]+")) {
                        lastMarker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.buidling_off));
                    } else if (lastMarker != null) {
                        lastMarker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.food_off));
                    }


                    boolean bool = centerMarker(c); //try to locate the marker by code
                    try {

                        if (!bool) { //if no code matched
                            String code = searchList(c); //search for building name
                            if (code != null) {
                                centerMarker(code.substring(0, 2)); //obtain building code
                            }
                        }
                    } catch (Exception e) {
                    }
                }


            });

            mSearchView.setOnMenuItemClickListener(new FloatingSearchView.OnMenuItemClickListener() {

                @Override
                public void onActionMenuItemSelected(MenuItem item) {
                    if (item.getItemId() == R.id.action_location) {
                        comeToMe();
                    } else if (item.getItemId() == R.id.action_food) {
                        toggleFood();
                    } else if (item.getItemId() == R.id.action_building) {
                        toggleBuildings();
                    }
                }


            });
            mMap.setOnInfoWindowClickListener(this);


        } catch (org.json.JSONException e) {
        }

        //enable my location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            mMap.setMyLocationEnabled(true);

        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_FINE_LOCATION);
            }
        }

    }

    /*Requests permission from the user to access current location

     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_FINE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        mMap.setMyLocationEnabled(true);
                    }

                } else {
                    Toast.makeText(getApplicationContext(), "This app requires location permissions to be granted", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;

        }
    }

    /*Loads the JSON file from the assets

     */

    public String loadJSONFromAsset(String filename) {
        String json = null;
        try {
            InputStream is = this.getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }





    /*Searches for a building

     */

    private String searchList(String querry) {
        String response = null;
        int i = 0;

        while (i < points.size()) {
            String var = points.get(i).toString().toUpperCase();
            if (var.contains(" " + querry.toUpperCase())) { //make sure the building begins with those letters
                response = var; //return if a building is found
                break; //returns the first available. sorry.
            }
            i++; //for loop wouldn't work for some odd reason :(
        }

        return response; //returns a building
    }

    /*Centers the camera to current location

     */
    private void comeToMe() {
        try {
            LocationManager locationManager = (LocationManager)
                    getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            Location location = locationManager.getLastKnownLocation(locationManager
                    .getBestProvider(criteria, false));
            double lat = location.getLatitude();
            double lng = location.getLongitude();
            LatLng cords = new LatLng(lat, lng);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(cords, 18));
        } catch (Exception e) {
        }


    }
    /* Centers the camera to a marker
    *
    * @String title: building code
    *
    * */

    private boolean centerMarker(String title) {
        try {
            Marker marker = markers.get(title.toUpperCase()); //converts to uppercase to match  codes entered in lower case
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 18)); //ZOOM
            marker.showInfoWindow();

            //marker is set to a different colour until next search is made

            if (marker.getTitle().substring(0, 1).matches("[a-zA-Z]+")) {
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.building_on));
            } else {
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.food_on));
            }

            lastMarker = marker;
            return true; //SUCCESS!!!
        } catch (Exception e) {
            return false; //notify that no marker was found
        }
    }


    @Override
    public void onInfoWindowClick(Marker marker) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        PoI poi = getPoi(marker.getTitle());
        builder1.setTitle(marker.getTitle());
        if (poi.getType() == 'B') {
            builder1.setMessage(poi.getAddress() + " " + poi.getPostal());
        } else {
            builder1.setMessage(poi.getAddress() + "\n\n" + poi.getDescription() + "\n\n" + poi.getUrl());

        }

        builder1.setCancelable(true);
        builder1.setPositiveButton(
                "close",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });


        AlertDialog alert11 = builder1.create();
        alert11.show();


    }

    private PoI getPoi(String title) {
        PoI ret = null;
        for (int i = 0; i < pois.size(); i++) {
            PoI poi = (PoI) pois.get(i);
            if (title.contains(poi.getName())) {
                ret = poi;
            }
        }
        return ret;
    }

    public void toggleFood() {
        for (int i = 0; i < points.size(); i++) {
            String point = (String) points.get(i);
            if (!point.substring(0, 1).matches("[a-zA-Z]+")) {
                Marker marker = markers.get(point.substring(0, 2));
                marker.setVisible(!marker.isVisible());

            }
        }
    }

    public void toggleBuildings() {
        for (int i = 0; i < points.size(); i++) {
            String point = (String) points.get(i);
            if ((point.substring(0, 1).matches("[a-zA-Z]+"))) {
                Marker marker = markers.get(point.substring(0, 2));
                boolean visible = marker.isVisible();
                marker.setVisible(!visible);

            }
        }
    }

}

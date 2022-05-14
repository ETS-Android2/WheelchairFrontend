package it.unipi.dii.aide.msss.myapplication;

import android.annotation.SuppressLint;
import android.location.Location;
import android.util.JsonReader;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import it.unipi.dii.aide.msss.myapplication.entities.Landmark;

public class Utils {

    public static ArrayList<Landmark> getLandmarks(LatLng start){

        //start is null or not. Change URL accordingly

        ArrayList<Landmark> landmarks = new ArrayList<>();

        try {
            URL serverEndpoint = new URL("http://127.0.0.1:12345/locations/inaccessible");
            HttpURLConnection connection = (HttpURLConnection) serverEndpoint.openConnection();
            connection.setRequestProperty("User-Agent", "my-rest-app-v0.1");


            if (connection.getResponseCode() == 200) {
                InputStream responseBody = connection.getInputStream();
                InputStreamReader responseBodyReader = new InputStreamReader(responseBody, "UTF-8");
                JsonReader jsonReader = new JsonReader(responseBodyReader);
                jsonReader.beginObject();
                double latitude = 0.0;
                double longitude = 0.0;
                String label = "";
                //find all landmarks returned and store them
                while (jsonReader.hasNext()) {
                    String key = jsonReader.nextName();
                    if (key.equals("latitude")) {
                        latitude = jsonReader.nextDouble();
                    } else if (key.equals("longitude")) {
                        longitude = jsonReader.nextDouble();
                    } else if (key.equals("class")) {
                        label = jsonReader.nextString();
                    } else {
                        jsonReader.skipValue();
                    }

                    if (latitude != 0.0 && longitude != 0.0 && !label.equals("")) {
                        Landmark newLandmark = new Landmark(latitude, longitude, label);
                        landmarks.add(newLandmark);
                    }
                }
            } else {
                // Error handling code goes here
                System.out.println("server not reachable");
            }
        }catch (Exception e){}

        return landmarks;
    }


    public static void initializeMap(GoogleMap mMap, ArrayList<Landmark> landmarks,FusedLocationProviderClient client){


        //handle clicks on map
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                setCamera(latLng,mMap);
            }
        });

        setLandmarks(landmarks,mMap);
        setGpsLocation(client,mMap);
    }


    private static void setLandmarks(ArrayList<Landmark> landmarks,GoogleMap mMap){

        //set Landmarks on the Map
        for(Landmark landmark: landmarks) {
            LatLng position = new LatLng(landmark.getLatitude(), landmark.getLongitude());
            mMap.addMarker(new MarkerOptions().position(position).title(landmark.getLabel()));
        }


    }

    //gets currents GPS position
    @SuppressLint("MissingPermission")
    private static void setGpsLocation(FusedLocationProviderClient locationClient, GoogleMap mMap){

        locationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            LatLng position = new LatLng(location.getLatitude(),location.getLongitude());
                            setCamera(position,mMap);
                        }

                    }
                });
    }

    //change camera focus on map
    private static void setCamera(LatLng currentPosition,GoogleMap mMap){

        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentPosition));
    }
}

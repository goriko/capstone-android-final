package com.example.dar.share;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.utils.PolylineUtils;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.dar.share.NavBarActivity.sContext;

public class TravelFragment extends Fragment implements OnMapReadyCallback,
        LocationEngineListener,
        PermissionsListener {

    private View rootView;

    private int i = 0;

    private MapView mapView;
    private MapboxMap map;
    private PermissionsManager permissionsManager;
    private LocationEngine locationEngine;
    private LocationLayerPlugin locationLayerPlugin;
    private Location userLocation;
    private GeoDataClient geoDataClient;
    private static final LatLngBounds latLngBounds = new LatLngBounds(new com.google.android.gms.maps.model.LatLng(-40, -168), new com.google.android.gms.maps.model.LatLng(71, 136));

    private String originString, destinationString;
    private LatLng originLatlng, destinationLatLng;
    private Marker markerOrigin = null, markerDestination = null;
    private PolylineOptions route = null, option1 = null, option2 = null;

    private AutoCompleteTextView editTextOrigin, editTextDestination;
    private Button buttonLocate, buttonFindRoom;
    private PlaceAutocompleteAdapter placeAutocompleteAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_travel, container, false);

        getActivity().getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        Mapbox.getInstance(getActivity(), getString(R.string.access_token));

        mapView = (MapView) rootView.findViewById(R.id.mapView);
        editTextOrigin = (AutoCompleteTextView) rootView.findViewById(R.id.editTextOrigin);
        editTextDestination = (AutoCompleteTextView) rootView.findViewById(R.id.editTextDestination);
        buttonLocate = (Button) rootView.findViewById(R.id.buttonLocate);
        buttonFindRoom = (Button) rootView.findViewById(R.id.buttonFindRoom);

        mapView.getMapAsync(this::onMapReady);

        geoDataClient = Places.getGeoDataClient(NavBarActivity.sContext, null);
        AutocompleteFilter filter =
                new AutocompleteFilter.Builder().setCountry("PH").build();
        placeAutocompleteAdapter = new PlaceAutocompleteAdapter(NavBarActivity.sContext, geoDataClient, latLngBounds, filter);
        editTextOrigin.setAdapter(placeAutocompleteAdapter);
        editTextDestination.setAdapter(placeAutocompleteAdapter);

        buttonLocate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!editTextOrigin.getText().toString().equals("") && !editTextDestination.getText().toString().equals("")){
                    String temp = geoLocate(editTextOrigin.getText().toString(), editTextDestination.getText().toString());
                    if (!temp.isEmpty()){
                        Toast.makeText(getActivity().getApplicationContext(), temp, Toast.LENGTH_SHORT).show();
                        buttonFindRoom.setVisibility(View.GONE);
                    }else{
                        buttonFindRoom.setVisibility(View.VISIBLE);
                    }
                    InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);
                }else{
                    Toast.makeText(getActivity().getApplicationContext(), "Please fill in origin and destination", Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonFindRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = new SearchRoomFragment(originString, originLatlng, destinationString, destinationLatLng);
                replaceFragment(fragment);
            }
        });

        return rootView;
    }

    private String geoLocate(String Origin, String Destination) {
        originString = Origin;
        destinationString = Destination;
        Geocoder geocoder = new Geocoder(sContext);
        List<Address> list = new ArrayList<>();
        try{
            list = geocoder.getFromLocationName(originString, 1);
        } catch (IOException e) {
            Toast.makeText(NavBarActivity.sContext, e.getMessage().toString(), Toast.LENGTH_LONG);
            return e.getMessage();
        }

        if (list.size() == 0){
            return "Invalid address";
        }else{
            Address originAddress = list.get(0);

            try{
                list = geocoder.getFromLocationName(destinationString, 1);
            } catch (IOException e) {
                Toast.makeText(NavBarActivity.sContext, e.getMessage().toString(), Toast.LENGTH_LONG);
                return e.getMessage();
            }

            if (list.size() == 0){
                Toast.makeText(NavBarActivity.sContext, "Invalid address", Toast.LENGTH_LONG);
                return "Invalid address";
            }else{
                Address destinationAddress = list.get(0);

                mark(originAddress, destinationAddress);
                getRoute(originAddress, destinationAddress);
            }
        }
        return "";
    }
    public void mark(Address origin, Address destination){
        originLatlng = new LatLng(origin.getLatitude(), origin.getLongitude());
        destinationLatLng = new LatLng(destination.getLatitude(), destination.getLongitude());

        if(markerOrigin != null || markerDestination != null){
            map.removeMarker(markerOrigin);
            map.removeMarker(markerDestination);
        }


        com.mapbox.mapboxsdk.geometry.LatLngBounds.Builder latLngBounds = new com.mapbox.mapboxsdk.geometry.LatLngBounds.Builder();
        latLngBounds.include(originLatlng);
        latLngBounds.include(destinationLatLng);
        com.mapbox.mapboxsdk.geometry.LatLngBounds position = latLngBounds.build();

        markerOrigin = map.addMarker(new MarkerOptions().position(originLatlng).title("Origin Address"));
        markerDestination = map.addMarker(new MarkerOptions().position(destinationLatLng).title("Destination Address"));
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(position, 250), 7000);
    }

    private void getRoute(Address origin, Address destination){
        Point originPoint = Point.fromLngLat(origin.getLongitude(), origin.getLatitude());
        Point destinationPoint = Point.fromLngLat(destination.getLongitude(), destination.getLatitude());

        if (route != null){
            map.removePolyline(route.getPolyline());
        }
        if (option1 != null){
            map.removePolyline(option1.getPolyline());
            option1 = null;
        }
        if (option2 != null){
            map.removePolyline(option2.getPolyline());
            option2 = null;
        }

        NavigationRoute.builder(sContext)
                .accessToken("pk.eyJ1IjoiZ29yaWtvIiwiYSI6ImNqbXhlMmU3cDFuc2wzcXM4MmV4aG5reHQifQ.JkqGov_XghkeZ_hmYEH8xg")
                .origin(originPoint)
                .destination(destinationPoint)
                .alternatives(true)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                        if(response.body() == null){
                            Toast.makeText(sContext, "No routes", Toast.LENGTH_SHORT).show();
                            return;
                        }else if(response.body().routes().size() == 0){
                            Toast.makeText(sContext, "No routes", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Integer count = response.body().routes().size();

                        for(int x=0; x<=2 && x<response.body().routes().size(); x++){
                            DirectionsRoute currentRoute = response.body().routes().get(x);

                            List<Point> points = PolylineUtils.decode(currentRoute.geometry(),6);

                            switch (x){
                                case 0:
                                    route = new PolylineOptions()
                                            .width(5);
                                    for(int i = 0; i<points.size(); i++){
                                        route.add(new LatLng(points.get(i).latitude(), points.get(i).longitude()));
                                    }
                                    route.color(Color.BLUE);
                                    break;
                                case 1:
                                    option1 = new PolylineOptions()
                                            .width(5);
                                    for(int i = 0; i<points.size(); i++){
                                        option1.add(new LatLng(points.get(i).latitude(), points.get(i).longitude()));
                                    }
                                    option1.color(Color.GRAY);
                                    map.addPolyline(option1);
                                    break;
                                case 2:
                                    option2 = new PolylineOptions()
                                            .width(5);
                                    for(int i = 0; i<points.size(); i++){
                                        option2.add(new LatLng(points.get(i).latitude(), points.get(i).longitude()));
                                    }
                                    option2.color(Color.GRAY);
                                    map.addPolyline(option2);
                                    break;
                            }
                        }
                        map.addPolyline(route);
                        Toast.makeText(sContext, "routes found: " + count, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                        Toast.makeText(sContext, "Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    //mapbox
    private void enableLocation(){
        if(PermissionsManager.areLocationPermissionsGranted(NavBarActivity.sContext)){
            initializeLocationEngine();
            initializeLocationLayer();
        }else{
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(getActivity());
        }
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        map = mapboxMap;
        enableLocation();
    }

    private void initializeLocationEngine(){
        locationEngine = new LocationEngineProvider(NavBarActivity.sContext).obtainBestLocationEngineAvailable();
        locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
        locationEngine.activate();

        @SuppressLint("MissingPermission")
        Location lastLocation = locationEngine.getLastLocation();
        if(lastLocation != null){
            userLocation = lastLocation;
            setCameraPosition(lastLocation);
        }else{
            locationEngine.addLocationEngineListener(this);
        }
    }

    @SuppressLint("MissingPermission")
    private void initializeLocationLayer(){
        locationLayerPlugin = new LocationLayerPlugin(mapView, map, locationEngine);
        locationLayerPlugin.setLocationLayerEnabled(true);
        locationLayerPlugin.setCameraMode(CameraMode.NONE);
        locationLayerPlugin.setRenderMode(RenderMode.NORMAL);
    }

    private void setCameraPosition(Location location){
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 16.0));
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onConnected() {
        locationEngine.requestLocationUpdates();
    }

    @Override
    public void onLocationChanged(Location location) {
        if(location != null){
            userLocation = location;
            if(NavBarActivity.roomId != null) {
                if (i == 0){
                    buttonLocate.setVisibility(View.GONE);
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("travel").child(NavBarActivity.roomId);
                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            editTextOrigin.setText(dataSnapshot.child("OriginString").getValue().toString());
                            editTextDestination.setText(dataSnapshot.child("DestinationString").getValue().toString());
                            geoLocate(dataSnapshot.child("OriginString").getValue().toString(), dataSnapshot.child("DestinationString").getValue().toString());
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    i++;
                    showUsers();
                }
            }else{
                setCameraPosition(location);
            }
        }
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {

    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted){
            enableLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void replaceFragment(Fragment someFragment) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, someFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void showUsers(){
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("travel").child(NavBarActivity.roomId);
        databaseReference.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()){
                    if(!data.getValue().toString().equals(user.getUid())){
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users").child(data.getValue().toString());
                        reference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                map.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(dataSnapshot.child("Location").child("Latitude").getValue().toString()), Double.parseDouble(dataSnapshot.child("Location").child("Longitude").getValue().toString())))
                                        .title(dataSnapshot.child("Fname").getValue().toString()+" "+dataSnapshot.child("Lname").getValue().toString()));
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
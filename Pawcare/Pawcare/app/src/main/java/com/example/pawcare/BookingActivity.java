package com.example.pawcare;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



public class BookingActivity extends AppCompatActivity implements OnMapReadyCallback {

    private CheckBox checkBoxService1, checkBoxService2, checkBoxService3;
    private CalendarView calendarView;
    private RecyclerView timeslotRecyclerView;
    private Button confirmButton;
    private GoogleMap mMap;
    private TextView selectedLocationText;
    private SupportMapFragment mapFragment;
    private FusedLocationProviderClient locationClient;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 101;
    private static final String TAG = "BookingActivity";

    private String selectedDate;

    private String selectedTimeSlot;

    private String selectedLocationName;
    private String selectedPlaceId;

    private List<String> selectedServices = new ArrayList<>();
    private LatLng selectedLocationLatLng;

    private TextView totalPriceTextView;
    private int totalPrice = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);


        ImageView backButton = findViewById(R.id.imgBack);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Finish the current activity and go back to the previous one
            }
        });

        ImageView homeButton = findViewById(R.id.imgHome);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent homeIntent = new Intent(BookingActivity.this, HomeActivity.class);
                homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(homeIntent);
                finish(); // Optional if you want this activity to close as well
            }
        });

        // Initialize Places API.
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        }

        // Setup Places Client and Autocomplete Fragment
        setupPlacesAutocomplete();

        // Initialize map fragment
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Initialize location client here to ensure it is ready when map is ready
        locationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize other UI components
        setupUIComponents();
    }



    private void setupPlacesAutocomplete() {
        PlacesClient placesClient = Places.createClient(this);
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                updateMapLocation(place.getLatLng(), place.getName());
                selectedLocationName = place.getName();
                selectedPlaceId = place.getId();
                selectedLocationText.setText("Selected: " + selectedLocationName + " - " + selectedPlaceId);
            }

            @Override
            public void onError(@NonNull Status status) {
                Log.i("PlacesAPI Error", "An error occurred: " + status);
            }
        });
    }

    private void updateMapLocation(LatLng latLng, String placeName) {
        if (latLng != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
            mMap.addMarker(new MarkerOptions().position(latLng).title(placeName));
        }
    }


    private void setupUIComponents() {
        checkBoxService1 = findViewById(R.id.checkBoxService1);
        checkBoxService2 = findViewById(R.id.checkBoxService2);
        checkBoxService3 = findViewById(R.id.checkBoxService3);
        calendarView = findViewById(R.id.calendarView);
        timeslotRecyclerView = findViewById(R.id.timeslotRecyclerView);
        confirmButton = findViewById(R.id.confirmButton);
        selectedLocationText = findViewById(R.id.selectedLocationText);
        totalPriceTextView = findViewById(R.id.labelPrice);

        checkBoxService1.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateService("Grooming", isChecked, 50);
        });

        checkBoxService2.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateService("Nail Clipping", isChecked, 20);
        });

        checkBoxService3.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateService("Bathing", isChecked, 30);
        });
        setupCalendarView();
        setupTimeSlots();

        confirmButton.setOnClickListener(v -> {
            if (selectedDate == null || selectedTimeSlot == null || selectedLocationName == null || selectedServices.isEmpty()) {
                Toast.makeText(BookingActivity.this, "Please select all options before confirming.", Toast.LENGTH_LONG).show();
            } else {
                proceedToConfirmation();
            }
        });
    }

    private void updateService(String service, boolean add, int price) {
        if (add) {
            selectedServices.add(service);
            totalPrice += price;
        } else {
            selectedServices.remove(service);
            totalPrice -= price;
        }
        totalPriceTextView.setText("$" + totalPrice);
    }

    private void proceedToConfirmation() {
        // TODO: Launch the confirmation activity, passing along the booking details.
        // You will create an Intent and put extras like the date, time slot, and location name.

        Intent confirmIntent = new Intent(BookingActivity.this, ConfirmationActivity.class);
        confirmIntent.putStringArrayListExtra("services", new ArrayList<>(selectedServices));
        confirmIntent.putExtra("location", selectedLocationName);
        confirmIntent.putExtra("date", selectedDate);
        confirmIntent.putExtra("time", selectedTimeSlot);
        confirmIntent.putExtra("locationLatLng", selectedLocationLatLng);
        startActivity(confirmIntent);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        setupMapSettings();
        enableMyLocation(); // Ensures location is only enabled after map is ready
    }

    private void setupMapSettings() {
        mMap.getUiSettings().setZoomControlsEnabled(true);
    }

    private void enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            mMap.setMyLocationEnabled(true);
            getLastLocation();
        }
    }


    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 12));
                }
            });
        }
    }

    private void fetchNearbyPlaces(LatLng location) {
        String types = "pet_store|pet_care|pet_services";
        int radius = 50000; // 50 km radius
        String apiKey = getString(R.string.google_maps_key);
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                "?location=" + location.latitude + "," + location.longitude +
                "&radius=" + radius +
                "&type=" + types +
                "&key=" + apiKey;

        new FetchPlacesTask().execute(url);
    }

    private class FetchPlacesTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                return downloadUrl(urls[0]);
            } catch (Exception e) {
                Log.e("Background Task", e.toString());
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                parseLocations(result);
            } catch (JSONException e) {
                Log.e("JSON Parser", "Error parsing data " + e.toString());
            }
        }

        private void parseLocations(String json) throws JSONException {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray resultsArray = jsonObject.getJSONArray("results");

            for (int i = 0; i < resultsArray.length(); i++) {
                JSONObject place = resultsArray.getJSONObject(i);
                JSONObject location = place.getJSONObject("geometry").getJSONObject("location");
                LatLng latLng = new LatLng(location.getDouble("lat"), location.getDouble("lng"));
                String placeName = place.getString("name");

                mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(placeName)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            }

            if (resultsArray.length() == 0) {
                Log.d(TAG, "No places found.");
                Toast.makeText(BookingActivity.this, "No pet-related places found nearby.", Toast.LENGTH_LONG).show();
            }
        }

        private String downloadUrl(String strUrl) throws IOException {
            InputStream iStream = null;
            HttpURLConnection urlConnection = null;
            StringBuilder sb = new StringBuilder();
            try {
                URL url = new URL(strUrl);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                iStream = urlConnection.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
            } catch (Exception e) {
                Log.e("Exception", e.toString());
            } finally {
                if (iStream != null) {
                    iStream.close();
                }
                urlConnection.disconnect();
            }
            return sb.toString();
        }
    }
    private void setupCalendarView() {
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, dayOfMonth);
            Log.d("Calendar", "Selected date: " + selectedDate);
        });
    }

    private void setupTimeSlots() {
        ArrayList<String> timeSlots = new ArrayList<>();
        timeSlots.add("09:00 AM");
        timeSlots.add("10:00 AM");
        timeSlots.add("11:00 AM");
        timeSlots.add("12:00 PM");
        timeSlots.add("03:00 PM");
        timeSlots.add("04:00 PM");
        timeSlots.add("05:00 PM");
        timeSlots.add("06:00 PM");
        timeSlots.add("07:00 PM");
        timeSlots.add("08:00 PM");

        TimeSlotAdapter adapter = new TimeSlotAdapter(timeSlots, new TimeSlotAdapter.OnTimeSlotClickListener() {
            @Override
            public void onTimeSlotClick(String timeSlot, int position) {
                selectedTimeSlot = timeSlot;
                // You can also update any UI or variables related to the time slot selection here
            }
        });
        timeslotRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        timeslotRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

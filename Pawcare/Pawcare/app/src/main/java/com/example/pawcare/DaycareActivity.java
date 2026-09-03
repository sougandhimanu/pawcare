package com.example.pawcare;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

public class DaycareActivity extends AppCompatActivity implements OnMapReadyCallback {

    private Button fourHoursButton, eightHoursButton, twelveHoursButton, twentyFourHoursButton, bookButton;
    private TextView priceTextView, selectedLocationText;
    private CalendarView calendarView;
    private GoogleMap mMap;
    private FusedLocationProviderClient locationClient;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 101;
    private static final String TAG = "DaycareActivity";

    private double selectedPrice;
    private int selectedHours;
    private String selectedDate;
    private String selectedLocationName;
    private LatLng selectedLocationLatLng;

    private String selectedPlaceId;

    private SupportMapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daycare);

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
                Intent homeIntent = new Intent(DaycareActivity.this, HomeActivity.class);
                homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(homeIntent);
                finish(); // Optional if you want this activity to close as well
            }
        });

        // Initialize Places API.
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        }

        // Initialize map fragment.
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Initialize location client.
        locationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize UI components.
        calendarView = findViewById(R.id.calendarView);
        priceTextView = findViewById(R.id.priceTextView);
        selectedLocationText = findViewById(R.id.selectedLocationText);
        fourHoursButton = findViewById(R.id.fourHoursButton);
        eightHoursButton = findViewById(R.id.eightHoursButton);
        twelveHoursButton = findViewById(R.id.twelveHoursButton);
        twentyFourHoursButton = findViewById(R.id.twentyFourHoursButton);
        bookButton = findViewById(R.id.confirmButton);

        setupCalendarView();
        setupDurationButtons();
        setupPlacesAutocomplete();

        bookButton.setOnClickListener(v -> proceedToConfirmation());
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

    private void setupCalendarView() {
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar cal = Calendar.getInstance();
            cal.set(year, month, dayOfMonth);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            selectedDate = dateFormat.format(cal.getTime());
        });
    }

    private void setupDurationButtons() {
        fourHoursButton.setOnClickListener(v -> updatePrice(4)); // $50 for 4 hours
        eightHoursButton.setOnClickListener(v -> updatePrice(8)); // $100 for 8 hours
        twelveHoursButton.setOnClickListener(v -> updatePrice(12)); // $150 for 12 hours
        twentyFourHoursButton.setOnClickListener(v -> updatePrice(24)); // $200 for 24 hours
    }

    private void updatePrice(int hours) {
        double basePrice = 50; // Base price for the first 4 hours
        double price = basePrice; // Start with the base price for 4 hours

        if (hours > 4) {
            int additionalBlocks = (hours - 4) / 4; // Calculate additional 4-hour blocks
            for (int i = 0; i < additionalBlocks; i++) {
                price += basePrice; // Add the price for each additional block
            }
        }

        selectedPrice = price; // Update the selected price
        selectedHours = hours; // Update the selected hours
        priceTextView.setText(String.format(Locale.getDefault(), "Total Cost: $%.2f for %d hours", selectedPrice, selectedHours));
    }


    private void proceedToConfirmation() {
        Intent confirmIntent = new Intent(DaycareActivity.this, DaycareConfirmationActivity.class);
        confirmIntent.putExtra("selectedPrice", selectedPrice);
        confirmIntent.putExtra("selectedHours", selectedHours);
        confirmIntent.putExtra("selectedDate", selectedDate);
        confirmIntent.putExtra("selectedLocationName", selectedLocationName);
        confirmIntent.putExtra("selectedLocationLatLng", selectedLocationLatLng);
        startActivity(confirmIntent);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        enableMyLocation();
    }

    private void enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }
        mMap.setMyLocationEnabled(true);
        getLastLocation();
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }
}

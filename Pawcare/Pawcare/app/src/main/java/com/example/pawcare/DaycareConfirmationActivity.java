package com.example.pawcare;

import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Calendar;

public class DaycareConfirmationActivity extends AppCompatActivity implements OnMapReadyCallback {

    private String selectedLocationName;
    private LatLng selectedLocationLatLng;
    private GoogleMap mMap;
    private double selectedPrice;
    private int selectedHours;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dcconfirm);

        TextView tvConfirmationMessage = findViewById(R.id.tvConfirmationMessage);
        TextView tvPrice = findViewById(R.id.tvPrice);
        TextView tvDuration = findViewById(R.id.tvHours);
        TextView tvLocation = findViewById(R.id.tvLocation);
        Button btnCancel = findViewById(R.id.btnCancel);
        Button btnAddToSchedule = findViewById(R.id.btnAddToCalendar);

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
                Intent homeIntent = new Intent(DaycareConfirmationActivity.this, HomeActivity.class);
                homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(homeIntent);
                finish(); // Optional if you want this activity to close as well
            }
        });

        // Retrieve data from the intent
        Intent intent = getIntent();
        selectedLocationName = intent.getStringExtra("selectedLocationName");
        selectedLocationLatLng = intent.getParcelableExtra("selectedLocationLatLng");
        selectedPrice = intent.getDoubleExtra("selectedPrice", 0); // Make sure the key matches
        selectedHours = intent.getIntExtra("selectedHours", 0); // Make sure the key matches


        // Set confirmation message and details
        tvConfirmationMessage.setText("Your daycare booking at " + selectedLocationName + " is confirmed!");
        tvPrice.setText(String.format("Total Cost: $%.2f", selectedPrice));
        tvDuration.setText(String.format("Duration: %d hours", selectedHours));
        tvLocation.setText(selectedLocationName);

        // Initialize the map fragment to show the selected location
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }


        btnAddToSchedule.setOnClickListener(v -> addToCalendar());


        btnCancel.setOnClickListener(v -> finish());
    }

    private void addToCalendar() {
        // Start with a date and time for the appointment
        Calendar beginTime = Calendar.getInstance();
        beginTime.setTimeInMillis(System.currentTimeMillis());

        // For demonstration, let's say the appointment is 2 hours from now
        beginTime.add(Calendar.HOUR_OF_DAY, 2);

        Calendar endTime = (Calendar) beginTime.clone();
        endTime.add(Calendar.HOUR_OF_DAY, selectedHours); // Use selectedHours for the appointment duration

        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.getTimeInMillis())
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getTimeInMillis())
                .putExtra(CalendarContract.Events.TITLE, "Daycare Booking")
                .putExtra(CalendarContract.Events.DESCRIPTION, "Daycare services booked for " + selectedHours + " hours.")
                .putExtra(CalendarContract.Events.EVENT_LOCATION, selectedLocationName)
                .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY);

        startActivity(intent);
    }


    private void updateMapWithMarker() {
        if (mMap != null && selectedLocationLatLng != null) {
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(selectedLocationLatLng).title(selectedLocationName));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLocationLatLng, 15));
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        updateMapWithMarker();
    }
}

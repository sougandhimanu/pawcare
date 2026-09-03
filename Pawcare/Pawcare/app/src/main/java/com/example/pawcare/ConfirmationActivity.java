package com.example.pawcare;

import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import android.text.TextUtils;


public class ConfirmationActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ArrayList<String> selectedServices;  // Change to ArrayList<String>
    private String selectedDate;
    private String selectedTimeSlot;
    private String selectedLocationName;
    private LatLng selectedLocationLatLng;

    private GoogleMap mMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);

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
                Intent homeIntent = new Intent(ConfirmationActivity.this, HomeActivity.class);
                homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(homeIntent);
                finish(); // Optional if you want this activity to close as well
            }
        });

        TextView tvConfirmationMessage = findViewById(R.id.tvConfirmationMessage);
        TextView tvAppointmentDetails = findViewById(R.id.tvAppointmentDetails);
        Button btnAddToCalendar = findViewById(R.id.btnAddToCalendar);
        Button btnCancel = findViewById(R.id.btnCancel);
        TextView tvService = findViewById(R.id.tvService);
        TextView tvCalendar = findViewById(R.id.tvCalendar);
        TextView tvLocation = findViewById(R.id.tvLocation);

        // Retrieve the data from the intent
        Intent intent = getIntent();
        selectedServices = intent.getStringArrayListExtra("services");
        selectedDate = intent.getStringExtra("date");
        selectedTimeSlot = intent.getStringExtra("time");
        selectedLocationName = intent.getStringExtra("location");
        selectedLocationLatLng = intent.getParcelableExtra("locationLatLng");
        Log.d("ConfirmationActivity", "Received LatLng: " + selectedLocationLatLng);

        updateMapWithMarker();

        // Handle null or empty service list
        String serviceList = selectedServices == null || selectedServices.isEmpty()
                ? "No services selected"
                : TextUtils.join(", ", selectedServices);

        // Set the confirmation message and appointment details
        String confirmationMessage = "Your furry friend's appointment for the following services is confirmed and approved: " + serviceList;
        tvConfirmationMessage.setText(confirmationMessage);

        String appointmentDetails = serviceList + " at " + selectedTimeSlot + " on " + selectedDate + " at " + selectedLocationName;
        tvAppointmentDetails.setText(appointmentDetails);

        tvService.setText(serviceList);  // Display selected services
        tvCalendar.setText(selectedDate + " " + selectedTimeSlot);  // Display the date and time slot
        tvLocation.setText(selectedLocationName);  // Display the location

        // Initialize the map fragment to show the selected location
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        btnAddToCalendar.setOnClickListener(v -> addToCalendar(selectedDate, selectedTimeSlot, selectedLocationName, serviceList));
        btnCancel.setOnClickListener(v -> finish());
    }


    private void addToCalendar(String date, String time, String location, String service) {
        Calendar beginTime = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault());
        try {
            Date startDate = dateFormat.parse(date + " " + time);
            beginTime.setTime(startDate);
        } catch (ParseException e) {
            Toast.makeText(this, "Error parsing date/time", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return;
        }
        Calendar endTime = (Calendar) beginTime.clone();
        endTime.add(Calendar.HOUR, 1);

        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.getTimeInMillis())
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getTimeInMillis())
                .putExtra(CalendarContract.Events.TITLE, service + " Appointment")
                .putExtra(CalendarContract.Events.DESCRIPTION, service + " appointment at " + location)
                .putExtra(CalendarContract.Events.EVENT_LOCATION, location)
                .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY);
        startActivity(intent);
    }

    private void updateMapWithMarker() {
        Log.d("ConfirmationActivity", "Updating Map Marker: " + selectedLocationLatLng); // Debug before updating the map
        if (mMap != null && selectedLocationLatLng != null) {
            mMap.clear(); // Clears any existing markers on the map
            mMap.addMarker(new MarkerOptions().position(selectedLocationLatLng).title(selectedLocationName));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLocationLatLng, 15));
        } else {
            Log.d("ConfirmationActivity", "Map or LatLng is null"); // Check if any is null
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Log.d("ConfirmationActivity", "Map is ready. LatLng: " + selectedLocationLatLng); // Confirm map readiness and data availability
        updateMapWithMarker();
    }



}




package com.example.pawcare;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class ServicesActivity extends AppCompatActivity {

    private Button bookAppointmentButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.services_screen);

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
                Intent homeIntent = new Intent(ServicesActivity.this, HomeActivity.class);
                homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(homeIntent);
                finish(); // Optional if you want this activity to close as well
            }
        });

        // Initialize the button and set an onClickListener
        bookAppointmentButton = findViewById(R.id.button_book_appointment);
        bookAppointmentButton.setOnClickListener(v -> {
            // Handle the click event for booking an appointment
            // For example, start a new Activity where the user can schedule their appointment
            Intent intent = new Intent(ServicesActivity.this, BookingActivity.class);
            startActivity(intent);
            // For now, let's just mimic this with a simple log statement
            System.out.println("Book Appointment button clicked!");
        });
    }
}


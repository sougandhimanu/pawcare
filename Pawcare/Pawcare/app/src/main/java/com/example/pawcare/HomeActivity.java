package com.example.pawcare;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private ViewPager2 viewPagerImages;
    private final Handler sliderHandler = new Handler();
    private final List<Integer> imageList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Populate the list with your images
        imageList.add(R.drawable.hair);
        imageList.add(R.drawable.bath);
        imageList.add(R.drawable.nails);
        // Add more images as needed

        viewPagerImages = findViewById(R.id.viewPagerImages);
        ImageAdapter imageAdapter = new ImageAdapter(this, imageList);
        viewPagerImages.setAdapter(imageAdapter);

        // Runnable for the auto-cycling images
        Runnable slideRunnable = new Runnable() {
            @Override
            public void run() {
                if (viewPagerImages != null) {
                    int currentItem = viewPagerImages.getCurrentItem();
                    int totalItems = imageList.size();
                    viewPagerImages.setCurrentItem((currentItem + 1) % totalItems, true);
                }
            }
        };

        // Start auto-cycling with a delay of 3 seconds
        sliderHandler.postDelayed(slideRunnable, 3000);

        // Set up click listeners for cards
        setupCardClickListeners();
    }

    private void setupCardClickListeners() {
        findViewById(R.id.cardAppoint).setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, CalendarActivity.class);
            startActivity(intent);
        });


        findViewById(R.id.cardWalk).setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ServicesActivity.class);
            startActivity(intent);

        });

        findViewById(R.id.cardPetCare).setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, VetActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.cardDayCare).setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, DaycareActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.cardProfile).setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, UserProfileActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        sliderHandler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume auto-cycling with a delay of 3 seconds
        sliderHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (viewPagerImages != null) {
                    int currentItem = viewPagerImages.getCurrentItem();
                    int totalItems = imageList.size();
                    viewPagerImages.setCurrentItem((currentItem + 1) % totalItems, true);
                }
            }
        }, 3000);
    }
}


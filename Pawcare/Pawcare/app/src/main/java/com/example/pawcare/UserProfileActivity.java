package com.example.pawcare;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class UserProfileActivity extends AppCompatActivity {

    private EditText editPetName, editBreed, editAge, editToy, editFoodPreferences, editAllergies, editSpecialNotes;
    private Button btnConfirm, btnEdit;
    private ImageView imgProfilePicture;
    private boolean isEditing = false;

    private boolean hasProfileChanged = false;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private Uri selectedImageUri;
    private static final int PICK_IMAGE_REQUEST_CODE = 123;

    private ImageView imgHome, imgLogout;
    private FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize FirebaseAuth
        auth = FirebaseAuth.getInstance();

        // Setup Home and Logout buttons
        imgHome = findViewById(R.id.imgHome);
        imgLogout = findViewById(R.id.imgLogout);

        imgHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to HomeActivity
                Intent homeIntent = new Intent(UserProfileActivity.this, HomeActivity.class);
                startActivity(homeIntent);
                finish();
            }
        });

        imgLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Sign out from FirebaseAuth
                auth.signOut();

                // Navigate to SignInActivity with clearing the activity stack
                Intent signInIntent = new Intent(UserProfileActivity.this, SignInActivity.class);
                signInIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(signInIntent);
                finish();
            }
        });


        databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(getUserId());
        storageReference = FirebaseStorage.getInstance().getReference().child("profile_images").child(getUserId() + ".jpg");

        imgProfilePicture = findViewById(R.id.imgProfilePicture);
        editPetName = findViewById(R.id.editPetName);
        editBreed = findViewById(R.id.editBreed);
        editAge = findViewById(R.id.editAge);
        editToy = findViewById(R.id.editToy);
        editFoodPreferences = findViewById(R.id.editFoodPreferences);
        editAllergies = findViewById(R.id.editAllergies);
        editSpecialNotes = findViewById(R.id.editSpecialNotes);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnEdit = findViewById(R.id.btnEdit);

        btnConfirm.setOnClickListener(view -> saveProfile());
        btnEdit.setOnClickListener(view -> toggleEditMode());

        loadProfileData();
        setFieldsEnabled(false);

        // Add TextChangedListeners to detect any changes in the EditTexts
        TextWatcher profileTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Set a flag that the profile has been edited
                hasProfileChanged = true;
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        // Assign the TextWatcher to all EditText fields
        editPetName.addTextChangedListener(profileTextWatcher);
        editBreed.addTextChangedListener(profileTextWatcher);
        editAge.addTextChangedListener(profileTextWatcher);
        editToy.addTextChangedListener(profileTextWatcher);
        editFoodPreferences.addTextChangedListener(profileTextWatcher);
        editAllergies.addTextChangedListener(profileTextWatcher);
        editSpecialNotes.addTextChangedListener(profileTextWatcher);

    }

    private void setFieldsEnabled(boolean enabled) {
        editPetName.setEnabled(enabled);
        editBreed.setEnabled(enabled);
        editAge.setEnabled(enabled);
        editToy.setEnabled(enabled);
        editFoodPreferences.setEnabled(enabled);
        editAllergies.setEnabled(enabled);
        editSpecialNotes.setEnabled(enabled);
        btnConfirm.setEnabled(enabled);
        isEditing = enabled;

        btnEdit.setText(enabled ? R.string.select_image : R.string.edit);
    }

    private void toggleEditMode() {
        if (!isEditing) {
            setFieldsEnabled(true);
        } else {
            selectProfilePicture();
        }
    }

    private void saveProfile() {
        if (!hasProfileChanged && selectedImageUri == null) {
            // No changes have been made, so don't proceed with saving
            Toast.makeText(this, "No changes to save", Toast.LENGTH_SHORT).show();
            return;
        }

        String petName = editPetName.getText().toString().trim();
        String breed = editBreed.getText().toString().trim();
        String age = editAge.getText().toString().trim();
        String toy = editToy.getText().toString().trim();
        String foodPreferences = editFoodPreferences.getText().toString().trim();
        String allergies = editAllergies.getText().toString().trim();
        String specialNotes = editSpecialNotes.getText().toString().trim();
        String profileImageUrl = selectedImageUri != null ? selectedImageUri.toString() : "";

        if (!TextUtils.isEmpty(petName) && !TextUtils.isEmpty(breed) && !TextUtils.isEmpty(age)) {
            UserProfile userProfile = new UserProfile(petName, breed, age, toy, foodPreferences, allergies, specialNotes, profileImageUrl);

            databaseReference.setValue(userProfile).addOnSuccessListener(aVoid -> {
                Toast.makeText(UserProfileActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();
                setFieldsEnabled(false);
                if (selectedImageUri != null) uploadProfilePicture(selectedImageUri);
            }).addOnFailureListener(e -> Toast.makeText(UserProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "Please fill in the required fields", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadProfilePicture(Uri imageUri) {
        StorageReference fileRef = storageReference.child("profile_images").child(getUserId() + ".jpg");

        fileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
            databaseReference.child("profileImageUri").setValue(uri.toString());
            Glide.with(UserProfileActivity.this).load(uri).into(imgProfilePicture);
            Toast.makeText(UserProfileActivity.this, "Profile picture updated", Toast.LENGTH_SHORT).show();
        })).addOnFailureListener(e -> Toast.makeText(UserProfileActivity.this, "Failed to upload profile picture", Toast.LENGTH_SHORT).show());
    }

    private void selectProfilePicture() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            imgProfilePicture.setImageURI(selectedImageUri);
            // Set flag that the profile picture has been changed
            hasProfileChanged = true;
        }
    }


    private void loadProfileData() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserProfile userProfile = dataSnapshot.getValue(UserProfile.class);
                if (userProfile != null) {
                    editPetName.setText(userProfile.getPetName());
                    editBreed.setText(userProfile.getBreed());
                    editAge.setText(userProfile.getAge());
                    editToy.setText(userProfile.getFavoriteToy());
                    editFoodPreferences.setText(userProfile.getFoodPreferences());
                    editAllergies.setText(userProfile.getAllergies());
                    editSpecialNotes.setText(userProfile.getSpecialNotes());
                    if (userProfile.getProfileImageUrl() != null) {
                        selectedImageUri = Uri.parse(userProfile.getProfileImageUrl());
                        Glide.with(UserProfileActivity.this).load(selectedImageUri).into(imgProfilePicture);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(UserProfileActivity.this, "Failed to load profile data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getUserId() {
        // Assuming FirebaseAuth is setup
        // return FirebaseAuth.getInstance().getCurrentUser().getUid();
        return "user-id"; // Placeholder, should be replaced with real user ID logic
    }
}

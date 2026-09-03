package com.example.pawcare;

public class UserProfile {
    private String petName;
    private String breed;
    private String age;
    private String favoriteToy;
    private String foodPreferences;
    private String allergies;
    private String specialNotes;
    private String profileImageUrl;

    // Default constructor required for Firebase
    public UserProfile() {
    }

    // Parameterized constructor
    public UserProfile(String petName, String breed, String age, String favoriteToy,
                       String foodPreferences, String allergies, String specialNotes,
                       String profileImageUrl) {
        this.petName = petName;
        this.breed = breed;
        this.age = age;
        this.favoriteToy = favoriteToy;
        this.foodPreferences = foodPreferences;
        this.allergies = allergies;
        this.specialNotes = specialNotes;
        this.profileImageUrl = profileImageUrl;
    }

    // Getters and setters for each field
    public String getPetName() {
        return petName;
    }

    public void setPetName(String petName) {
        this.petName = petName;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getFavoriteToy() {
        return favoriteToy;
    }

    public void setFavoriteToy(String favoriteToy) {
        this.favoriteToy = favoriteToy;
    }

    public String getFoodPreferences() {
        return foodPreferences;
    }

    public void setFoodPreferences(String foodPreferences) {
        this.foodPreferences = foodPreferences;
    }

    public String getAllergies() {
        return allergies;
    }

    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }

    public String getSpecialNotes() {
        return specialNotes;
    }

    public void setSpecialNotes(String specialNotes) {
        this.specialNotes = specialNotes;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}


package com.example.pawcare;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignInActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button signInButton;
    private TextView signUpTextView; // Changed variable name for clarity
    private TextView forgotPasswordTextView;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen); // Make sure this matches your layout file name

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Bind views to the UI elements
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.loginPasswordEditText);
        signInButton = findViewById(R.id.loginButton);
        signUpTextView = findViewById(R.id.textViewSignUp);
        forgotPasswordTextView = findViewById(R.id.forgotPasswordText);

        // Set click listeners
        signInButton.setOnClickListener(view -> attemptSignIn());
        signUpTextView.setOnClickListener(view -> navigateToSignUp());
        forgotPasswordTextView.setOnClickListener(view -> sendPasswordResetEmail());

        if (getIntent().hasExtra("email")) {
            String email = getIntent().getStringExtra("email");
            emailEditText.setText(email);
        }

    }

    private void attemptSignIn() {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        if (validateInput(email, password)) {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            navigateToMain();
                        } else {
                            displayToast("Authentication failed: " + task.getException().getMessage());
                        }
                    });
        }
    }

    private boolean validateInput(String email, String password) {
        if (email.isEmpty()) {
            displayToast("Email cannot be empty.");
            return false;
        }
        if (password.isEmpty()) {
            displayToast("Password cannot be empty.");
            return false;
        }
        return true;
    }

    private void navigateToMain() {
        Intent intent = new Intent(SignInActivity.this, HomeActivity.class);
        startActivity(intent);
        finish(); // Close the SignInActivity

    }

    private void navigateToSignUp() {
        Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
        startActivity(intent);
    }

    private void sendPasswordResetEmail() {
        String emailAddress = emailEditText.getText().toString();

        if (!emailAddress.isEmpty()) {
            mAuth.sendPasswordResetEmail(emailAddress)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            displayToast("Password reset email sent.");
                        } else {
                            displayToast("Failed to send reset email. " + task.getException().getMessage());
                        }
                    });
        } else {
            displayToast("Please enter your email address to reset the password.");
        }
    }

    private void displayToast(String message) {
        Toast.makeText(SignInActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}

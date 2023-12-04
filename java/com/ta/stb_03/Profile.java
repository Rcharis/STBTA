package com.ta.stb_03;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Profile extends AppCompatActivity {

    private TextView displayNameTextView, emailTextView;
    private Button editProfileButton, logoutButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = findViewById(R.id.toolbar); // Replace with your toolbar's ID
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Enable the Up button

        displayNameTextView = findViewById(R.id.profileName);
        emailTextView = findViewById(R.id.profileEmail);
        editProfileButton = findViewById(R.id.editProfileButton);
        logoutButton = findViewById(R.id.LogoutButton);

        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            displayNameTextView.setText(currentUser.getDisplayName());
            emailTextView.setText(currentUser.getEmail());
        }

        editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editProfileIntent = new Intent(Profile.this, EditProfileActivity.class);
                startActivity(editProfileIntent);
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                startActivity(new Intent(Profile.this, Login.class));
                finish(); // Finish the current activity
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // This will go back to the previous activity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Update UI with the latest user information
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            displayNameTextView.setText(currentUser.getDisplayName());
            emailTextView.setText(currentUser.getEmail());
        }
    }
}

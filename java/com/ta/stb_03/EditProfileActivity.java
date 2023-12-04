package com.ta.stb_03;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class EditProfileActivity extends AppCompatActivity {

    private EditText newDisplayNameEditText;
    private Button saveChangesButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mAuth = FirebaseAuth.getInstance();
        newDisplayNameEditText = findViewById(R.id.editTextNewDisplayName);
        saveChangesButton = findViewById(R.id.btnSaveChanges);

        saveChangesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newDisplayName = newDisplayNameEditText.getText().toString();

                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null) {
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(newDisplayName)
                            .build();

                    currentUser.updateProfile(profileUpdates)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        // Display a message or perform any other action on success.
                                        // For example, you can finish the activity to go back to the profile screen.
                                        finish();
                                    } else {
                                        // Handle any errors that occurred during the profile update.
                                    }
                                }
                            });
                }
            }
        });
    }
}

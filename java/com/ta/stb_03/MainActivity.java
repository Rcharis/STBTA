package com.ta.stb_03;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import com.fasterxml.jackson.core.json.DupDetector;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;

    private SecurityPopup securityPopup;
    private static final String CHANNEL_ID = "IoTNotificationsChannel"; // Notification channel ID
    private int notificationId = 0; // Initialize a unique ID for each notification
    private TextView horg;
    private TextView forg;
    private TextView hang;
    private TextView fang;
    private TextView name;
    private TextView halo;
    private TextView tempOrg;
    private TextView humidOrg;
    private TextView tempArg;
    private TextView humidArg;
    private TextView volOrg;
    private TextView volArg;
    private TextView timeTextView;
    private TextView dateTextView;
    private ImageView imgOrg, imgArg, menu, imgTempOrg, imgHumidOrg, imgTempArg, imgHumidArg;

    private Boolean horgBoolean = false;
    private Boolean forgBoolean = false;
    private Boolean hangBoolean = false;
    private Boolean fangBoolean = false;

    private FirebaseDatabase database;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fang = findViewById(R.id.fang);
        forg = findViewById(R.id.forg);
        hang = findViewById(R.id.hang);
        horg = findViewById(R.id.horg);
        halo = findViewById(R.id.halo);
        name = findViewById(R.id.name);
        volOrg = findViewById(R.id.volOrg);
        imgOrg = findViewById(R.id.imgOrg);
        tempOrg = findViewById(R.id.tempOrg);
        humidOrg = findViewById(R.id.humidOrg);
        imgTempOrg = findViewById(R.id.imgTempOrg);
        imgHumidOrg = findViewById(R.id.imgHumidOrg);
        volArg = findViewById(R.id.volArg);
        imgArg = findViewById(R.id.imgArg);
        tempArg = findViewById(R.id.tempArg);
        humidArg = findViewById(R.id.humidArg);
        imgTempArg = findViewById(R.id.imgTempArg);
        imgHumidArg = findViewById(R.id.imgHumidArg);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        timeTextView = findViewById(R.id.time);
        dateTextView = findViewById(R.id.date);
        menu = findViewById(R.id.Drawer);
        toolbar = findViewById(R.id.toolbar);

        // Inisialisasi database reference
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Panggil metode untuk mendapatkan dan menampilkan data
        fetchDataFromFirebase();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            name.setText(firebaseUser.getDisplayName());
        }

        securityPopup = new SecurityPopup(this);
        securityPopup.setOnPasswordEnteredListener(new SecurityPopup.OnPasswordEnteredListener() {
            @Override
            public void onPasswordEntered() {
                // Handle the case when the correct password is entered
                // You can put your logic here to navigate to another activity or perform other actions
                Intent intent = new Intent(MainActivity.this, Graph.class);
                startActivity(intent);
            }
        });

        final Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                updateTimeAndDate();
                handler.postDelayed(this, 1000);
            }
        });

        setSupportActionBar(toolbar);

        Menu menu = navigationView.getMenu();
        menu.findItem(R.id.nav_rate).setVisible(false);
        menu.findItem(R.id.nav_logout).setVisible(false);
        menu.findItem(R.id.nav_trash).setVisible(false);

        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        navigationView.setCheckedItem(R.id.nav_home);

        database = FirebaseDatabase.getInstance();
        DatabaseReference horgRef = database.getReference("Ketinggian_Sampah/A");
        DatabaseReference forgRef = database.getReference("Ketinggian_Sampah/B");
        DatabaseReference hangRef = database.getReference("Ketinggian_Sampah/C");
        DatabaseReference fangRef = database.getReference("Ketinggian_Sampah/D");

        databaseReference = FirebaseDatabase.getInstance().getReference();

        horgRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Assuming the value is a floating-point number
                Boolean A = dataSnapshot.getValue(Boolean.class);
                if (A != null) {
                    horg.setText(Boolean.toString(A));
                    horgBoolean = A;
                    cek(horgBoolean, forgBoolean);
                } else {
                    horg.setText("N/A");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle cancelled event or errors
            }
        });

        forgRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Assuming the value is a floating-point number
                Boolean B = dataSnapshot.getValue(Boolean.class);
                if (B != null) {
                    forg.setText(Boolean.toString(B));
                    forgBoolean = B;
                    cek(horgBoolean, forgBoolean);
                } else {
                    forg.setText("N/A");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle cancelled event or errors
            }
        });

        hangRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Assuming the value is a floating-point number
                Boolean C = dataSnapshot.getValue(Boolean.class);
                if (C != null) {
                    hang.setText(Boolean.toString(C));
                    hangBoolean = C;
                    check(hangBoolean, fangBoolean);
                } else {
                    hang.setText("N/A");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle cancelled event or errors
            }
        });

        fangRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Assuming the value is a floating-point number
                Boolean D = dataSnapshot.getValue(Boolean.class);
                if (D != null) {
                    fang.setText(Boolean.toString(D));
                    fangBoolean = D;
                    check(hangBoolean, fangBoolean);
                } else {
                    fang.setText("N/A");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle cancelled event or errors
            }
        });

        createNotificationChannel();

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser != null) {
            String displayName = firebaseUser.getDisplayName();
            String email = firebaseUser.getEmail();

            View headerView = navigationView.getHeaderView(0);
            TextView headerName = headerView.findViewById(R.id.header_name);
            TextView headerEmail = headerView.findViewById(R.id.header_email);

            headerName.setText(displayName);
            headerEmail.setText(email);
        }
    }

    private void fetchDataFromFirebase() {
        DatabaseReference kapasitasSampahRef = databaseReference.child("Kapasitas_Sampah");
        DatabaseReference suhuSampahRef = databaseReference.child("Suhu_Sampah");
        DatabaseReference kelembabanSampahRef = databaseReference.child("Kelembaban_Sampah");

        kapasitasSampahRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dateSnapshot : dataSnapshot.getChildren()) {
                    String date = dateSnapshot.getKey();

                    // Iterate through times for each date
                    for (DataSnapshot timeSnapshot : dateSnapshot.getChildren()) {
                        String time = timeSnapshot.getKey();

                        // Get Anorganik_Kap and Organik_Kap values
                        int anorganik_Kap = timeSnapshot.child("Anorganik_Kap").getValue(Integer.class);
                        int organik_Kap = timeSnapshot.child("Organik_Kap").getValue(Integer.class);

                        // Update the UI with the new values
                        updateUI1(anorganik_Kap, organik_Kap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors here
            }
        });

        suhuSampahRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dateSnapshot : dataSnapshot.getChildren()) {
                    String date = dateSnapshot.getKey();

                    // Iterate through times for each date
                    for (DataSnapshot timeSnapshot : dateSnapshot.getChildren()) {
                        String time = timeSnapshot.getKey();

                        // Get Anorganik_Kap and Organik_Kap values
                        float anorganik_Suhu = timeSnapshot.child("Anorganik_Suhu").getValue(Float.class);
                        float organik_Suhu = timeSnapshot.child("Organik_Suhu").getValue(Float.class);

                        // Update the UI with the new values
                        updateUI2(anorganik_Suhu, organik_Suhu);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FirebaseData", "Error fetching data", databaseError.toException());
            }
        });

        kelembabanSampahRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dateSnapshot : dataSnapshot.getChildren()) {
                    String date = dateSnapshot.getKey();

                    // Iterate through times for each date
                    for (DataSnapshot timeSnapshot : dateSnapshot.getChildren()) {
                        String time = timeSnapshot.getKey();

                        // Get Anorganik_Kap and Organik_Kap values
                        float anorganik_Kelemb = timeSnapshot.child("Anorganik_Kelemb").getValue(Float.class);
                        float organik_Kelemb = timeSnapshot.child("Organik_Kelemb").getValue(Float.class);

                        // Update the UI with the new values
                        updateUI3(anorganik_Kelemb, organik_Kelemb);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FirebaseData", "Error fetching data", databaseError.toException());
            }
        });
    }

    private void updateUI1(int anorganik_Kap, int organik_Kap) {
        double anorganikInL = anorganik_Kap / 1000.0;
        double organikInL = organik_Kap / 1000.0;

        volArg.setText(": " + anorganikInL + " L");
        volOrg.setText(": " + organikInL + " L");
    }

    private void updateUI2(float anorganik_Suhu, float organik_Suhu) {
        tempArg.setText(": " + anorganik_Suhu + " °C");
        tempOrg.setText(": " + organik_Suhu + " °C");
    }

    private void updateUI3(float anorganik_Kelemb, float organik_Kelemb) {
        humidArg.setText(": " + anorganik_Kelemb + " %");
        humidOrg.setText(": " + organik_Kelemb + " %");
    }

    private void updateTimeAndDate() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

        String currentTime = timeFormat.format(new Date());
        String currentDate = dateFormat.format(new Date());

        timeTextView.setText(currentTime);
        dateTextView.setText(currentDate);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "IoT Notifications";
            String description = "Receive notifications for IoT events";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void showNotification(String title, String content) {
        // Create an explicit intent that opens your app's main activity
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with your notification icon
                .setContentTitle(title)
                .setContentText(content)
                .setContentIntent(pendingIntent) // Set the PendingIntent
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true); // Auto dismiss the notification when clicked

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(notificationId, builder.build());

        // Increment the notification ID to ensure the next notification is unique
        notificationId++;
    }

    private void cek(Boolean horg, Boolean forg) {
        if (horg && forg) {
            imgOrg.setImageResource(R.drawable.forg); // true + true
            showNotification("Smart Trash Bank", "Your Organic Bank is Full Now");
        } else if (horg || forg) {
            imgOrg.setImageResource(R.drawable.horg); // true + false
        } else {
            imgOrg.setImageResource(R.drawable.norg);// false false
        }
    }

    private void check(Boolean hang, Boolean fang) {
        if (hang && fang) {
            imgArg.setImageResource(R.drawable.farg); // true + true
            showNotification("Smart Trash Bank", "Your Inorganic Bank is Full Now");
        } else if (hang || fang) {
            imgArg.setImageResource(R.drawable.harg); // true + false
        } else {
            imgArg.setImageResource(R.drawable.narg);// false false
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (securityPopup.isDialogOpen()) {
            securityPopup.dismiss(); // Close the dialog
        } else {
            super.onBackPressed(); // Proceed with the default back action
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int itemId = menuItem.getItemId();

        if (itemId == R.id.nav_home) {
            // Handle the home navigation
        } else if (itemId == R.id.nav_profile) {
            Intent intent = new Intent(MainActivity.this, Profile.class);
            startActivity(intent);
        } else if (itemId == R.id.nav_chart) {
            // Tampilkan popup keamanan sebelum beralih ke GraphActivity
            onDrawerItemClicked();
        } else if (itemId == R.id.nav_trash) {
            Intent intent = new Intent(MainActivity.this, Graph.class);
            startActivity(intent);
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void onDrawerItemClicked() {
        // Tampilkan popup keamanan untuk memasukkan kata sandi
        securityPopup.show();
    }

}
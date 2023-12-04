package com.ta.stb_03;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DailyFragment extends Fragment {

    private ImageView selectDateButton, refreshButton;
    private LineChart lineChart1;
    private LineChart lineChart2;
    private EditText selectedDateText;
    private FirebaseDatabase database;
    private DatabaseReference AnorganikRef;
    private DatabaseReference OrganikRef;
    private LineDataSet lineDataSet;
    private LineData lineData;
    private Calendar selectedDate = Calendar.getInstance();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private Runnable dataReloadRunnable;

    public DailyFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_daily, container, false);

        selectedDateText = view.findViewById(R.id.selectedDateText);
        selectDateButton = view.findViewById(R.id.imageCalendarButton);
        refreshButton = view.findViewById(R.id.imageReloadButton);
        lineChart1 = view.findViewById(R.id.lineChart1);
        lineChart2 = view.findViewById(R.id.lineChart2);

        database = FirebaseDatabase.getInstance();
        AnorganikRef = database.getReference("Kapasitas_Sampah");
        OrganikRef = database.getReference("Kapasitas_Sampah");

        lineDataSet = new LineDataSet(new ArrayList<>(), "Anorganik_Kap");
        lineDataSet = new LineDataSet(new ArrayList<>(), "Organik_Kap");
        lineDataSet.setLineWidth(3f);
        lineDataSet.setColor(Color.BLACK);
        lineData = new LineData(lineDataSet);

        // Set listener for "Select Date" button
        selectDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }
        });

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Gantilah argumen ini sesuai dengan kebutuhan Anda
                retrieveData();
            }
        });

        // Schedule data reload every 1 minute
        dataReloadRunnable = new Runnable() {
            @Override
            public void run() {
                retrieveData();
                mainHandler.postDelayed(this, 1 * 60 * 1000); // 1 minute in milliseconds
            }
        };
        mainHandler.postDelayed(dataReloadRunnable, 0); // Start the data reload runnable

        // Initialize and configure your line charts here

        return view;
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                        selectedDate.set(year, month, dayOfMonth);
                        SimpleDateFormat dateFormatWithDash = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        SimpleDateFormat dateFormatWithoutDash = new SimpleDateFormat("yyyy-MM-d", Locale.getDefault());
                        String selectedDateStr;

                        // Check if dayOfMonth has a leading zero or not
                        if (dayOfMonth < 10) {
                            selectedDateStr = dateFormatWithoutDash.format(selectedDate.getTime());
                        } else {
                            selectedDateStr = dateFormatWithDash.format(selectedDate.getTime());
                        }

                        selectedDateText.setText(selectedDateStr);
                        Toast.makeText(getContext(), "Selected Date: " + selectedDateStr, Toast.LENGTH_SHORT).show();
                        retrieveData(); // Reload data when date is selected
                    }
                },
                year, month, dayOfMonth
        );

        datePickerDialog.show();
    }

    private void retrieveData() {
        SimpleDateFormat dateFormatWithDash = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat dateFormatWithoutDash = new SimpleDateFormat("yyyy-MM-d", Locale.getDefault());

        String selectedDateStr = "";

        // Check if dayOfMonth has a leading zero or not
        if (selectedDate.get(Calendar.DAY_OF_MONTH) < 10) {
            selectedDateStr = dateFormatWithoutDash.format(selectedDate.getTime());
        } else {
            selectedDateStr = dateFormatWithDash.format(selectedDate.getTime());
        }

        DatabaseReference AnselectedDateRef = AnorganikRef.child(selectedDateStr);
        DatabaseReference OselectedDateRef = OrganikRef.child(selectedDateStr);

        AnselectedDateRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Entry> dataVals1 = new ArrayList<>();
                List<Long> hourlySum = new ArrayList<>();
                List<Integer> hourlyCount = new ArrayList<>();

                for (DataSnapshot timeSnapshot : dataSnapshot.getChildren()) {
                    long anorganik_Kap = getValueOrDefault(timeSnapshot.child("Anorganik_Kap"), 0L);
                    String time = timeSnapshot.getKey();

                    // Convert time to hours for x-axis
                    int hour = convertTimeToHour(time);

                    // Ensure that the lists have the required size
                    while (hourlySum.size() <= hour) {
                        hourlySum.add(0L);
                        hourlyCount.add(0);
                    }

                    // Update the sum and count for the corresponding hour
                    hourlySum.set(hour, hourlySum.get(hour) + anorganik_Kap);
                    hourlyCount.set(hour, hourlyCount.get(hour) + 1);
                }

                // Calculate average values for each hour in cm cubic
                for (int hour = 0; hour < hourlySum.size(); hour++) {
                    long sum = hourlySum.get(hour);
                    int count = hourlyCount.get(hour);

                    // Avoid division by zero in Liter
                    if (count > 0) {
                        float average = (sum / count) / 1000;
                        dataVals1.add(new Entry(hour, average));
                    }
                }

                showChart1((ArrayList<Entry>) dataVals1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database error
                Toast.makeText(getContext(), "Failed to retrieve data.", Toast.LENGTH_SHORT).show();
            }
        });

        OselectedDateRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Entry> dataVals2 = new ArrayList<>();
                List<Long> hourlySum = new ArrayList<>();
                List<Integer> hourlyCount = new ArrayList<>();

                for (DataSnapshot timeSnapshot : dataSnapshot.getChildren()) {
                    long organik_Kap = getValueOrDefault(timeSnapshot.child("Organik_Kap"), 0L);
                    String time = timeSnapshot.getKey();

                    // Convert time to hours for x-axis
                    int hour = convertTimeToHour(time);

                    // Ensure that the lists have the required size
                    while (hourlySum.size() <= hour) {
                        hourlySum.add(0L);
                        hourlyCount.add(0);
                    }

                    // Update the sum and count for the corresponding hour
                    hourlySum.set(hour, hourlySum.get(hour) + organik_Kap);
                    hourlyCount.set(hour, hourlyCount.get(hour) + 1);
                }

                // Calculate average values for each hour in cm cubic
                for (int hour = 0; hour < hourlySum.size(); hour++) {
                    long sum = hourlySum.get(hour);
                    int count = hourlyCount.get(hour);

                    // Avoid division by zero in Liter
                    if (count > 0) {
                        float average = (sum / count) / 1000;
                        dataVals2.add(new Entry(hour, average));
                    }
                }

                showChart2((ArrayList<Entry>) dataVals2);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database error
                Toast.makeText(getContext(), "Failed to retrieve data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private long getValueOrDefault(DataSnapshot dataSnapshot, long defaultValue) {
        Long value = dataSnapshot.getValue(Long.class);
        return (value != null) ? value : defaultValue;
    }

    private void showChart1(ArrayList<Entry> dataVals1) {
        LineDataSet lineDataSet = new LineDataSet(dataVals1, "Volume Anorganik");
        lineDataSet.setLineWidth(3f);
        lineDataSet.setColor(Color.BLACK);

        LineData lineData = new LineData(lineDataSet);

        lineChart1.setData(lineData);
        lineChart1.getAxisRight().setEnabled(false);
        lineChart1.setDragEnabled(true);
        lineChart1.setScaleEnabled(true);
        lineChart1.setMinOffset(0f);
        XAxis xAxis = lineChart1.getXAxis();
        xAxis.setGranularity(1f); // Jarak antar titik
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new HourAxisValueFormatter());
        xAxis.setLabelRotationAngle(-45f);

        lineChart1.notifyDataSetChanged();
        lineChart1.invalidate();
    }

    private void showChart2(ArrayList<Entry> dataVals2) {
        LineDataSet lineDataSet = new LineDataSet(dataVals2, "Volume Organiki");
        lineDataSet.setLineWidth(3f);
        lineDataSet.setColor(Color.BLACK);

        LineData lineData = new LineData(lineDataSet);

        lineChart2.setData(lineData);
        lineChart2.getAxisRight().setEnabled(false);
        lineChart2.setDragEnabled(true);
        lineChart2.setScaleEnabled(true);
        lineChart2.setMinOffset(0f);
        XAxis xAxis = lineChart2.getXAxis();
        xAxis.setGranularity(1f); // Jarak antar titik
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new HourAxisValueFormatter());
        xAxis.setLabelRotationAngle(-45f);

        lineChart2.notifyDataSetChanged();
        lineChart2.invalidate();
    }

    private class HourAxisValueFormatter extends ValueFormatter {
        @Override
        public String getAxisLabel(float value, AxisBase axis) {
            // Convert hour value to hour label
            int hourOfDay = (int) value;
            return String.format("%02d:00 - %02d:59", hourOfDay, hourOfDay);
        }
    }

    private int convertTimeToHour(String time) {
        try {
            if (time != null && !time.isEmpty()) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                Date date = sdf.parse(time);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                return calendar.get(Calendar.HOUR_OF_DAY);
            } else {
                // Tindakan sesuai kebutuhan Anda saat string waktu null atau kosong
                return 0;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            // Tindakan sesuai kebutuhan Anda saat ParseException
            return 0;
        }
    }
}

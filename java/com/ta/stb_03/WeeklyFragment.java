package com.ta.stb_03;

import android.os.Bundle;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.formatter.DefaultValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.datepicker.RangeDateSelector;
import com.google.android.material.datepicker.SingleDateSelector;
import com.google.android.material.textfield.TextInputEditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class WeeklyFragment extends Fragment {

    private Button weeklySelectDateButton, reloadButton;
    private TextInputEditText minDateEditText;
    private TextInputEditText maxDateEditText;
    private LineChart weeklyLineChart1;
    private LineChart weeklyLineChart2;
    private LineChart weeklyLineChart3;
    private FirebaseDatabase database;
    private DatabaseReference volRef;
    private DatabaseReference tempRef;
    private DatabaseReference humidRef;
    private LineDataSet lineDataSet;
    private LineData lineData;
    private MaterialDatePicker<Pair<Long, Long>> dateRangePicker;
    private RangeDateSelector rangeDateSelector;
    private String selectedStartDate; // Menyimpan tanggal awal yang dipilih
    private String selectedEndDate;   // Menyimpan tanggal akhir yang dipilih

    public WeeklyFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_weekly, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        weeklySelectDateButton = view.findViewById(R.id.weeklySelectDateRangeButton);
        reloadButton = view.findViewById(R.id.weeklyReloadDataButton);
        minDateEditText = view.findViewById(R.id.minDateEditText);
        maxDateEditText = view.findViewById(R.id.maxDateEditText);
        weeklyLineChart1 = view.findViewById(R.id.weeklyLineChart1);
        weeklyLineChart2 = view.findViewById(R.id.weeklyLineChart2);
        weeklyLineChart3 = view.findViewById(R.id.weeklyLineChart3);

        database = FirebaseDatabase.getInstance();
        volRef = database.getReference("Kapasitas_Sampah");
        tempRef = database.getReference("Suhu_Sampah");
        humidRef = database.getReference("Kelembaban_Sampah");

        lineDataSet = new LineDataSet(new ArrayList<>(), "Organik Weekly");
        lineDataSet.setLineWidth(3f);
        lineDataSet.setColor(Color.BLACK);
        lineData = new LineData(lineDataSet);

        // Set click listener for date selection button
        weeklySelectDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateRangePickerDialog();
            }
        });

        reloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Gantilah argumen ini sesuai dengan kebutuhan Anda
                loadChartDataForDateRange(selectedStartDate, selectedEndDate);
            }
        });


        // Initialize date range picker
        rangeDateSelector = new RangeDateSelector();
        dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select Date Range")
                .setSelection(rangeDateSelector.getSelection())
                .build();

        dateRangePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Pair<Long, Long>>() {
            @Override
            public void onPositiveButtonClick(Pair<Long, Long> selection) {
                long startDate = selection.first;
                long endDate = selection.second;

                // Parse selectedStartDate and selectedEndDate into Calendar objects
                Calendar calSelectedStartDate = Calendar.getInstance();
                Calendar calSelectedEndDate = Calendar.getInstance();

                calSelectedStartDate.setTimeInMillis(startDate);
                calSelectedEndDate.setTimeInMillis(endDate);

                // Determine the correct date format based on the length of the day part
                SimpleDateFormat dateFormatWithDash = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat dateFormatWithoutDash = new SimpleDateFormat("yyyy-MM-d", Locale.getDefault());

                String selectedStartDateStr = "";
                String selectedEndDateStr = "";

                // Format selected start date
                if (calSelectedStartDate.get(Calendar.DAY_OF_MONTH) < 10) {
                    selectedStartDateStr = dateFormatWithoutDash.format(startDate);
                } else {
                    selectedStartDateStr = dateFormatWithDash.format(startDate);
                }

                // Format selected end date
                if (calSelectedEndDate.get(Calendar.DAY_OF_MONTH) < 10) {
                    selectedEndDateStr = dateFormatWithoutDash.format(endDate);
                } else {
                    selectedEndDateStr = dateFormatWithDash.format(endDate);
                }

                minDateEditText.setText(selectedStartDateStr);
                maxDateEditText.setText(selectedEndDateStr);

                // Load chart data for the selected date range
                loadChartDataForDateRange(selectedStartDateStr, selectedEndDateStr);
                loadChartDataForDateRange1(selectedStartDateStr, selectedEndDateStr);
                loadChartDataForDateRange2(selectedStartDateStr, selectedEndDateStr);
            }
        });


    }

    private void showDateRangePickerDialog() {
        dateRangePicker.show(getParentFragmentManager(), dateRangePicker.toString());
    }

    private void loadChartDataForDateRange(String startDate, String endDate) {
        volRef.orderByKey().startAt(startDate).endAt(endDate).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Iterasi melalui data dari database
                ArrayList<Entry> dataVals1 = new ArrayList<>();
                ArrayList<Entry> dataVals2 = new ArrayList<>();
                SimpleDateFormat dateFormatWithDash = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat dateFormatWithoutDash = new SimpleDateFormat("yyyy-MM-d", Locale.getDefault());

                for (DataSnapshot dateSnapshot : snapshot.getChildren()) {
                    String date = dateSnapshot.getKey();

                    // Pastikan tanggal berada dalam rentang yang diinginkan
                    if (isDateInRange(date, startDate, endDate)) {
                        int totalAnorganikKap = 0;
                        int totalOrganikKap = 0;
                        int totalEntriesAnorganik1 = 0;
                        int totalEntriesOrganik1 = 0;

                        for (DataSnapshot timeSnapshot : dateSnapshot.getChildren()) {
                            DataPoint dataPoint = timeSnapshot.getValue(DataPoint.class);
                            totalAnorganikKap += dataPoint.getAnorganik_Kap();
                            totalOrganikKap += dataPoint.getOrganik_Kap();
                            totalEntriesAnorganik1++;
                            totalEntriesOrganik1++;
                        }

                        float averageAnorganikKap = totalEntriesAnorganik1 > 0 ? totalAnorganikKap / totalEntriesAnorganik1 : 0;
                        float averageOrganikKap = totalEntriesOrganik1 > 0 ? totalOrganikKap / totalEntriesOrganik1 : 0;

                        float sumAnorganikKap = averageAnorganikKap / 1000;
                        float sumOrganikKap = averageOrganikKap / 1000;

                        try {
                            Date dateObj;
                            if (date.contains("-")) {
                                dateObj = dateFormatWithDash.parse(date);
                            } else {
                                dateObj = dateFormatWithoutDash.parse(date);
                            }

                            long millis = dateObj.getTime();

                            dataVals1.add(new Entry(millis, sumAnorganikKap));
                            dataVals2.add(new Entry(millis, sumOrganikKap));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }

                showChart1(dataVals1, dataVals2);

                XAxis xAxis = weeklyLineChart1.getXAxis();
                xAxis.setAxisMinimum(dataVals1.get(0).getX());
                xAxis.setAxisMaximum(dataVals1.get(dataVals1.size() - 1).getX());

                xAxis.setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getFormattedValue(float value) {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        return dateFormat.format(new Date((long) value));
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database error
            }
        });
    }

    private void loadChartDataForDateRange1(String startDate, String endDate) {
        tempRef.orderByKey().startAt(startDate).endAt(endDate).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Iterasi melalui data dari database
                ArrayList<Entry> dataVals3 = new ArrayList<>();
                ArrayList<Entry> dataVals4 = new ArrayList<>();
                SimpleDateFormat dateFormatWithDash = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat dateFormatWithoutDash = new SimpleDateFormat("yyyy-MM-d", Locale.getDefault());

                for (DataSnapshot dateSnapshot : snapshot.getChildren()) {
                    String date = dateSnapshot.getKey();

                    // Pastikan tanggal berada dalam rentang yang diinginkan
                    if (isDateInRange(date, startDate, endDate)) {
                        float totalAnorganikSuhu = 0;
                        float totalOrganikSuhu = 0;
                        float totalEntriesAnorganik2 = 0;
                        float totalEntriesOrganik2 = 0;

                        for (DataSnapshot timeSnapshot : dateSnapshot.getChildren()) {
                            DataPoint dataPoint = timeSnapshot.getValue(DataPoint.class);
                            totalAnorganikSuhu += dataPoint.getAnorganik_Suhu();
                            totalOrganikSuhu += dataPoint.getOrganik_Suhu();
                            totalEntriesAnorganik2++;
                            totalEntriesOrganik2++;
                        }

                        float averageAnorganikSuhu = totalEntriesAnorganik2 > 0 ? totalAnorganikSuhu / totalEntriesAnorganik2 : 0;
                        float averageOrganikSuhu = totalEntriesOrganik2 > 0 ? totalOrganikSuhu / totalEntriesOrganik2 : 0;

                        try {
                            Date dateObj;
                            if (date.contains("-")) {
                                dateObj = dateFormatWithDash.parse(date);
                            } else {
                                dateObj = dateFormatWithoutDash.parse(date);
                            }

                            long millis = dateObj.getTime();

                            dataVals3.add(new Entry(millis, averageAnorganikSuhu));
                            dataVals4.add(new Entry(millis, averageOrganikSuhu));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }

                showChart2(dataVals3, dataVals4);

                XAxis xAxis = weeklyLineChart2.getXAxis();
                xAxis.setAxisMinimum(dataVals3.get(0).getX());
                xAxis.setAxisMaximum(dataVals3.get(dataVals3.size() - 1).getX());

                xAxis.setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getFormattedValue(float value) {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        return dateFormat.format(new Date((long) value));
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database error
            }
        });
    }

    private void loadChartDataForDateRange2(String startDate, String endDate) {
        humidRef.orderByKey().startAt(startDate).endAt(endDate).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Iterasi melalui data dari database
                ArrayList<Entry> dataVals5 = new ArrayList<>();
                ArrayList<Entry> dataVals6 = new ArrayList<>();
                SimpleDateFormat dateFormatWithDash = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat dateFormatWithoutDash = new SimpleDateFormat("yyyy-MM-d", Locale.getDefault());

                for (DataSnapshot dateSnapshot : snapshot.getChildren()) {
                    String date = dateSnapshot.getKey();

                    // Pastikan tanggal berada dalam rentang yang diinginkan
                    if (isDateInRange(date, startDate, endDate)) {
                        float totalAnorganikKel = 0;
                        float totalOrganikKel = 0;
                        float totalEntriesAnorganik3 = 0;
                        float totalEntriesOrganik3 = 0;

                        for (DataSnapshot timeSnapshot : dateSnapshot.getChildren()) {
                            DataPoint dataPoint = timeSnapshot.getValue(DataPoint.class);
                            totalAnorganikKel += dataPoint.getAnorganik_Kelemb();
                            totalOrganikKel += dataPoint.getOrganik_Kelemb();
                            totalEntriesAnorganik3++;
                            totalEntriesOrganik3++;
                        }

                        float averageAnorganikKel = totalEntriesAnorganik3 > 0 ? totalAnorganikKel / totalEntriesAnorganik3 : 0;
                        float averageOrganikKel = totalEntriesOrganik3 > 0 ? totalOrganikKel / totalEntriesOrganik3 : 0;

                        try {
                            Date dateObj;
                            if (date.contains("-")) {
                                dateObj = dateFormatWithDash.parse(date);
                            } else {
                                dateObj = dateFormatWithoutDash.parse(date);
                            }

                            long millis = dateObj.getTime();

                            dataVals5.add(new Entry(millis, averageAnorganikKel));
                            dataVals6.add(new Entry(millis, averageOrganikKel));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }

                showChart3(dataVals5, dataVals6);

                XAxis xAxis = weeklyLineChart3.getXAxis();
                xAxis.setAxisMinimum(dataVals5.get(0).getX());
                xAxis.setAxisMaximum(dataVals5.get(dataVals5.size() - 1).getX());

                xAxis.setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getFormattedValue(float value) {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        return dateFormat.format(new Date((long) value));
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database error
            }
        });
    }

    private void showChart1(ArrayList<Entry> dataVals1, ArrayList<Entry> dataVals2) {
        LineDataSet lineDataSet1 = new LineDataSet(dataVals1, "Volume Anorganik");
        LineDataSet lineDataSet2 = new LineDataSet(dataVals2, "Volume Organik");
        lineDataSet1.setLineWidth(3f);
        lineDataSet2.setLineWidth(3f);

        // Set warna untuk garis Anorganik_Kap
        lineDataSet1.setColor(Color.RED);
        lineDataSet1.setLineWidth(3f);
        lineDataSet1.setMode(LineDataSet.Mode.LINEAR);

        // Set warna untuk garis Organik_Kap
        lineDataSet2.setColor(Color.BLUE);
        lineDataSet2.setLineWidth(3f);
        lineDataSet2.setMode(LineDataSet.Mode.LINEAR);

        LineData lineData = new LineData(lineDataSet1, lineDataSet2);

        weeklyLineChart1.setData(lineData);

        // Set Y-axis range with padding
        YAxis yAxis = weeklyLineChart1.getAxisLeft();
        yAxis.setAxisMinimum(0f); // Set this value based on your preference
        yAxis.setAxisMaximum(Math.max(getMaximumYValue(dataVals1), getMaximumYValue(dataVals2)) + 10f); // Set additional padding as needed


        weeklyLineChart1.getAxisRight().setEnabled(false);
        weeklyLineChart1.getDescription().setEnabled(false);
        weeklyLineChart1.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        weeklyLineChart1.getXAxis().setLabelRotationAngle(0f);
        weeklyLineChart1.getXAxis().setGranularity(1f);
        weeklyLineChart1.setTouchEnabled(true);
        weeklyLineChart1.setPinchZoom(true);

        // Format X-axis as date
        weeklyLineChart1.getXAxis().setValueFormatter(new DateAxisValueFormatter());
        // Konfigurasi sumbu x
        XAxis xAxis = weeklyLineChart1.getXAxis();
        xAxis.setAxisMinimum(1f);
        xAxis.setAxisMaximum(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                // Konversi nilai x (millis) ke format tanggal
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                return dateFormat.format(new Date((long) value));
            }
        });

        weeklyLineChart1.invalidate();
    }


    private void showChart2(ArrayList<Entry> dataVals3, ArrayList<Entry> dataVals4) {
        LineDataSet lineDataSet1 = new LineDataSet(dataVals3, "Suhu Anorganik");
        LineDataSet lineDataSet2 = new LineDataSet(dataVals4, "Suhu Organik");
        lineDataSet1.setLineWidth(3f);
        lineDataSet2.setLineWidth(3f);

        // Set warna untuk garis Anorganik_Suhu
        lineDataSet1.setColor(Color.RED);
        lineDataSet1.setLineWidth(3f);
        lineDataSet1.setMode(LineDataSet.Mode.LINEAR);

        // Set warna untuk garis Organik_Suhu
        lineDataSet2.setColor(Color.BLUE);
        lineDataSet2.setLineWidth(3f);
        lineDataSet2.setMode(LineDataSet.Mode.LINEAR);

        LineData lineData = new LineData(lineDataSet1, lineDataSet2);

        weeklyLineChart2.setData(lineData);

        // Set Y-axis range with padding
        YAxis yAxis = weeklyLineChart2.getAxisLeft();
        yAxis.setAxisMinimum(0f); // Set this value based on your preference
        yAxis.setAxisMaximum(Math.max(getMaximumYValue(dataVals3), getMaximumYValue(dataVals4)) + 10f); // Set additional padding as needed

        weeklyLineChart2.getAxisRight().setEnabled(false);
        weeklyLineChart2.getDescription().setEnabled(false);
        weeklyLineChart2.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        weeklyLineChart2.getXAxis().setLabelRotationAngle(0f);
        weeklyLineChart2.getXAxis().setGranularity(1f);
        weeklyLineChart2.setTouchEnabled(true);
        weeklyLineChart2.setPinchZoom(true);

        // Format X-axis as date
        weeklyLineChart2.getXAxis().setValueFormatter(new DateAxisValueFormatter());
        // Konfigurasi sumbu x
        XAxis xAxis = weeklyLineChart2.getXAxis();
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                // Konversi nilai x (millis) ke format tanggal
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                return dateFormat.format(new Date((long) value));
            }
        });

        weeklyLineChart2.invalidate();
    }

    private void showChart3(ArrayList<Entry> dataVals5, ArrayList<Entry> dataVals6) {
        LineDataSet lineDataSet1 = new LineDataSet(dataVals5, "Humid Anorganik");
        LineDataSet lineDataSet2 = new LineDataSet(dataVals6, "Humid Organik");
        lineDataSet1.setLineWidth(3f);
        lineDataSet2.setLineWidth(3f);

        // Set warna untuk garis Anorganik_Suhu
        lineDataSet1.setColor(Color.RED);
        lineDataSet1.setLineWidth(3f);
        lineDataSet1.setMode(LineDataSet.Mode.LINEAR);

        // Set warna untuk garis Organik_Suhu
        lineDataSet2.setColor(Color.BLUE);
        lineDataSet2.setLineWidth(3f);
        lineDataSet2.setMode(LineDataSet.Mode.LINEAR);

        LineData lineData = new LineData(lineDataSet1, lineDataSet2);

        weeklyLineChart3.setData(lineData);

        // Set Y-axis range with padding
        YAxis yAxis = weeklyLineChart3.getAxisLeft();
        yAxis.setAxisMinimum(0f); // Set this value based on your preference
        yAxis.setAxisMaximum(Math.max(getMaximumYValue(dataVals5), getMaximumYValue(dataVals6)) + 10f); // Set additional padding as needed

        weeklyLineChart3.getAxisRight().setEnabled(false);
        weeklyLineChart3.getDescription().setEnabled(false);
        weeklyLineChart3.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        weeklyLineChart3.getXAxis().setLabelRotationAngle(0f);
        weeklyLineChart3.getXAxis().setGranularity(1f);
        weeklyLineChart3.setTouchEnabled(true);
        weeklyLineChart3.setPinchZoom(true);

        // Format X-axis as date
        weeklyLineChart3.getXAxis().setValueFormatter(new DateAxisValueFormatter());
        // Konfigurasi sumbu x
        XAxis xAxis = weeklyLineChart3.getXAxis();
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                // Konversi nilai x (millis) ke format tanggal
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                return dateFormat.format(new Date((long) value));
            }
        });

        weeklyLineChart3.invalidate();
    }

    private long convertDateToMillis(String date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date dateObject = dateFormat.parse(date);
            return dateObject.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    // Formatter untuk sumbu-X yang mengonversi timestamp kembali ke tanggal
    public class DateAxisValueFormatter extends ValueFormatter {
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        @Override
        public String getAxisLabel(float value, AxisBase axis) {
            return dateFormat.format(new Date((long) value));
        }
    }

    private boolean isDateInRange(String date, String startDate, String endDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        try {
            Date dateObj = dateFormat.parse(date);
            Date startDateObj = dateFormat.parse(startDate);
            Date endDateObj = dateFormat.parse(endDate);

            return dateObj.compareTo(startDateObj) >= 0 && dateObj.compareTo(endDateObj) <= 0;
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    private float getMaximumYValue(ArrayList<Entry> data) {
        float max = Float.MIN_VALUE;
        for (Entry entry : data) {
            if (entry.getY() > max) {
                max = entry.getY();
            }
        }
        return max;
    }
}


package com.thuhang.hethongnuoica;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChartActivity extends AppCompatActivity {

    private LineChart lineChart;
    private DatabaseReference databaseReference;

    private final List<Entry> tdsEntries = new ArrayList<>();
    private final List<Long> timestamps = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        lineChart = findViewById(R.id.lineChart);
        setupChartAppearance();

        databaseReference = FirebaseDatabase.getInstance().getReference("/tds_logs");

        // Đọc toàn bộ log TDS
        databaseReference.orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                tdsEntries.clear();
                timestamps.clear();

                for (DataSnapshot child : snapshot.getChildren()) {
                    try {
                        long timestamp = Long.parseLong(child.getKey());
                        Float tds = child.getValue(Float.class);

                        if (tds != null) {
                            float timeInHours = timestamp / 3600f;
                            tdsEntries.add(new Entry(timeInHours, tds));
                            timestamps.add(timestamp);
                            Log.d("FirebaseTDS", "TDS log: " + timestamp + " -> " + tds);
                        }
                    } catch (Exception e) {
                        Log.e("ParseError", "Lỗi khi đọc TDS log: " + e.getMessage());
                    }
                }

                if (!tdsEntries.isEmpty()) {
                    Collections.sort(tdsEntries, (a, b) -> Float.compare(a.getX(), b.getX()));
                    updateChart();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("FirebaseError", "Không đọc được TDS log", error.toException());
            }
        });
    }

    private void updateChart() {
        LineDataSet dataSet = new LineDataSet(tdsEntries, "Mức TDS (ppm)");
        dataSet.setColor(Color.BLUE);
        dataSet.setCircleColor(Color.BLUE);
        dataSet.setLineWidth(2f);
        dataSet.setDrawCircles(true);
        dataSet.setDrawValues(true);
        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(Color.BLACK);

        LineData data = new LineData(dataSet);
        lineChart.setData(data);

        // ✅ Cấu hình scroll ngang
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(false);
        lineChart.setPinchZoom(false);
        lineChart.setVisibleXRangeMaximum(3f); // hiển thị 3 điểm tại 1 thời điểm

        // ✅ Di chuyển tới điểm mới nhất
        lineChart.moveViewToX(tdsEntries.get(tdsEntries.size() - 1).getX());

        lineChart.invalidate();
    }


    private void setupChartAppearance() {
        lineChart.getDescription().setText("Biểu đồ chất lượng nước");
        lineChart.getDescription().setTextSize(10f);
        lineChart.setExtraBottomOffset(24f);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setLabelRotationAngle(-45f);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(5);
        xAxis.setValueFormatter(new ValueFormatter() {
            private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());

            @Override
            public String getFormattedValue(float value) {
                long millis = (long) (value * 60 * 60 * 1000);
                return sdf.format(new Date(millis));
            }
        });

        YAxis yAxisLeft = lineChart.getAxisLeft();
        yAxisLeft.setAxisMinimum(0);
        yAxisLeft.setAxisMaximum(1500);
        yAxisLeft.setLabelCount(6);
        lineChart.getAxisRight().setEnabled(false);

        Legend legend = lineChart.getLegend();
        legend.setWordWrapEnabled(true);

        List<LegendEntry> customEntries = new ArrayList<>();
        customEntries.add(new LegendEntry("< 300 ppm: Nước quá trong, không thích hợp", Legend.LegendForm.SQUARE, 10f, 2f, null, Color.GRAY));
        customEntries.add(new LegendEntry("300–800 ppm: Lý tưởng", Legend.LegendForm.SQUARE, 10f, 2f, null, Color.GREEN));
        customEntries.add(new LegendEntry("800–1200 ppm: Trung bình", Legend.LegendForm.SQUARE, 10f, 2f, null, Color.YELLOW));
        customEntries.add(new LegendEntry("> 1200 ppm: Nguy hiểm", Legend.LegendForm.SQUARE, 10f, 2f, null, Color.RED));
        legend.setCustom(customEntries);
    }
}

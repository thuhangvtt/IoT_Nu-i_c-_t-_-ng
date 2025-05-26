package com.thuhang.hethongnuoica;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class LogHistoryActivity extends AppCompatActivity {

    private RecyclerView logRecyclerView;
    private TextView emptyTextView;
    private LogAdapter logAdapter;
    private List<LogEntry> logList;
    private DatabaseReference logsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_history);

        logRecyclerView = findViewById(R.id.logRecyclerView);
        emptyTextView = findViewById(R.id.emptyTextView);
        logList = new ArrayList<>();
        logAdapter = new LogAdapter(logList);

        logRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        logRecyclerView.setAdapter(logAdapter);

        logsRef = FirebaseDatabase.getInstance().getReference("logs");

        // Nút chọn ngày lọc log
        findViewById(R.id.btnPickDate).setOnClickListener(v -> showDatePickerDialog());

        // Load toàn bộ log lần đầu
        logsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                logList.clear();

                if (!snapshot.exists()) {
                    emptyTextView.setText("Chưa có dữ liệu log");
                    emptyTextView.setVisibility(View.VISIBLE);
                    logAdapter.notifyDataSetChanged();
                    return;
                }

                for (DataSnapshot item : snapshot.getChildren()) {
                    if ("counter".equals(item.getKey())) continue;

                    LogEntry log = item.getValue(LogEntry.class);
                    if (log != null) {
                        logList.add(log);
                    }
                }

                Collections.reverse(logList);
                logAdapter.notifyDataSetChanged();
                emptyTextView.setVisibility(logList.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                emptyTextView.setText("Lỗi tải dữ liệu");
                emptyTextView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    String selectedDate = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year);
                    locLogTheoNgay(selectedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }

    private void locLogTheoNgay(String ngay) {
        logsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                logList.clear();

                for (DataSnapshot item : snapshot.getChildren()) {
                    if ("counter".equals(item.getKey())) continue;

                    LogEntry log = item.getValue(LogEntry.class);
                    if (log != null && log.time != null && log.time.contains(ngay)) {
                        logList.add(log);
                    }
                }

                Collections.reverse(logList);
                logAdapter.notifyDataSetChanged();
                emptyTextView.setVisibility(logList.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                emptyTextView.setText("Lỗi tải dữ liệu");
                emptyTextView.setVisibility(View.VISIBLE);
            }
        });
    }
}

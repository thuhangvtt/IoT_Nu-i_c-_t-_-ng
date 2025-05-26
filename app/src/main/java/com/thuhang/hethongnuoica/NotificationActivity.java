package com.thuhang.hethongnuoica;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.*;

import java.util.*;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView recyclerNotification;
    private List<NotificationItem> notifications = new ArrayList<>();
    private NotificationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        recyclerNotification = findViewById(R.id.recyclerNotification);
        adapter = new NotificationAdapter(notifications);
        recyclerNotification.setLayoutManager(new LinearLayoutManager(this));
        recyclerNotification.setAdapter(adapter);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("notifications");

        ref.orderByChild("timestamp").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                notifications.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    NotificationItem item = child.getValue(NotificationItem.class);
                    if (item != null) notifications.add(item);

                    // Đánh dấu đã đọc
                    child.getRef().child("seen").setValue(true);
                }
                Collections.reverse(notifications); // mới nhất lên đầu
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }
}

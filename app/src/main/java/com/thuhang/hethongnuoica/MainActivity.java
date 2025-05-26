package com.thuhang.hethongnuoica;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.*;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference controlRef, sensorRef, notifRef;

    private Button btnGuong, btnThucAn, btnChanDoan;
    private Switch switchAuto;
    private EditText inputGram;
    private FloatingActionButton btnMic, btnAccount;
    private ImageButton btnNotification, btnHome;
    private TextView statusText, badgeText;

    private ViewPager2 viewPagerSensor;
    private SensorPagerAdapter sensorAdapter;
    private List<SensorItem> sensorList;
    private Handler autoScrollHandler;

    private SpeechRecognizer speechRecognizer;
    private boolean guongDangChay = false;
    private boolean thucAnDangChay = false;

    private static final String CHANNEL_ID = "aquarium_alerts";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);
        createNotificationChannel();
        requestNotificationPermission();

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        controlRef = FirebaseDatabase.getInstance().getReference("aquarium/control");
        sensorRef = FirebaseDatabase.getInstance().getReference("aquarium");
        notifRef = FirebaseDatabase.getInstance().getReference("notifications");

        // Mapping View
        viewPagerSensor = findViewById(R.id.viewPagerSensor);
        statusText = findViewById(R.id.statusText);
        btnGuong = findViewById(R.id.btnGuong);
        btnThucAn = findViewById(R.id.btnThucAn);
        btnChanDoan = findViewById(R.id.btnChanDoan);
        switchAuto = findViewById(R.id.switchAuto);
        inputGram = findViewById(R.id.inputGram);
        btnMic = findViewById(R.id.btnMic);
        btnAccount = findViewById(R.id.btnAccount);
        btnNotification = findViewById(R.id.btnNotification);
        badgeText = findViewById(R.id.badgeText);
        btnHome = findViewById(R.id.btnHome);

        // Init Sensor List
        sensorList = new ArrayList<>(Arrays.asList(
                new SensorItem("Nhiệt độ", "...", R.raw.sun),
                new SensorItem("TDS", "...", R.raw.tds),
                new SensorItem("Độ đục", "...", R.raw.cannang),
                new SensorItem("Khối lượng", "...", R.raw.ca)
        ));
        sensorAdapter = new SensorPagerAdapter(sensorList);
        viewPagerSensor.setAdapter(sensorAdapter);

        autoScrollViewPager();

        setupControls();
        docDuLieuCamBien();
        capNhatTrangThaiChoAn();
        setupSpeechRecognition();
        demThongBaoChuaDoc();
        cleanupOldNotifications();
        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        btnAccount.setOnClickListener(v -> startActivity(new Intent(this, UserActivity.class)));
        btnChanDoan.setOnClickListener(v -> startActivity(new Intent(this, DiagnosisActivity.class)));
        btnNotification.setOnClickListener(v -> startActivity(new Intent(this, NotificationActivity.class)));
    }

    private void autoScrollViewPager() {
        autoScrollHandler = new Handler();
        Runnable runnable = new Runnable() {
            int currentPage = 0;
            @Override
            public void run() {
                currentPage = (currentPage + 1) % sensorList.size();
                viewPagerSensor.setCurrentItem(currentPage, true);
                autoScrollHandler.postDelayed(this, 5000);
            }
        };
        autoScrollHandler.postDelayed(runnable, 5000);
    }

    private void setupControls() {
        btnGuong.setOnClickListener(v -> {
            guongDangChay = !guongDangChay;
            controlRef.child("guong").setValue(guongDangChay);
            btnGuong.setText(guongDangChay ? "Tắt Guồng" : "Bật Guồng");
        });

        switchAuto.setOnCheckedChangeListener((buttonView, isChecked) -> {
            controlRef.child("thucan").child("mode").setValue(isChecked ? "auto" : "manual");
            btnThucAn.setEnabled(!isChecked);
            inputGram.setEnabled(!isChecked);
        });

        btnThucAn.setOnClickListener(v -> {
            if (thucAnDangChay) {
                // 👉 ĐANG CHO ĂN → DỪNG LẠI
                controlRef.child("thucan").child("target_gram").setValue(0);
                controlRef.child("thucan").child("state").setValue(false);
                thucAnDangChay = false;
                btnThucAn.setText("Cho Ăn");
                Toast.makeText(this, "Đã gửi lệnh dừng cho ăn", Toast.LENGTH_SHORT).show();
            } else {
                // 👉 CHƯA CHO ĂN → THỰC HIỆN CHO ĂN
                String input = inputGram.getText().toString().trim();
                if (!input.isEmpty()) {
                    try {
                        float gram = Float.parseFloat(input);
                        if (gram <= 0) {
                            Toast.makeText(this, "Số gram phải > 0", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Lấy khối lượng hiện tại từ Firebase
                        sensorRef.child("weight").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Double currentWeight = snapshot.getValue(Double.class);
                                if (currentWeight == null) {
                                    Toast.makeText(MainActivity.this, "Không đọc được cân nặng hiện tại", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                if (gram > currentWeight) {
                                    Toast.makeText(MainActivity.this, "Không đủ lượng thức ăn để cho cá ăn", Toast.LENGTH_LONG).show();
                                } else {
                                    // Gửi lệnh cho ăn
                                    controlRef.child("thucan").child("target_gram").setValue(gram);
                                    thucAnDangChay = true;
                                    btnThucAn.setText("Dừng Cho Ăn");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(MainActivity.this, "Lỗi đọc cân nặng", Toast.LENGTH_SHORT).show();
                            }
                        });

                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Nhập không hợp lệ", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Vui lòng nhập số gram", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    private void docDuLieuCamBien() {
        sensorRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                statusText.setVisibility(View.GONE);

                Double temp = snapshot.child("temperature").getValue(Double.class);
                Double tds = snapshot.child("water_quality").getValue(Double.class);
                Long turbidity = snapshot.child("ts300b").getValue(Long.class);
                Double weight = snapshot.child("weight").getValue(Double.class);

                if (temp != null) {
                    String text = temp + " °C";
                    int anim = (temp < 24 || temp > 34) ? R.raw.rain : R.raw.sun;
                    sensorAdapter.update(0, text, anim);
                    if (temp < 24 || temp > 34) showAndSaveNotif("Nhiệt độ nước không phù hợp!");
                }

                if (tds != null) {
                    String text = tds.intValue() + " ppm";
                    int anim = (tds < 400 || tds > 1500) ? R.raw.ca : R.raw.ca;
                    sensorAdapter.update(1, text, anim);
                    if (tds < 400 || tds > 1500) showAndSaveNotif("Chất lượng nước không phù hợp!");
                }

                if (turbidity != null) {
                    String mucDo = turbidity >= 3000 ? "Rất trong" :
                            turbidity >= 2000 ? "Trong" :
                                    turbidity >= 1000 ? "Đục nhẹ" : "Rất đục";
                    String text = turbidity + " (" + mucDo + ")";
                    int anim = (turbidity < 800) ? R.raw.tds : R.raw.tds;
                    sensorAdapter.update(2, text, anim);
                    if (turbidity < 800) showAndSaveNotif("Nước quá đục!");
                }

                if (weight != null) {
                    String text = String.format("%.1f g", weight);
                    sensorAdapter.update(3, text, R.raw.cannang);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                statusText.setText("Lỗi kết nối Firebase");
            }
        });
    }

    private void showAndSaveNotif(String message) {
        long timestamp = System.currentTimeMillis();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("Cảnh báo Ao Cá")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);
        NotificationManagerCompat.from(this).notify((int) (timestamp % Integer.MAX_VALUE), builder.build());

        Map<String, Object> data = new HashMap<>();
        data.put("message", message);
        data.put("timestamp", timestamp);
        data.put("seen", false);
        notifRef.push().setValue(data);
    }

    private void demThongBaoChuaDoc() {
        notifRef.orderByChild("seen").equalTo(false)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        long count = snapshot.getChildrenCount();
                        if (count > 0) {
                            badgeText.setVisibility(View.VISIBLE);
                            badgeText.setText(String.valueOf(count));
                        } else {
                            badgeText.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private void cleanupOldNotifications() {
        long sevenDaysAgo = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000);
        notifRef.orderByChild("timestamp").endAt(sevenDaysAgo)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot child : snapshot.getChildren()) {
                            child.getRef().removeValue();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }
    private void capNhatTrangThaiChoAn() {
        controlRef.child("thucan").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Float targetGram = snapshot.child("target_gram").getValue(Float.class);
                Boolean state = snapshot.child("state").getValue(Boolean.class);

                boolean dangChoAn = (targetGram != null && targetGram > 0)
                        || (state != null && state);

                thucAnDangChay = dangChoAn;
                btnThucAn.setText(dangChoAn ? "Dừng Cho Ăn" : "Cho Ăn");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void setupSpeechRecognition() {
        btnMic.setOnClickListener(v -> {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi-VN");

            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            speechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override public void onResults(Bundle results) {
                    ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (matches != null && !matches.isEmpty()) {
                        xuLyLenhGiongNoi(matches.get(0).toLowerCase());
                    }
                }
                @Override public void onError(int error) {
                    Toast.makeText(MainActivity.this, "Lỗi giọng nói", Toast.LENGTH_SHORT).show();
                }
                @Override public void onReadyForSpeech(Bundle params) {}
                @Override public void onBeginningOfSpeech() {}
                @Override public void onRmsChanged(float rmsdB) {}
                @Override public void onBufferReceived(byte[] buffer) {}
                @Override public void onEndOfSpeech() {}
                @Override public void onPartialResults(Bundle partialResults) {}
                @Override public void onEvent(int eventType, Bundle params) {}
            });

            speechRecognizer.startListening(intent);
        });
    }

    private void xuLyLenhGiongNoi(String command) {
        if (command.contains("bật guồng")) {
            guongDangChay = true;
            controlRef.child("guong").setValue(true);
            btnGuong.setText("Tắt Guồng");
        } else if (command.contains("tắt guồng")) {
            guongDangChay = false;
            controlRef.child("guong").setValue(false);
            btnGuong.setText("Bật Guồng");
        } else if (command.contains("cho ăn")) {
            for (String word : command.split(" ")) {
                try {
                    float gram = Float.parseFloat(word);
                    controlRef.child("thucan").child("target_gram").setValue(gram);
                    inputGram.setText(String.valueOf(gram));
                    btnThucAn.setText("Dừng Cho Ăn");
                    return;
                } catch (NumberFormatException ignored) {}
            }
        } else if (command.contains("dừng cho ăn") || command.contains("hủy cho ăn")) {
            controlRef.child("thucan").child("target_gram").setValue(0);
            controlRef.child("thucan").child("state").setValue(false);
            thucAnDangChay = false;
            btnThucAn.setText("Cho Ăn");
        } else {
            Toast.makeText(this, "Không hiểu lệnh: " + command, Toast.LENGTH_SHORT).show();
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Cảnh báo ao nuôi", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Thông báo khi chất lượng nước không đạt");
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }
    }
}

package com.thuhang.hethongnuoica;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;

import java.io.IOException;

import okhttp3.*;

public class DiagnosisActivity extends AppCompatActivity {

    private Spinner spinnerLoaiCa;
    private EditText editTrieuChung;
    private Button btnChanDoan;
    private TextView txtTenBenh;
    private ImageButton btnHome;
    private View resultLayout;

    private Map<String, FishDiseases> allDiseaseData;
    private final OkHttpClient client = new OkHttpClient();

    // OpenRouter API (miễn phí)
    private final String OPENROUTER_API_KEY = "Bearer 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"; // <-- Thay bằng key của bạn
    private final String OPENROUTER_URL = "https://openrouter.ai/api/v1/chat/completions";

    private final String[] hienThiCa = {"Cá rô phi", "Cá chép", "Cá trắm cỏ", "Cá trắm đen", "Cá mè"};
    private final String[] tenCaTrongJson = {"ca_ro_phi", "ca_chep", "ca_tram_co", "ca_tram_den", "ca_me"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diagnosis);

        spinnerLoaiCa = findViewById(R.id.spinnerLoaiCa);
        editTrieuChung = findViewById(R.id.editTrieuChung);
        btnChanDoan = findViewById(R.id.btnChanDoan);
        btnHome = findViewById(R.id.btnHome);

        txtTenBenh = findViewById(R.id.txtTenBenh);

        resultLayout = findViewById(R.id.resultLayout);


        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, hienThiCa);
        spinnerLoaiCa.setAdapter(adapter);

        loadJsonData();

        btnChanDoan.setOnClickListener(v -> {
            int pos = spinnerLoaiCa.getSelectedItemPosition();
            String inputText = editTrieuChung.getText().toString().trim();

            if (inputText.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập triệu chứng!", Toast.LENGTH_SHORT).show();
                return;
            }

            String fishKey = tenCaTrongJson[pos];
            sendSymptomToOpenRouter(fishKey, inputText);
        });
        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

    }

    private void loadJsonData() {
        try {
            InputStream inputStream = getResources().openRawResource(R.raw.benh_ca);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }

            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, FishDiseases>>() {}.getType();
            allDiseaseData = gson.fromJson(jsonBuilder.toString(), type);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Không đọc được dữ liệu bệnh!", Toast.LENGTH_LONG).show();
        }
    }

    private void sendSymptomToOpenRouter(String fishKey, String inputText) {
        try {
            FishDiseases data = allDiseaseData.get(fishKey);
            if (data == null || data.benh == null) {
                Toast.makeText(this, "Không có dữ liệu cho loài cá này!", Toast.LENGTH_SHORT).show();
                return;
            }

            Gson gson = new Gson();
            String jsonBenh = gson.toJson(data);

            JSONArray messages = new JSONArray();

            messages.put(new JSONObject()
                    .put("role", "system")
                    .put("content", "Bạn là chuyên gia bệnh học thủy sản. Dựa vào dữ liệu bệnh dưới đây, hãy chẩn đoán tên bệnh, nguyên nhân và cách điều trị khi người dùng mô tả triệu chứng."));

            messages.put(new JSONObject()
                    .put("role", "user")
                    .put("content", "Dữ liệu bệnh cá: " + jsonBenh));

            messages.put(new JSONObject()
                    .put("role", "user")
                    .put("content", "Triệu chứng: " + inputText));

            JSONObject requestJson = new JSONObject();
            requestJson.put("model", "openai/gpt-3.5-turbo");  // bạn cũng có thể thử "anthropic/claude-3-opus"
            requestJson.put("messages", messages);
            requestJson.put("temperature", 0.7);

            RequestBody body = RequestBody.create(
                    requestJson.toString(),
                    MediaType.get("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(OPENROUTER_URL)
                    .addHeader("Authorization", OPENROUTER_API_KEY)
                    .addHeader("HTTP-Referer", "https://yourapp.example") // có thể là bất kỳ tên miền nào
                    .addHeader("X-Title", "FishDiagnosisApp")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() ->
                            Toast.makeText(DiagnosisActivity.this, "Không thể kết nối OpenRouter: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        String err = response.body().string();
                        Log.e("GPT_ERROR", err);
                        runOnUiThread(() ->
                                Toast.makeText(DiagnosisActivity.this, "Lỗi GPT: " + err, Toast.LENGTH_LONG).show()
                        );
                        return;
                    }



                    String responseStr = response.body().string();
                    try {
                        JSONObject root = new JSONObject(responseStr);
                        JSONArray choices = root.getJSONArray("choices");
                        String reply = choices.getJSONObject(0).getJSONObject("message").getString("content");
                        String replyClean = reply.replaceAll("\\*\\*", "").replaceAll("[!@#$%^&*]", "");



                        runOnUiThread(() -> {
                            txtTenBenh.setText(replyClean);

                            resultLayout.setVisibility(View.VISIBLE);
                        });

                    } catch (Exception e) {
                        runOnUiThread(() ->
                                Toast.makeText(DiagnosisActivity.this, "Lỗi xử lý phản hồi GPT", Toast.LENGTH_SHORT).show()
                        );
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi chuẩn bị dữ liệu gửi GPT", Toast.LENGTH_SHORT).show();
        }
    }
}

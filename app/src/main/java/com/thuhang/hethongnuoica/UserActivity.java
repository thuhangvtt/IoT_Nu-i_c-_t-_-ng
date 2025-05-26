package com.thuhang.hethongnuoica;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.view.Window;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class UserActivity extends AppCompatActivity {

    private TextView emailText;
    private EditText inputName, inputPhone;
    private Button btnSaveInfo, btnHistory, btnChart, btnLogout;
    private ImageView profileImage;
    private ImageButton btnHome;

    private DatabaseReference userRef;
    private String uid;

    private static final int PICK_IMAGE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        emailText = findViewById(R.id.emailText);
        inputName = findViewById(R.id.inputName);
        inputPhone = findViewById(R.id.inputPhone);
        btnSaveInfo = findViewById(R.id.btnSaveInfo);
        btnHistory = findViewById(R.id.btnHistory);
        btnChart = findViewById(R.id.btnChart);
        btnLogout = findViewById(R.id.btnLogout);
        profileImage = findViewById(R.id.profileImage);
        btnHome = findViewById(R.id.btnHome);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            finish();
            return;
        }

        uid = user.getUid();
        emailText.setText("Email: " + user.getEmail());

        userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String phone = snapshot.child("phone").getValue(String.class);
                    String avatarBase64 = snapshot.child("avatarBase64").getValue(String.class);

                    if (name != null) inputName.setText(name);
                    if (phone != null) inputPhone.setText(phone);

                    if (avatarBase64 != null && !avatarBase64.isEmpty()) {
                        byte[] decoded = Base64.decode(avatarBase64, Base64.DEFAULT);
                        Bitmap avatar = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                        profileImage.setImageBitmap(avatar);
                    } else {
                        // Gán ảnh mặc định nếu không có base64
                        profileImage.setImageResource(R.drawable.fish_avata);
                    }
                } else {
                    // Gán mặc định nếu không có node người dùng
                    profileImage.setImageResource(R.drawable.fish_avata);
                }
            }


            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(UserActivity.this, "Lỗi tải thông tin!", Toast.LENGTH_SHORT).show();
            }
        });

        profileImage.setOnClickListener(v -> showBottomSheet());

        btnSaveInfo.setOnClickListener(v -> {
            String name = inputName.getText().toString().trim();
            String phone = inputPhone.getText().toString().trim();
            userRef.child("name").setValue(name);
            userRef.child("phone").setValue(phone);
            Toast.makeText(this, "Đã lưu thông tin", Toast.LENGTH_SHORT).show();
        });

        btnHistory.setOnClickListener(v -> startActivity(new Intent(this, LogHistoryActivity.class)));
        btnChart.setOnClickListener(v -> startActivity(new Intent(this, ChartActivity.class)));

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });
    }

    private void showBottomSheet() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottom_sheet_avatar);

        LinearLayout pickImage = dialog.findViewById(R.id.optionPick);
        LinearLayout deleteImage = dialog.findViewById(R.id.optionDelete);
        Button cancel = dialog.findViewById(R.id.btnCancel);

        pickImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE);
            dialog.dismiss();
        });

        deleteImage.setOnClickListener(v -> {
            userRef.child("avatarBase64").removeValue();
            profileImage.setImageResource(R.drawable.fish_avata);
            dialog.dismiss();
        });

        cancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri sourceUri = data.getData();
            Uri destinationUri = Uri.fromFile(new File(getCacheDir(), "cropped.jpg"));

            UCrop.Options options = new UCrop.Options();
            options.setCircleDimmedLayer(true);
            options.setShowCropFrame(false);
            options.setShowCropGrid(false);

            UCrop.of(sourceUri, destinationUri)
                    .withAspectRatio(1, 1)
                    .withMaxResultSize(300, 300)
                    .withOptions(options)
                    .start(this);
        }

        if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK && data != null) {
            Uri resultUri = UCrop.getOutput(data);
            if (resultUri != null) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                    profileImage.setImageBitmap(bitmap);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                    String base64 = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
                    userRef.child("avatarBase64").setValue(base64);

                    Toast.makeText(this, "Đã cập nhật ảnh đại diện", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Lỗi xử lý ảnh", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}

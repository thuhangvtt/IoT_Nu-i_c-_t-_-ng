package com.thuhang.hethongnuoica;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText emailEditText;
    private Button resetPasswordButton;
    private TextView loginTextView;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        // Khởi tạo Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Liên kết các view
        emailEditText = findViewById(R.id.emailEditText);
        resetPasswordButton = findViewById(R.id.resetPasswordButton);
        loginTextView = findViewById(R.id.loginTextView);

        // Xử lý sự kiện click nút gửi yêu cầu reset mật khẩu
        resetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });

        // Quay lại trang đăng nhập
        loginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Kết thúc activity và quay lại màn hình trước đó (LoginActivity)
            }
        });
    }

    // Hàm gửi yêu cầu reset mật khẩu
    private void resetPassword() {
        String email = emailEditText.getText().toString();

        if (email.isEmpty()) {
            Toast.makeText(ResetPasswordActivity.this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Nếu thành công, thông báo cho người dùng
                        Toast.makeText(ResetPasswordActivity.this, "Đã gửi liên kết đặt lại mật khẩu tới email của bạn", Toast.LENGTH_SHORT).show();
                        finish(); // Quay lại trang đăng nhập
                    } else {
                        // Nếu có lỗi, hiển thị thông báo lỗi
                        Toast.makeText(ResetPasswordActivity.this, "Lỗi khi gửi yêu cầu. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

# Hệ thống nuôi cá tự động IoT (Android + ESP32)

Dự án xây dựng hệ thống giám sát và điều khiển cho cá ăn tự động, ứng dụng IoT kết hợp giữa phần cứng ESP32 và phần mềm Android.

---
Ảnh minh họa cho app

![d8](https://github.com/user-attachments/assets/6d0d545b-8301-4bf7-82fc-4414dbc4a95a)

---
 Ứng dụng Android

- Hiển thị thông số:
  -  Nhiệt độ nước (DS18B20)
  - Chất lượng nước TDS
  - Độ đục (TS-300B)
  - Trọng lượng thức ăn (LoadCell + HX711)
- Điều khiển từ xa qua Firebase:
  - Cho cá ăn theo lịch
  - Bật/tắt guồng tạo oxy và máy cho ăn
- Các chức năng:
  - Đăng nhập, đăng ký, lấy lại mật khẩu
  - Biểu đồ chất lượng nước (MPAndroidChart)
  - Cảnh báo nếu vượt ngưỡng
  - Chẩn đoán bệnh cho cá (AI đơn giản)
  - Lưu lịch sử cho ăn

---

Mã điều khiển ESP32 (Arduino)

- Lập trình bằng Arduino IDE
- Kết nối cảm biến và gửi dữ liệu lên Firebase Realtime Database
- Điều khiển guồng và máy cho ăn bằng lệnh từ Firebase
- Ghi log cho ăn, phát cảnh báo nếu chất lượng nước kém

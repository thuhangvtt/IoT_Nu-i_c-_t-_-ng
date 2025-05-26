# Hệ thống nuôi cá tự động IoT (Android + ESP32)

Dự án xây dựng hệ thống giám sát và điều khiển cho cá ăn tự động, ứng dụng IoT kết hợp giữa phần cứng ESP32 và phần mềm Android.

---
## Ảnh minh họa cho app

<p align="center">
  <img src="https://github.com/user-attachments/assets/c05ab22d-224e-4d40-8d5d-332f213d493b" alt="UI 1" width="30%"/>
  <img src="https://github.com/user-attachments/assets/482c2187-0e1c-41bf-9aba-b97be7c82130" alt="UI 2" width="30%"/>
  <img src="https://github.com/user-attachments/assets/7a10ea82-c745-4948-aab0-e95470bd9907" alt="UI 3" width="30%"/>
</p>


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
  - Cho ăn, bật guồng bằng giọng nói
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

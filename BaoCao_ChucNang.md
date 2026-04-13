# Báo Cáo Chức Năng Ứng Dụng AngriSmart (Ngoại trừ Đăng nhập/Đăng ký)

Dưới đây là phần tổng hợp các hệ thống, tính năng, thư viện, cách triển khai và API mà chúng ta đang sử dụng cho toàn bộ dự án (cả Android và Backend).

---

## 1. Chức năng Xem Dự Báo Thời Tiết (Weather Forecast)
Đây là chức năng quan trọng giúp nông dân xem nhiệt độ, độ ẩm để lên kế hoạch canh tác lúa.

- **Thư viện sử dụng**: 
  - Nhóm HTTP Client: `Retrofit2` (v2.9.0) và `OkHttp3` (v4.12.0) giúp gọi mạng mượt mà.
  - Cấu trúc dữ liệu: `GsonConverterFactory` để tự động ánh xạ (parse) file JSON trả về thành Kotlin Data Class (`WeatherForecastResponse`).
  - Đa luồng: `Kotlinx Coroutines` để chạy ngầm, không làm treo UI khi load.
- **Cách thức triển khai (Tỉa / Tối ưu hoá payload)**: 
  - Khởi tạo request trong interface `WeatherApiService.kt`.
  - **"Tỉa" dữ liệu API**: Để tăng tốc độ load mạng và tiết kiệm băng thông, ứng dụng chỉ query lấy lượng dữ liệu vừa đủ bao gồm: dự báo 7 ngày (`forecast_days=7`), vĩ độ (`latitude`), kinh độ (`longitude`), nhiệt độ cao/thấp (`temperature_2m_max`, `temperature_2m_min`), và độ ẩm cao nhất (`relative_humidity_2m_max`). Việc lược bỏ đi những thông số không cần thiết giúp Response JSON cực kỳ nhẹ.
  - Tự động hóa thiết lập múi giờ địa phương bằng `Asia/Ho_Chi_Minh`.
- **API sử dụng**: **OpenMeteo API** (`v1/forecast`).
- **Giới hạn miễn phí**: Đây là tổ chức API nguồn mở không cần tài khoản/API Key. Bản dùng phi thương mại (Non-Commercial) cấp mức truy cập khổng lồ lên đến **10,000 requests/ngày** (rất dư sức làm đồ án/dự án sinh viên).

---

## 2. Hệ thống AI Quét Ảnh Chẩn Đoán Bệnh Lúa (Disease Detection Feature)
Hệ thống lõi bao gồm giao diện bật Camera quét trực tiếp ở Mobile và đưa về Backend xử lý Computer Vision.

- **Thư viện sử dụng**:
  - **Phía Android**: Thư viện `CameraX` của Google (phiên bản `1.4.1`) bao gồm: `camera-core`, `camera-camera2`, `camera-lifecycle`, `camera-view`.
  - **Phía Backend Python**: Sử dụng `FastAPI` làm nòng cốt, `Uvicorn` để chạy server, và `Pydantic` giúp validate đầu vào.
- **Cách thức triển khai**:
  - **CameraX 1.4.1 siêu nhẹ**: Mã nguồn đã được nới lên biên dịch SDK đỉnh cao để hỗ trợ `16KB Page Size` của phiên bản Android 15 mới nhất. Các lifecycle của camera được gắn cứng vào vòng đời của Jetpack Compose đảm bảo không văng app (crash) hay leak memory.
  - **Bắn luồng qua Local Network**: Cấu hình file `RetrofitClient.kt` gọi tới `http://10.0.2.2:8000/`. Máy ảo Android sẽ hiểu đó là localhost của máy tính để luân chuyển hình ảnh. 
  - **Tối ưu Backend API**: Xây dựng Middleware `CORS` với `allow_origins=["*"]` đảm bảo máy di động kết nối an toàn với máy chủ. Hình ảnh sẽ được POST thẳng vào route module `/api/v1/disease`.
- **API sử dụng**: **Custom Backend local Web API** (được code bằng FastAPI).
- **Giới hạn miễn phí**: **100% không giới hạn và miễn cước phí**. Đây là API do chính chúng ta chạy bằng sức mạnh (Card đồ họa, CPU) máy tính ở nhà (Local Server).

---

## 3. Hệ thống Lập lịch thông báo ngầm (Background Job / Daily Push)
Tính năng thông tin cập nhật cho người nông dân một cách chủ động hằng ngày.

- **Thư viện sử dụng**: `androidx.work:work-runtime-ktx` phiên bản `2.9.0` (WorkManager).
- **Cách thức triển khai**:
  - Viết worker xử lý việc gửi thông báo nhắc nhở chăm sóc đồng ruộng, cảnh báo hạn hán hoặc đẩy thời tiết vào màn hình lockscreen mỗi sáng.
  - **Tỉa & Tiết Kiệm Pin**: WorkManager sẽ tự động xử lý Doze Mode của hệ điều hành. App sẽ tận dụng thời điểm các app khác đang kết nối mạng để "đi ké", vừa hoàn thành việc chạy ngầm dưới background vừa không hao tốn tài nguyên thiết bị di động.
- **Giới hạn miễn phí**: API của bản thân hệ điều hành (Native OS API) - hoàn toàn miễn phí, không tốn tài nguyên Cloud.

---

## 4. Quản trị Cơ Sở Dữ Liệu Lưu Trữ Đám Mây (Cloud Database)
Hệ thống đồng bộ lịch sử quét AI và dữ liệu môi trường với thời gian thực.

- **Thư viện sử dụng**: Tích hợp chung qua `Firebase BOM` gồm `firebase-firestore` (Cloud) và `firebase-database` (Realtime).
- **Cách thức triển khai**:
  - Firebase được gắn trực tiếp vào App qua file `google-services.json` (kết nối Gradle plugin google.services).
  - Tối ưu query qua Coroutines để đọc/ghi dữ liệu dạng non-blocking (không khoá giao diện).
- **API sử dụng**: **Firebase Cloud API**.
- **Giới hạn miễn phí (Gói Spark Firebase)**:
  - **Cloud Firestore**: Cho phép **50,000 lần Đọc**, **20,000 lần Ghi** và **20,000 lần Xóa** mỗi ngày. Tổng dung lượng lưu trữ 1GB.
  - **Realtime Database**: Miễn phí lưu trữ 1GB, lượng tải xuống 10GB/tháng và cùng lúc hỗ trợ 100 người dùng trực tuyến (connections). Đủ tải cho các mục đích Demo mà không lo nạp thẻ tín dụng.

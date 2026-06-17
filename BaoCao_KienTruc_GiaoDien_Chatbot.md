# Báo Cáo Kỹ Thuật: Xây Dựng Giao Diện (UI) & Tích Hợp Chatbot RAG AI


---

## 1. Phát Triển Giao Diện (User Interface - UI)

### 1.1. Frameworks và Thư Viện Chính
Ứng dụng Android được xây dựng hoàn toàn dựa trên xu hướng phát triển ứng dụng di động hiện đại của Google:
*   **Jetpack Compose**: Framework UI theo hướng khai báo (Declarative UI). Thay vì khởi tạo các file Layout kéo thả XML truyền thống, mã lệnh được viết thuần bằng Kotlin. Việc này giúp giảm codebase, dễ bảo trì và dễ dàng tuỳ biến quản lý Data layer truyền xuống UI.
*   **Material Design 3 (M3)**: Phân tầng thiết kế giao diện từ Google. Sử dụng `Scaffold`, `TopAppBar`, `Card`, `OutlinedTextField` giúp ứng dụng có giao diện nguyên bản (native), mượt mà, trực quan hơn và tiết kiệm vô vàn thời gian CSS/Style.
*   **Google Maps SDK for Compose** (`com.google.maps.android:maps-compose`): Thư viện chuyên dụng tích hợp bản đồ Google Maps native trực tiếp vào Jetpack Compose.
*   **AndroidX Lifecycle & ViewModel**: Quản lý vòng đời (lifecycle) của màn hình, không làm mất dữ liệu khi app xoay màn hình hay ẩn xuống nền. Đây cũng là cầu nối State Management (hiện thực hoá thông qua `StateFlow`) quan trọng cho Compose.

### 1.2. Biện Pháp & Quy Trình Thực Hiện
*   **Kiến trúc Component-Driven**: Chia màn hình thành nhiều hàm `@Composable` nhỏ gọn (Ví dụ: `GrowthProgressCard`, `InfoCard` trong màn hình `FieldDetailScreen`). Việc này cho phép chúng ta tái sử dụng một thẻ UI trên nhiều màn hình khác nhau.
*   **Reactive State**: UI phản ứng 100% tự động dựa trên trạng thái (State). Ví dụ, tại `AddFieldScreen`, các thanh điền chữ được lưu dưới dạng biến `remember { mutableStateOf("") }`. Khi dữ liệu được thay đổi, chỉ duy nhất block chứa Component Text Field đó được "tái hiện" (Re-composition), tối ưu hoá tối đa hiệu suất bộ nhớ.

## 2. Cách Tích Hợp API Chatbot Trí Tuệ Nhân Tạo (Python Backend)

### 2.1. Kiến Trúc Hoạt Động (Client - Server)
 Sử dụng mô hình truyền thống nổi tiếng **Retrofit2 + OkHttp3 + Gson**.
    *   `Retrofit`: Chịu trách nhiệm tạo Interface mạng (ví dụ: `ApiService`) để khai báo nhanh các Endpoints POST/GET/DELETE.
    *   `OkHttp`: Xử lý lõi phía dưới cho HTTP Request, quản lý Timeout, gắn Interceptor cho Logging dữ liệu hoặc gán Access Token (Bearer Header) bảo mật.
    *   `Gson Converter`: Giải mã gói chữ chuỗi cục bộ JSON (từ Backend API trả về) được ép/parse kiểu tự động thành các Data Class (Model Object) để dùng cực dễ bên trong Kotlin.



# 🚗 Car Rental System – Project Structure

> **Stack:** Spring Boot 4.0 + SQL Server + HTML/CSS/JS
> **Package gốc:** `com.carrental`  
> **Java:** 21 | **Lombok** ✅ | **Spring Security + JWT** ✅ | **JPA** ✅

---

## 📁 BACKEND – Cấu trúc thư mục Java

```
src/main/java/com/carrental/
│
├── CarRentalSystemApplication.java          ← [CÓ SẴN] Entry point
│
├── config/                                  ← Cấu hình hệ thống
│   ├── SecurityConfig.java                  ← Cấu hình Spring Security, phân quyền theo role
│   ├── JwtConfig.java                       ← Cấu hình JWT (secret key, expiration)
│   ├── CorsConfig.java                      ← Cho phép frontend gọi API (cross-origin)
│   └── FileStorageConfig.java               ← Cấu hình đường dẫn lưu file ảnh upload
│
├── security/                                ← Xử lý JWT & Authentication
│   ├── JwtTokenProvider.java                ← Tạo, validate, parse JWT token
│   ├── JwtAuthenticationFilter.java         ← Filter kiểm tra JWT trên mỗi request
│   └── CustomUserDetailsService.java        ← Load user từ DB để Spring Security xác thực
│
├── entity/                                  ← Ánh xạ các bảng trong DB (JPA Entity)
│   ├── User.java                            ← Bảng User (userId, phone, email, role, status...)
│   ├── Car.java                             ← Bảng Car (carId, ownerId, brandId, status...)
│   ├── CarImage.java                        ← Bảng CarImage (ảnh xe)
│   ├── CarType.java                         ← Bảng CarType (Sedan, SUV, MPV...)
│   ├── Brand.java                           ← Bảng Brand (Toyota, Honda...)
│   ├── Region.java                          ← Bảng Region (Hà Nội, Đà Nẵng...)
│   ├── CarSchedule.java                     ← Bảng CarSchedule (lịch xe bị block)
│   ├── RentalOrder.java                     ← Bảng RentalOrder (đơn thuê xe)
│   ├── RentalImage.java                     ← Bảng RentalImage (ảnh nhận/trả xe)
│   ├── Payment.java                         ← Bảng Payment (giao dịch đặt cọc, hoàn tiền)
│   ├── Review.java                          ← Bảng Review (đánh giá xe)
│   ├── Complaint.java                       ← Bảng Complaint (khiếu nại/báo cáo sự cố)
│   ├── IdentityVerification.java            ← Bảng IdentityVerification (xác minh CCCD+GPLX)
│   └── OwnerRegistration.java               ← Bảng OwnerRegistration (đăng ký chủ xe)
│
├── repository/                              ← Tầng truy vấn DB (extends JpaRepository)
│   ├── UserRepository.java                  ← Truy vấn User (tìm theo phone, email, role...)
│   ├── CarRepository.java                   ← Truy vấn Car (tìm xe theo region, status, loại...)
│   ├── CarImageRepository.java              ← Truy vấn ảnh xe theo carId
│   ├── CarTypeRepository.java               ← Truy vấn danh mục loại xe
│   ├── BrandRepository.java                 ← Truy vấn danh mục hãng xe
│   ├── RegionRepository.java                ← Truy vấn danh mục khu vực
│   ├── CarScheduleRepository.java           ← Truy vấn lịch xe (kiểm tra ngày trống)
│   ├── RentalOrderRepository.java           ← Truy vấn đơn thuê (theo customer, owner, status)
│   ├── RentalImageRepository.java           ← Truy vấn ảnh nhận/trả xe theo orderId
│   ├── PaymentRepository.java               ← Truy vấn giao dịch thanh toán
│   ├── ReviewRepository.java                ← Truy vấn đánh giá theo xe, theo order
│   ├── ComplaintRepository.java             ← Truy vấn khiếu nại theo status, sender
│   ├── IdentityVerificationRepository.java  ← Truy vấn xác minh danh tính theo userId
│   └── OwnerRegistrationRepository.java     ← Truy vấn đăng ký chủ xe theo userId, status
│
├── dto/                                     ← Data Transfer Object (dữ liệu gửi/nhận API)
│   │
│   ├── request/                             ← DTO nhận dữ liệu từ client (request body)
│   │   ├── auth/
│   │   │   ├── RegisterRequest.java         ← Đăng ký tài khoản (name, phone, email, password)
│   │   │   ├── LoginRequest.java            ← Đăng nhập (phone, password)
│   │   │   └── ForgotPasswordRequest.java   ← Quên mật khẩu (phone, otp, newPassword)
│   │   ├── user/
│   │   │   ├── UpdateProfileRequest.java    ← Cập nhật hồ sơ (name, email, address, avatar)
│   │   │   ├── ChangePasswordRequest.java   ← Đổi mật khẩu (oldPass, newPass, confirmPass)
│   │   │   └── LockAccountRequest.java      ← Khóa tài khoản (reason)
│   │   ├── car/
│   │   │   ├── CarRequest.java              ← Thêm/sửa xe (brandId, model, price, region...)
│   │   │   └── CarScheduleRequest.java      ← Cập nhật lịch xe (startDate, endDate, type)
│   │   ├── order/
│   │   │   ├── CreateOrderRequest.java      ← Tạo đơn thuê (carId, pickupDate, returnDate...)
│   │   │   └── OrderActionRequest.java      ← Xác nhận/từ chối/hủy đơn (reason)
│   │   ├── payment/
│   │   │   └── PaymentRequest.java          ← Xác nhận thanh toán (method, orderId)
│   │   ├── review/
│   │   │   └── ReviewRequest.java           ← Gửi đánh giá (rating, comment)
│   │   ├── complaint/
│   │   │   ├── ComplaintRequest.java        ← Gửi khiếu nại/báo cáo (type, content, images)
│   │   │   └── ResolveComplaintRequest.java ← Xử lý khiếu nại (status, resolution)
│   │   ├── identity/
│   │   │   └── IdentityVerificationRequest.java ← Gửi xác minh danh tính (CCCD + GPLX)
│   │   └── category/
│   │       ├── CarTypeRequest.java          ← Thêm/sửa loại xe
│   │       ├── BrandRequest.java            ← Thêm/sửa hãng xe
│   │       └── RegionRequest.java           ← Thêm/sửa khu vực
│   │
│   └── response/                            ← DTO trả dữ liệu về client (response body)
│       ├── ApiResponse.java                 ← Wrapper chung: {success, message, data}
│       ├── auth/
│       │   └── LoginResponse.java           ← Trả token + thông tin user sau đăng nhập
│       ├── user/
│       │   ├── UserResponse.java            ← Thông tin user (không có password)
│       │   └── UserDetailResponse.java      ← Chi tiết user cho Admin (kèm verification)
│       ├── car/
│       │   ├── CarResponse.java             ← Thông tin xe (dùng trong danh sách)
│       │   ├── CarDetailResponse.java       ← Chi tiết xe (kèm ảnh, lịch, đánh giá)
│       │   └── CarScheduleResponse.java     ← Lịch xe (danh sách ngày blocked)
│       ├── order/
│       │   ├── OrderResponse.java           ← Thông tin đơn thuê (dùng trong danh sách)
│       │   └── OrderDetailResponse.java     ← Chi tiết đơn thuê (kèm timeline, ảnh)
│       ├── payment/
│       │   ├── PaymentResponse.java         ← Thông tin giao dịch
│       │   └── PaymentDetailResponse.java   ← Chi tiết giao dịch
│       ├── review/
│       │   └── ReviewResponse.java          ← Thông tin đánh giá (name, rating, comment)
│       ├── complaint/
│       │   └── ComplaintResponse.java       ← Thông tin khiếu nại
│       ├── identity/
│       │   └── IdentityVerificationResponse.java ← Kết quả xác minh danh tính
│       └── dashboard/
│           └── DashboardResponse.java       ← Dữ liệu dashboard Admin (KPI, charts)
│
├── service/                                 ← Business logic (xử lý nghiệp vụ chính)
│   ├── AuthService.java                     ← Đăng ký, đăng nhập, quên mật khẩu, JWT
│   ├── UserService.java                     ← Quản lý hồ sơ, đổi mật khẩu
│   ├── AdminUserService.java                ← Admin: xem/khóa/mở user, duyệt chủ xe
│   ├── CarService.java                      ← CRUD xe, tìm kiếm & lọc xe cho Guest/Customer
│   ├── OwnerCarService.java                 ← Owner: quản lý xe cá nhân, lịch xe
│   ├── AdminCarService.java                 ← Admin: duyệt/từ chối xe đăng mới
│   ├── RentalOrderService.java              ← Tạo đơn, xem đơn (Customer & Owner)
│   ├── AdminOrderService.java               ← Admin: xem toàn bộ đơn hệ thống
│   ├── PaymentService.java                  ← Xử lý đặt cọc, hoàn tiền, lịch sử giao dịch
│   ├── ReviewService.java                   ← Gửi đánh giá, xem danh sách đánh giá
│   ├── ComplaintService.java                ← Gửi & xử lý khiếu nại/báo cáo sự cố
│   ├── IdentityVerificationService.java     ← Gửi & duyệt xác minh CCCD/GPLX
│   ├── OwnerRegistrationService.java        ← Đăng ký & duyệt tài khoản chủ xe
│   ├── CategoryService.java                 ← Admin: CRUD loại xe, hãng xe, khu vực
│   ├── DashboardService.java                ← Thống kê KPI, doanh thu, dashboard
│   └── FileStorageService.java              ← Upload & lưu file ảnh vào server
│
├── controller/                              ← REST Controller (định nghĩa API endpoints)
│   ├── AuthController.java                  ← POST /api/auth/register, /login, /forgot-password
│   ├── UserController.java                  ← GET/PUT /api/users/me (profile, password)
│   ├── AdminUserController.java             ← GET/PUT /api/admin/users (Admin only)
│   ├── CarController.java                   ← GET /api/cars (tìm kiếm, lọc, chi tiết xe)
│   ├── OwnerCarController.java              ← CRUD /api/owner/cars (Owner only)
│   ├── AdminCarController.java              ← GET/PUT /api/admin/cars/pending (Admin only)
│   ├── RentalOrderController.java           ← CRUD /api/orders (Customer & Owner)
│   ├── AdminOrderController.java            ← GET /api/admin/orders (Admin only)
│   ├── PaymentController.java               ← POST/GET /api/payments
│   ├── ReviewController.java                ← POST/GET /api/reviews
│   ├── ComplaintController.java             ← POST/GET /api/complaints
│   ├── IdentityVerificationController.java  ← POST/PUT /api/identity
│   ├── OwnerRegistrationController.java     ← POST/PUT /api/owner-registration
│   ├── CategoryController.java              ← CRUD /api/admin/categories (Admin only)
│   ├── DashboardController.java             ← GET /api/admin/dashboard, /api/owner/stats
│   └── FileController.java                  ← POST /api/files/upload (upload ảnh)
│
├── exception/                               ← Xử lý lỗi tập trung
│   ├── GlobalExceptionHandler.java          ← @ControllerAdvice bắt toàn bộ exception
│   ├── ResourceNotFoundException.java       ← Lỗi 404: không tìm thấy resource
│   ├── BadRequestException.java             ← Lỗi 400: dữ liệu không hợp lệ
│   ├── UnauthorizedException.java           ← Lỗi 401: chưa đăng nhập
│   └── ForbiddenException.java             ← Lỗi 403: không có quyền truy cập
│
└── util/                                    ← Các utility/helper class
    ├── DateTimeUtil.java                    ← Tính số ngày thuê, format ngày giờ
    ├── DepositCalculator.java               ← Tính tiền cọc (30% tổng tiền)
    ├── RefundCalculator.java                ← Tính tiền hoàn theo chính sách hủy
    └── SlugUtil.java                        ← Generate mã đơn, mã giao dịch duy nhất
```

---

## 📁 BACKEND – Resources

```
src/main/resources/
│
├── application.properties                   ← [CÓ SẴN] Cấu hình DB, JWT, server port
└── uploads/                                 ← Thư mục lưu file ảnh upload (tạo runtime)
    ├── avatars/                             ← Ảnh đại diện user
    ├── cars/                                ← Ảnh xe
    ├── identity/                            ← Ảnh CCCD/GPLX
    └── rentals/                             ← Ảnh nhận/trả xe
```

---

## 📁 FRONTEND – Cấu trúc thư mục

```
src/main/resources/static/
│
├── index.html                               ← Trang chủ (tìm kiếm xe, hiển thị xe nổi bật)
│
├── assets/
│   ├── css/
│   │   ├── bootstrap.min.css                ← Bootstrap 5 (download về dùng offline)
│   │   ├── global.css                       ← CSS chung toàn hệ thống (font, màu, layout)
│   │   ├── auth.css                         ← CSS cho trang đăng ký/đăng nhập
│   │   ├── car-list.css                     ← CSS cho trang danh sách & lọc xe
│   │   ├── car-detail.css                   ← CSS cho trang chi tiết xe
│   │   ├── order.css                        ← CSS cho trang tạo đơn & quản lý đơn
│   │   ├── profile.css                      ← CSS cho trang hồ sơ cá nhân
│   │   └── admin.css                        ← CSS cho giao diện Admin/Dashboard
│   │
│   ├── js/
│   │   ├── bootstrap.bundle.min.js          ← Bootstrap JS
│   │   ├── config.js                        ← Khai báo BASE_URL API, hằng số chung
│   │   ├── auth.js                          ← Hàm lưu/xóa JWT token, kiểm tra login
│   │   └── utils.js                         ← Format tiền VNĐ, ngày giờ, show toast...
│   │
│   └── images/
│       ├── logo.png                         ← Logo hệ thống
│       └── placeholder-car.jpg             ← Ảnh mặc định khi xe chưa có ảnh
│
├── pages/
│   │
│   ├── auth/
│   │   ├── login.html                       ← Trang đăng nhập (phone + password)
│   │   ├── register.html                    ← Trang đăng ký tài khoản
│   │   └── forgot-password.html            ← Trang quên mật khẩu (OTP flow)
│   │
│   ├── guest/                               ← Các trang không cần đăng nhập
│   │   ├── car-list.html                    ← Danh sách xe + bộ lọc
│   │   └── car-detail.html                  ← Chi tiết xe (ảnh, thông số, lịch, review)
│   │
│   ├── customer/                            ← Các trang dành cho Customer
│   │   ├── profile.html                     ← Xem & chỉnh sửa hồ sơ cá nhân
│   │   ├── identity-verification.html       ← Upload CCCD + GPLX để xác minh danh tính
│   │   ├── owner-registration.html          ← Đăng ký trở thành chủ xe
│   │   ├── create-order.html                ← Tạo đơn thuê xe (chọn ngày, giờ, hình thức)
│   │   ├── payment.html                     ← Thanh toán đặt cọc
│   │   ├── order-list.html                  ← Danh sách đơn thuê của tôi
│   │   ├── order-detail.html                ← Chi tiết đơn thuê + timeline trạng thái
│   │   ├── pickup-confirm.html              ← Xác nhận nhận xe (checklist + upload ảnh)
│   │   ├── return-confirm.html              ← Xác nhận trả xe (checklist + upload ảnh)
│   │   ├── review.html                      ← Đánh giá xe sau khi hoàn thành
│   │   ├── transaction-list.html            ← Lịch sử giao dịch
│   │   ├── transaction-detail.html          ← Chi tiết giao dịch
│   │   └── incident-report.html             ← Báo cáo sự cố trong chuyến thuê
│   │
│   ├── owner/                               ← Các trang dành cho Owner
│   │   ├── car-list.html                    ← Danh sách xe của tôi
│   │   ├── car-add.html                     ← Thêm xe mới (form đầy đủ + upload giấy tờ)
│   │   ├── car-edit.html                    ← Chỉnh sửa thông tin xe
│   │   ├── car-schedule.html                ← Xem & cập nhật lịch khả dụng xe (calendar)
│   │   ├── order-list.html                  ← Danh sách đơn thuê xe của tôi
│   │   ├── order-detail.html                ← Chi tiết đơn + nút xác nhận/từ chối
│   │   ├── revenue-stats.html               ← Thống kê doanh thu (biểu đồ, bảng)
│   │   ├── rental-history.html              ← Lịch sử cho thuê xe
│   │   └── complaint.html                   ← Gửi khiếu nại về khách thuê
│   │
│   └── admin/                               ← Các trang dành cho Admin
│       ├── dashboard.html                   ← Dashboard tổng quan (KPI + biểu đồ)
│       ├── user-list.html                   ← Danh sách & tìm kiếm người dùng
│       ├── user-detail.html                 ← Chi tiết người dùng + khóa/mở tài khoản
│       ├── identity-review.html             ← Duyệt xác minh CCCD/GPLX
│       ├── owner-approval.html              ← Duyệt/từ chối đăng ký chủ xe
│       ├── car-pending.html                 ← Danh sách xe chờ duyệt
│       ├── car-review.html                  ← Duyệt/từ chối xe đăng mới
│       ├── order-list.html                  ← Toàn bộ đơn thuê hệ thống
│       ├── complaint-list.html              ← Danh sách khiếu nại
│       ├── complaint-detail.html            ← Chi tiết & xử lý khiếu nại
│       ├── car-type.html                    ← Quản lý loại xe (CRUD)
│       ├── brand.html                       ← Quản lý hãng xe (CRUD)
│       └── region.html                      ← Quản lý khu vực (CRUD)
│
└── js/                                      ← JS gọi API cho từng trang
    ├── auth/
    │   ├── login.js                         ← Gọi POST /api/auth/login, lưu token
    │   ├── register.js                      ← Gọi POST /api/auth/register
    │   └── forgot-password.js              ← Gọi POST /api/auth/forgot-password
    │
    ├── guest/
    │   ├── car-list.js                      ← Gọi GET /api/cars với params lọc
    │   └── car-detail.js                    ← Gọi GET /api/cars/{id}, render lịch & review
    │
    ├── customer/
    │   ├── profile.js                       ← Gọi GET/PUT /api/users/me
    │   ├── identity-verification.js         ← Gọi POST /api/identity (upload CCCD+GPLX)
    │   ├── owner-registration.js            ← Gọi POST /api/owner-registration
    │   ├── create-order.js                  ← Gọi POST /api/orders
    │   ├── payment.js                       ← Gọi POST /api/payments
    │   ├── order-list.js                    ← Gọi GET /api/orders
    │   ├── order-detail.js                  ← Gọi GET /api/orders/{id}
    │   ├── pickup-confirm.js                ← Gọi PUT /api/orders/{id}/pickup
    │   ├── return-confirm.js                ← Gọi PUT /api/orders/{id}/return
    │   ├── review.js                        ← Gọi POST /api/reviews
    │   ├── transaction-list.js              ← Gọi GET /api/payments
    │   ├── transaction-detail.js            ← Gọi GET /api/payments/{id}
    │   └── incident-report.js               ← Gọi POST /api/complaints
    │
    ├── owner/
    │   ├── car-list.js                      ← Gọi GET /api/owner/cars
    │   ├── car-add.js                       ← Gọi POST /api/owner/cars
    │   ├── car-edit.js                      ← Gọi GET/PUT /api/owner/cars/{id}
    │   ├── car-schedule.js                  ← Gọi GET/PUT /api/owner/cars/{id}/schedule
    │   ├── order-list.js                    ← Gọi GET /api/orders?role=owner
    │   ├── order-detail.js                  ← Gọi GET/PUT /api/orders/{id}
    │   ├── revenue-stats.js                 ← Gọi GET /api/owner/stats/revenue
    │   ├── rental-history.js                ← Gọi GET /api/owner/stats/history
    │   └── complaint.js                     ← Gọi POST /api/complaints
    │
    └── admin/
        ├── dashboard.js                     ← Gọi GET /api/admin/dashboard (Chart.js)
        ├── user-list.js                     ← Gọi GET /api/admin/users
        ├── user-detail.js                   ← Gọi GET/PUT /api/admin/users/{id}
        ├── identity-review.js               ← Gọi PUT /api/admin/identity/{id}
        ├── owner-approval.js                ← Gọi PUT /api/admin/owner-registration/{id}
        ├── car-pending.js                   ← Gọi GET /api/admin/cars/pending
        ├── car-review.js                    ← Gọi PUT /api/admin/cars/{id}/approve
        ├── order-list.js                    ← Gọi GET /api/admin/orders
        ├── complaint-list.js                ← Gọi GET /api/admin/complaints
        ├── complaint-detail.js              ← Gọi PUT /api/admin/complaints/{id}
        ├── car-type.js                      ← Gọi CRUD /api/admin/categories/car-types
        ├── brand.js                         ← Gọi CRUD /api/admin/categories/brands
        └── region.js                        ← Gọi CRUD /api/admin/categories/regions
```

## 🔑 Thứ tự nên code

```
1. application.properties      → Cấu hình DB connection
2. Entity                       → Ánh xạ bảng DB
3. Repository                   → Truy vấn DB
4. config/Security + JWT        → Xác thực & phân quyền
5. DTO + Service + Controller   → Từng use case (Auth → User → Car → Order...)
6. Exception Handler            → Xử lý lỗi
7. Frontend HTML + JS           → Gọi API, render UI
```

---

> 💡 **Gợi ý:** Nên code theo thứ tự Use Case từ SRS:  
> **UC01 Auth** → **UC02 User** → **UC03 Car** → **UC04 Order** → **UC05 Payment** → **UC06 Review** → **UC07 Complaint** → **UC08 Category** → **UC09 Dashboard**
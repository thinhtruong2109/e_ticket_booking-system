# API Routes - E-Ticket Booking System

> **Base URL:** `http://localhost:8080`
>
> **Content-Type:** `application/json`
>
> **Authentication:** Các API cần xác thực phải gửi JWT token trong header:
> ```
> Authorization: Bearer <access_token>
> ```

---

## Mục lục

1. [Authentication](#1-authentication)
2. [Users](#2-users)
3. [Events](#3-events)
4. [Event Categories](#4-event-categories)
5. [Event Schedules](#5-event-schedules)
6. [Ticket Types](#6-ticket-types)
7. [Bookings](#7-bookings)
8. [Payments](#8-payments)
9. [Tickets](#9-tickets)
10. [Venues](#10-venues)
11. [Seats & Sections](#11-seats--sections)
12. [Promo Codes](#12-promo-codes)
13. [Ticket Listings & Exchanges](#13-ticket-listings--exchanges)
14. [Transaction Histories](#14-transaction-histories-lịch-sử-giao-dịch)
15. [Organizer E-Wallet](#15-organizer-e-wallet-ví-điện-tử-organizer)

---

## 1. Authentication

### 1.1. Đăng ký tài khoản

| | |
|---|---|
| **URL** | `POST /api/auth/register` |
| **Mô tả** | Tạo tài khoản mới cho người dùng. User sẽ ở trạng thái `INACTIVE` và nhận OTP qua email để xác thực |
| **Authorization** | ❌ Không cần |
| **Header** | `Content-Type: application/json` |

**Request Body:**
```json
{
  "email": "user@example.com",         // ✅ Bắt buộc, email hợp lệ
  "password": "Password123",            // ✅ Bắt buộc, tối thiểu 8 ký tự, có chữ hoa, chữ thường, số
  "fullName": "Nguyen Van A",           // ✅ Bắt buộc
  "phoneNumber": "0901234567"           // ❌ Không bắt buộc
}
```

**Response:** `AuthResponse` (accessToken, refreshToken, user info với status = INACTIVE)

> ⚠️ Sau khi đăng ký, hệ thống gửi OTP 6 số qua email. User cần gọi `POST /api/auth/verify-email` để xác nhận.

---

### 1.2. Xác nhận email (OTP)

| | |
|---|---|
| **URL** | `POST /api/auth/verify-email` |
| **Mô tả** | Xác nhận email bằng mã OTP nhận qua email. Chuyển user sang trạng thái `ACTIVE` |
| **Authorization** | ❌ Không cần |
| **Header** | `Content-Type: application/json` |

**Request Body:**
```json
{
  "email": "user@example.com",   // ✅ Bắt buộc
  "otp": "123456"                // ✅ Bắt buộc, mã OTP 6 số
}
```

**Response:**
```json
{
  "status": "success",
  "message": "Xác nhận email thành công"
}
```

**Lỗi có thể xảy ra:**
- OTP không hợp lệ hoặc đã hết hạn (400)
- Email không tồn tại (400)

---

### 1.3. Gửi lại OTP

| | |
|---|---|
| **URL** | `POST /api/auth/resend-otp` |
| **Mô tả** | Gửi lại mã OTP xác thực email. Chỉ dùng cho user chưa verify (status = INACTIVE) |
| **Authorization** | ❌ Không cần |
| **Header** | `Content-Type: application/json` |

**Request Body:**
```json
{
  "email": "user@example.com"    // ✅ Bắt buộc
}
```

**Response:**
```json
{
  "status": "success",
  "message": "Đã gửi lại OTP"
}
```

**Lỗi có thể xảy ra:**
- Email không tồn tại (400)
- Email đã được xác nhận (400)

---

### 1.4. Đăng nhập

| | |
|---|---|
| **URL** | `POST /api/auth/login` |
| **Mô tả** | Đăng nhập bằng email và password |
| **Authorization** | ❌ Không cần |
| **Header** | `Content-Type: application/json` |

**Request Body:**
```json
{
  "email": "user@example.com",     // ✅ Bắt buộc
  "password": "Password123"        // ✅ Bắt buộc
}
```

**Response:** `AuthResponse` (accessToken, refreshToken, user info)

---

### 1.5. Làm mới token

| | |
|---|---|
| **URL** | `POST /api/auth/refresh-token` |
| **Mô tả** | Lấy access token mới từ refresh token |
| **Authorization** | ❌ Không cần |
| **Header** | `Content-Type: application/json` |

**Request Body:**
```json
{
  "refreshToken": "eyJhbGciOi..."  // ✅ Bắt buộc
}
```

**Response:** `AuthResponse` (new accessToken, refreshToken, user info)

---

### 1.6. Đăng xuất

| | |
|---|---|
| **URL** | `POST /api/auth/logout` |
| **Mô tả** | Đăng xuất (client tự xóa token) |
| **Authorization** | ❌ Không cần |
| **Header** | — |
| **Request Body** | Không có |

---

## 2. Users

### 2.1. Xem profile cá nhân

| | |
|---|---|
| **URL** | `GET /api/users/me` |
| **Mô tả** | Lấy thông tin profile của user đang đăng nhập |
| **Authorization** | ✅ `Bearer <token>` |
| **Header** | `Authorization: Bearer <token>` |
| **Params** | Không có |
| **Request Body** | Không có |

---

### 2.2. Cập nhật profile

| | |
|---|---|
| **URL** | `PUT /api/users/me` |
| **Mô tả** | Cập nhật thông tin cá nhân |
| **Authorization** | ✅ `Bearer <token>` |
| **Header** | `Authorization: Bearer <token>`, `Content-Type: application/json` |

**Request Body:**
```json
{
  "fullName": "Nguyen Van B",      // ❌ Không bắt buộc
  "phoneNumber": "0909876543"      // ❌ Không bắt buộc
}
```

---

### 2.3. Đổi mật khẩu

| | |
|---|---|
| **URL** | `PUT /api/users/me/password` |
| **Mô tả** | Đổi mật khẩu cho user đang đăng nhập |
| **Authorization** | ✅ `Bearer <token>` |
| **Header** | `Authorization: Bearer <token>`, `Content-Type: application/json` |

**Request Body:**
```json
{
  "currentPassword": "OldPass123",     // ✅ Bắt buộc
  "newPassword": "NewPass456"          // ✅ Bắt buộc, tối thiểu 8 ký tự
}
```

---

### 2.4. Lấy danh sách tất cả users (Admin)

| | |
|---|---|
| **URL** | `GET /api/users` |
| **Mô tả** | Lấy danh sách tất cả users |
| **Authorization** | ✅ `Bearer <token>` |
| **Role** | 🔒 `ADMIN` |
| **Params** | Không có |
| **Request Body** | Không có |

---

### 2.5. Lấy users theo role (Admin)

| | |
|---|---|
| **URL** | `GET /api/users/role/{role}` |
| **Mô tả** | Lấy danh sách users theo role |
| **Authorization** | ✅ `Bearer <token>` |
| **Role** | 🔒 `ADMIN` |
| **Path Variable** | `role` — Tên role (VD: `CUSTOMER`, `ORGANIZER`, `ADMIN`, `STAFF`) |
| **Request Body** | Không có |

---

### 2.6. Ban user (Admin)

| | |
|---|---|
| **URL** | `PUT /api/users/{userId}/ban` |
| **Mô tả** | Cấm tài khoản user |
| **Authorization** | ✅ `Bearer <token>` |
| **Role** | 🔒 `ADMIN` |
| **Path Variable** | `userId` — ID của user cần ban |
| **Request Body** | Không có |

---

### 2.7. Unban user (Admin)

| | |
|---|---|
| **URL** | `PUT /api/users/{userId}/unban` |
| **Mô tả** | Gỡ cấm tài khoản user |
| **Authorization** | ✅ `Bearer <token>` |
| **Role** | 🔒 `ADMIN` |
| **Path Variable** | `userId` — ID của user cần unban |
| **Request Body** | Không có |

---

### 2.8. Đổi role user (Admin)

| | |
|---|---|
| **URL** | `PUT /api/users/{userId}/role` |
| **Mô tả** | Thay đổi role của user |
| **Authorization** | ✅ `Bearer <token>` |
| **Role** | 🔒 `ADMIN` |
| **Path Variable** | `userId` — ID của user |
| **Query Param** | `role` (String) — Role mới (VD: `ORGANIZER`, `STAFF`) |
| **Request Body** | Không có |

---

## 3. Events

### 3.1. Lấy danh sách sự kiện đã publish (Public)

| | |
|---|---|
| **URL** | `GET /api/events` |
| **Mô tả** | Lấy danh sách sự kiện đã PUBLISHED, có thể lọc theo category và tên |
| **Authorization** | ❌ Không cần |
| **Query Params** | `categoryId` (Long, optional) — Lọc theo category ID |
| | `name` (String, optional) — Tìm kiếm theo tên event |
| **Request Body** | Không có |

---

### 3.2. Xem chi tiết sự kiện (Public)

| | |
|---|---|
| **URL** | `GET /api/events/{id}` |
| **Mô tả** | Lấy thông tin chi tiết của một sự kiện |
| **Authorization** | ❌ Không cần |
| **Path Variable** | `id` — Event ID |
| **Request Body** | Không có |

---

### 3.3. Tạo sự kiện (Organizer/Admin)

| | |
|---|---|
| **URL** | `POST /api/events` |
| **Mô tả** | Tạo sự kiện mới (trạng thái DRAFT) |
| **Authorization** | ✅ `Bearer <token>` |
| **Role** | 🔒 `ORGANIZER` hoặc `ADMIN` |
| **Header** | `Authorization: Bearer <token>`, `Content-Type: application/json` |

**Request Body:**
```json
{
  "name": "Concert ABC",               // ✅ Bắt buộc
  "description": "Mô tả sự kiện...",   // ❌ Không bắt buộc
  "categoryId": 1,                      // ✅ Bắt buộc
  "venueId": 1,                         // ✅ Bắt buộc
  "bannerImageUrl": "https://...",      // ❌ Không bắt buộc
  "thumbnailImageUrl": "https://...",   // ❌ Không bắt buộc
  "totalTickets": 500,                  // ❌ Không bắt buộc (mặc định lấy từ venue capacity)
  "allowTicketExchange": true           // ❌ Không bắt buộc (mặc định: true)
}
```

> **Validation:** `totalTickets ≤ venue.totalCapacity` (nếu totalTickets được truyền)

---

### 3.4. Cập nhật sự kiện (Organizer/Admin)

| | |
|---|---|
| **URL** | `PUT /api/events/{id}` |
| **Mô tả** | Cập nhật thông tin sự kiện (chỉ organizer sở hữu mới được sửa) |
| **Authorization** | ✅ `Bearer <token>` |
| **Role** | 🔒 `ORGANIZER` hoặc `ADMIN` |
| **Path Variable** | `id` — Event ID |
| **Header** | `Authorization: Bearer <token>`, `Content-Type: application/json` |

**Request Body:** (tất cả field đều optional, chỉ gửi field cần update)
```json
{
  "name": "Concert XYZ Updated",
  "description": "Mô tả mới...",
  "categoryId": 2,
  "bannerImageUrl": "https://...",
  "thumbnailImageUrl": "https://...",
  "allowTicketExchange": false
}
```

---

### 3.5. Publish sự kiện (Organizer/Admin)

| | |
|---|---|
| **URL** | `PUT /api/events/{id}/publish` |
| **Mô tả** | Chuyển sự kiện từ DRAFT sang PUBLISHED |
| **Authorization** | ✅ `Bearer <token>` |
| **Role** | 🔒 `ORGANIZER` hoặc `ADMIN` |
| **Path Variable** | `id` — Event ID |
| **Request Body** | Không có |

---

### 3.6. Hủy sự kiện (Organizer/Admin)

| | |
|---|---|
| **URL** | `PUT /api/events/{id}/cancel` |
| **Mô tả** | Hủy sự kiện (CANCELLED) |
| **Authorization** | ✅ `Bearer <token>` |
| **Role** | 🔒 `ORGANIZER` hoặc `ADMIN` |
| **Path Variable** | `id` — Event ID |
| **Request Body** | Không có |

---

### 3.7. Lấy sự kiện của tôi (Organizer/Admin)

| | |
|---|---|
| **URL** | `GET /api/events/my-events` |
| **Mô tả** | Lấy danh sách sự kiện do organizer đang đăng nhập tạo |
| **Authorization** | ✅ `Bearer <token>` |
| **Role** | 🔒 `ORGANIZER` hoặc `ADMIN` |
| **Request Body** | Không có |

---

### 3.8. Lấy tất cả sự kiện (Admin)

| | |
|---|---|
| **URL** | `GET /api/events/all` |
| **Mô tả** | Lấy danh sách toàn bộ sự kiện (mọi trạng thái) |
| **Authorization** | ✅ `Bearer <token>` |
| **Role** | 🔒 `ADMIN` |
| **Request Body** | Không có |

---

## 4. Event Categories

### 4.1. Lấy tất cả categories (Public)

| | |
|---|---|
| **URL** | `GET /api/event-categories` |
| **Mô tả** | Lấy danh sách tất cả thể loại sự kiện |
| **Authorization** | ❌ Không cần |
| **Request Body** | Không có |

---

### 4.2. Xem chi tiết category (Public)

| | |
|---|---|
| **URL** | `GET /api/event-categories/{id}` |
| **Mô tả** | Lấy thông tin chi tiết một category |
| **Authorization** | ❌ Không cần |
| **Path Variable** | `id` — Category ID |
| **Request Body** | Không có |

---

### 4.3. Tạo category (Admin)

| | |
|---|---|
| **URL** | `POST /api/event-categories` |
| **Mô tả** | Tạo thể loại sự kiện mới |
| **Authorization** | ✅ `Bearer <token>` |
| **Role** | 🔒 `ADMIN` |
| **Header** | `Authorization: Bearer <token>`, `Content-Type: application/json` |

**Request Body:**
```json
{
  "name": "Âm nhạc",                   // ✅ Bắt buộc
  "description": "Các sự kiện âm nhạc", // ❌ Không bắt buộc
  "iconUrl": "https://..."              // ❌ Không bắt buộc
}
```

---

### 4.4. Cập nhật category (Admin)

| | |
|---|---|
| **URL** | `PUT /api/event-categories/{id}` |
| **Mô tả** | Cập nhật thể loại sự kiện |
| **Authorization** | ✅ `Bearer <token>` |
| **Role** | 🔒 `ADMIN` |
| **Path Variable** | `id` — Category ID |
| **Header** | `Authorization: Bearer <token>`, `Content-Type: application/json` |

**Request Body:** (giống Create, field nào gửi lên sẽ được cập nhật)
```json
{
  "name": "Thể thao",
  "description": "Các sự kiện thể thao",
  "iconUrl": "https://..."
}
```

---

### 4.5. Xóa category (Admin)

| | |
|---|---|
| **URL** | `DELETE /api/event-categories/{id}` |
| **Mô tả** | Xóa thể loại sự kiện |
| **Authorization** | ✅ `Bearer <token>` |
| **Role** | 🔒 `ADMIN` |
| **Path Variable** | `id` — Category ID |
| **Request Body** | Không có |

---

## 5. Event Schedules

### 5.1. Lấy lịch chiếu theo event (Public)

| | |
|---|---|
| **URL** | `GET /api/event-schedules` |
| **Mô tả** | Lấy tất cả lịch chiếu/biểu diễn của một sự kiện |
| **Authorization** | ❌ Không cần |
| **Query Param** | `eventId` (Long) ✅ Bắt buộc |
| **Request Body** | Không có |

---

### 5.2. Lấy lịch chiếu còn chỗ (Public)

| | |
|---|---|
| **URL** | `GET /api/event-schedules/available` |
| **Mô tả** | Lấy các lịch chiếu còn chỗ trống (status = SCHEDULED và còn seat) |
| **Authorization** | ❌ Không cần |
| **Query Param** | `eventId` (Long) ✅ Bắt buộc |
| **Request Body** | Không có |

---

### 5.3. Xem chi tiết lịch chiếu (Public)

| | |
|---|---|
| **URL** | `GET /api/event-schedules/{id}` |
| **Mô tả** | Lấy chi tiết một lịch chiếu |
| **Authorization** | ❌ Không cần |
| **Path Variable** | `id` — Schedule ID |
| **Request Body** | Không có |

---

### 5.4. Tạo lịch chiếu (Organizer/Admin)

| | |
|---|---|
| **URL** | `POST /api/event-schedules` |
| **Mô tả** | Tạo lịch chiếu mới cho sự kiện |
| **Authorization** | ✅ `Bearer <token>` |
| **Role** | 🔒 `ORGANIZER` hoặc `ADMIN` |
| **Header** | `Authorization: Bearer <token>`, `Content-Type: application/json` |

**Request Body:**
```json
{
  "eventId": 1,                                // ✅ Bắt buộc
  "startTime": "2026-03-15T19:00:00",          // ✅ Bắt buộc (ISO 8601)
  "endTime": "2026-03-15T22:00:00"             // ✅ Bắt buộc (ISO 8601)
}
```

---

### 5.5. Hủy lịch chiếu (Organizer/Admin)

| | |
|---|---|
| **URL** | `PUT /api/event-schedules/{id}/cancel` |
| **Mô tả** | Hủy một lịch chiếu |
| **Authorization** | ✅ `Bearer <token>` |
| **Role** | 🔒 `ORGANIZER` hoặc `ADMIN` |
| **Path Variable** | `id` — Schedule ID |
| **Request Body** | Không có |

---

## 6. Ticket Types

### 6.1. Lấy loại vé theo event (Public)

| | |
|---|---|
| **URL** | `GET /api/ticket-types` |
| **Mô tả** | Lấy tất cả loại vé của một sự kiện |
| **Authorization** | ❌ Không cần |
| **Query Param** | `eventId` (Long) ✅ Bắt buộc |
| **Request Body** | Không có |

---

### 6.2. Lấy loại vé còn hàng (Public)

| | |
|---|---|
| **URL** | `GET /api/ticket-types/available` |
| **Mô tả** | Lấy các loại vé còn số lượng (availableQuantity > 0) |
| **Authorization** | ❌ Không cần |
| **Query Param** | `eventId` (Long) ✅ Bắt buộc |
| **Request Body** | Không có |

---

### 6.3. Xem chi tiết loại vé (Public)

| | |
|---|---|
| **URL** | `GET /api/ticket-types/{id}` |
| **Mô tả** | Lấy thông tin chi tiết của một loại vé |
| **Authorization** | ❌ Không cần |
| **Path Variable** | `id` — Ticket Type ID |
| **Request Body** | Không có |

---

### 6.4. Tạo loại vé (Organizer/Admin)

| | |
|---|---|
| **URL** | `POST /api/ticket-types` |
| **Mô tả** | Tạo loại vé mới cho sự kiện |
| **Authorization** | ✅ `Bearer <token>` |
| **Role** | 🔒 `ORGANIZER` hoặc `ADMIN` |
| **Header** | `Authorization: Bearer <token>`, `Content-Type: application/json` |

**Request Body:**
```json
{
  "eventId": 1,                        // ✅ Bắt buộc
  "sectionId": 1,                      // ❌ Không bắt buộc (liên kết ticket type với section)
  "name": "VIP",                       // ✅ Bắt buộc
  "description": "Vé VIP hàng đầu",   // ❌ Không bắt buộc
  "price": 500000,                     // ✅ Bắt buộc, số dương
  "totalQuantity": 100,                // ✅ Bắt buộc, số dương
  "maxPerBooking": 5                   // ❌ Không bắt buộc (mặc định: 10)
}
```

> **Validation:**
> - Σ totalQuantity các ticket types (cùng event) ≤ event.totalTickets
> - Nếu có `sectionId`: totalQuantity ≤ section.capacity
> - Nếu section.hasNumberedSeats = true: section phải có seats đã tạo, và totalQuantity ≤ số seat thực tế

---

## 7. Bookings

### 7.1. Tạo booking

| | |
|---|---|
| **URL** | `POST /api/bookings` |
| **Mô tả** | Tạo đơn đặt vé mới (trạng thái PENDING, giữ chỗ 15 phút) |
| **Authorization** | ✅ `Bearer <token>` |
| **Header** | `Authorization: Bearer <token>`, `Content-Type: application/json` |

**Request Body:**
```json
{
  "eventId": 1,                        // ✅ Bắt buộc
  "scheduleId": 1,                     // ❌ Không bắt buộc
  "items": [                           // ✅ Bắt buộc, ít nhất 1 item
    {
      "ticketTypeId": 1,               // ✅ Bắt buộc
      "quantity": 2                    // ✅ Bắt buộc, số dương
    },
    {
      "ticketTypeId": 2,
      "quantity": 1
    }
  ],
  "seatIds": [10, 11, 12],            // ⚠️ Bắt buộc nếu ticket type có section.hasNumberedSeats = true
  "promoCodeId": 1                     // ❌ Không bắt buộc (ID promo code, áp dụng khi tạo booking)
}
```

> **Lưu ý về seatIds:** Nếu bất kỳ ticket type nào trong `items` thuộc section có `hasNumberedSeats = true`, thì `seatIds` **bắt buộc**. Số lượng seatIds phải bằng tổng quantity của các ticket types có numbered seats. Ghế phải available (chưa bị HOLDING/CONFIRMED) và thuộc đúng section. Cũng cần truyền `scheduleId` khi chọn ghế.

> **Lưu ý về promoCodeId:** Nếu có `promoCodeId`, hệ thống sẽ validate promo code (status ACTIVE, thời hạn, usage limit, min order amount) **và kiểm tra applicationType** (GLOBAL → tất cả event; ORGANIZER_ALL → event phải do đúng organizer tổ chức; SPECIFIC_EVENTS → event phải nằm trong danh sách PromoCodeEventJoin). Tính discount và tạo `BookingPromoCode` record. `usedCount` của promo code chỉ được tăng khi thanh toán thành công (payment callback SUCCESS).

---

### 7.2. Hủy booking

| | |
|---|---|
| **URL** | `DELETE /api/bookings/{id}` |
| **Mô tả** | Hủy đơn đặt vé |
| **Authorization** | ✅ `Bearer <token>` |
| **Path Variable** | `id` — Booking ID |
| **Request Body** | Không có |

---

### 7.3. Lấy danh sách booking của tôi

| | |
|---|---|
| **URL** | `GET /api/bookings/my-bookings` |
| **Mô tả** | Lấy tất cả booking của user đang đăng nhập |
| **Authorization** | ✅ `Bearer <token>` |
| **Request Body** | Không có |

---

### 7.4. Xem chi tiết booking

| | |
|---|---|
| **URL** | `GET /api/bookings/{id}` |
| **Mô tả** | Lấy chi tiết một booking (chỉ xem được booking của mình) |
| **Authorization** | ✅ `Bearer <token>` |
| **Path Variable** | `id` — Booking ID |
| **Request Body** | Không có |

---

## 8. Payments

### 8.1. Tạo thanh toán

| | |
|---|---|
| **URL** | `POST /api/payments` |
| **Mô tả** | Tạo giao dịch thanh toán cho booking |
| **Authorization** | ✅ `Bearer <token>` |
| **Header** | `Authorization: Bearer <token>`, `Content-Type: application/json` |

**Request Body:**
```json
{
  "bookingId": 1,                      // ✅ Bắt buộc
  "paymentMethod": "PAYOS"             // ✅ Bắt buộc (PAYOS)
}
```

---

### 8.2. Xem thanh toán theo booking

| | |
|---|---|
| **URL** | `GET /api/payments/booking/{bookingId}` |
| **Mô tả** | Lấy thông tin thanh toán của một booking |
| **Authorization** | ✅ `Bearer <token>` |
| **Path Variable** | `bookingId` — Booking ID |
| **Request Body** | Không có |

---

### 8.3. Lấy thông tin thanh toán PayOS (đồng bộ trạng thái)

| | |
|---|---|
| **URL** | `GET /api/payments/payos/{orderCode}` |
| **Mô tả** | Lấy thông tin thanh toán từ PayOS và đồng bộ trạng thái. Nếu PayOS báo PAID mà DB chưa SUCCESS → tự động cập nhật. Nếu CANCELLED mà DB còn PENDING → cập nhật CANCELLED |
| **Authorization** | ✅ `Bearer <token>` |
| **Path Variable** | `orderCode` — PayOS orderCode (long) |
| **Request Body** | Không có |

**Response:** `PaymentResponse` (trạng thái được đồng bộ từ PayOS)

---

### 8.4. Hủy thanh toán PayOS

| | |
|---|---|
| **URL** | `PUT /api/payments/payos/{orderCode}/cancel` |
| **Mô tả** | Hủy link thanh toán PayOS. Chỉ hủy được khi Payment status = PENDING |
| **Authorization** | ✅ `Bearer <token>` |
| **Path Variable** | `orderCode` — PayOS orderCode (long) |
| **Request Body** | Không có |

**Lỗi có thể xảy ra:**
- Payment not found (404)
- Only pending payments can be cancelled (400)
- Failed to cancel payment on PayOS (400)

---

### 8.5. PayOS Webhook

| | |
|---|---|
| **URL** | `POST /api/payments/payos/webhook` |
| **Mô tả** | Endpoint nhận webhook từ PayOS khi trạng thái thanh toán thay đổi. PayOS gửi POST tự động |
| **Authorization** | ❌ Không cần (xác thực bằng PayOS signature) |
| **Header** | — |

**Request Body (PayOS gửi):**
```json
{
  "code": "00",
  "desc": "success",
  "success": true,
  "data": {
    "orderCode": 123,
    "amount": 3000,
    "description": "DH-BK20260303001",
    "code": "00",
    "desc": "Thành công",
    "reference": "TF230204212323",
    "transactionDateTime": "2023-02-04 18:25:00",
    "paymentLinkId": "..."
  },
  "signature": "..."
}
```

> **Logic xử lý:**
> - Xác thực webhook signature qua PayOS SDK
> - `data.code = "00"` → thanh toán thành công → update Payment status = SUCCESS, confirm booking, generate tickets
> - `data.code != "00"` → thanh toán thất bại → update Payment status = FAILED
> - Luôn trả về HTTP 200 `{"success": true}` để PayOS không retry liên tục

---

### 8.6. PayOS Return URL (Success Redirect)

| | |
|---|---|
| **URL** | `GET /api/payments/payos/success` |
| **Mô tả** | URL PayOS redirect đến khi user thanh toán xong. Backend trả thông tin payment, frontend xử lý hiển thị |
| **Authorization** | ❌ Không cần |
| **Query Params** | `orderCode` (long) ✅ Bắt buộc |
| | `status` (String, optional) |
| **Request Body** | Không có |

---

### 8.7. PayOS Cancel URL (Cancel Redirect)

| | |
|---|---|
| **URL** | `GET /api/payments/payos/cancel` |
| **Mô tả** | URL PayOS redirect đến khi user hủy thanh toán trên trang PayOS |
| **Authorization** | ❌ Không cần |
| **Query Param** | `orderCode` (long) ✅ Bắt buộc |
| **Request Body** | Không có |

---

## 9. Tickets

### 9.1. Lấy vé của tôi

| | |
|---|---|
| **URL** | `GET /api/tickets/my-tickets` |
| **Mô tả** | Lấy tất cả vé mà user đang sở hữu |
| **Authorization** | ✅ `Bearer <token>` |
| **Request Body** | Không có |

---

### 9.2. Lấy vé theo booking

| | |
|---|---|
| **URL** | `GET /api/tickets/booking/{bookingId}` |
| **Mô tả** | Lấy danh sách vé thuộc một booking |
| **Authorization** | ✅ `Bearer <token>` |
| **Path Variable** | `bookingId` — Booking ID |
| **Request Body** | Không có |

---

### 9.3. Xem vé theo mã code

| | |
|---|---|
| **URL** | `GET /api/tickets/code/{ticketCode}` |
| **Mô tả** | Tìm vé theo ticket code |
| **Authorization** | ✅ `Bearer <token>` |
| **Path Variable** | `ticketCode` — Mã vé (VD: `TKT1A2B3C4D5E`) |
| **Request Body** | Không có |

---

### 9.4. Check-in vé (Staff/Organizer/Admin)

| | |
|---|---|
| **URL** | `POST /api/tickets/check-in` |
| **Mô tả** | Check-in vé tại cổng sự kiện |
| **Authorization** | ✅ `Bearer <token>` |
| **Role** | 🔒 `STAFF`, `ORGANIZER` hoặc `ADMIN` |
| **Header** | `Authorization: Bearer <token>`, `Content-Type: application/json` |

**Request Body:**
```json
{
  "ticketCode": "TKT1A2B3C4D5E",      // ✅ Bắt buộc
  "scheduleId": 1                      // ❌ Không bắt buộc (validate đúng lịch nếu cung cấp)
}
```

---

## 10. Venues

### 10.1. Lấy tất cả venues (Public)

| | |
|---|---|
| **URL** | `GET /api/venues` |
| **Mô tả** | Lấy danh sách tất cả địa điểm |
| **Authorization** | ❌ Không cần |
| **Request Body** | Không có |

---

### 10.2. Xem chi tiết venue (Public)

| | |
|---|---|
| **URL** | `GET /api/venues/{id}` |
| **Mô tả** | Lấy thông tin chi tiết của một venue |
| **Authorization** | ❌ Không cần |
| **Path Variable** | `id` — Venue ID |
| **Request Body** | Không có |

---

### 10.3. Tìm venue theo thành phố (Public)

| | |
|---|---|
| **URL** | `GET /api/venues/search` |
| **Mô tả** | Tìm kiếm venues theo tên thành phố |
| **Authorization** | ❌ Không cần |
| **Query Param** | `city` (String) ✅ Bắt buộc |
| **Request Body** | Không có |

---

### 10.4. Tạo venue (Organizer/Admin)

| | |
|---|---|
| **URL** | `POST /api/venues` |
| **Mô tả** | Tạo địa điểm tổ chức sự kiện mới |
| **Authorization** | ✅ `Bearer <token>` |
| **Role** | 🔒 `ORGANIZER` hoặc `ADMIN` |
| **Header** | `Authorization: Bearer <token>`, `Content-Type: application/json` |

**Request Body:**
```json
{
  "name": "Nhà hát lớn Hà Nội",       // ✅ Bắt buộc
  "address": "1 Tràng Tiền, Hoàn Kiếm", // ✅ Bắt buộc
  "city": "Hà Nội",                   // ❌ Không bắt buộc
  "country": "Việt Nam",              // ❌ Không bắt buộc
  "totalCapacity": 600,               // ❌ Không bắt buộc
  "hasSeatMap": true                   // ❌ Không bắt buộc (mặc định: false)
}
```

---

### 10.5. Cập nhật venue (Organizer/Admin)

| | |
|---|---|
| **URL** | `PUT /api/venues/{id}` |
| **Mô tả** | Cập nhật thông tin venue |
| **Authorization** | ✅ `Bearer <token>` |
| **Role** | 🔒 `ORGANIZER` hoặc `ADMIN` |
| **Path Variable** | `id` — Venue ID |
| **Header** | `Authorization: Bearer <token>`, `Content-Type: application/json` |

**Request Body:** (giống Create, field nào gửi sẽ được update)
```json
{
  "name": "Nhà hát lớn TP.HCM",
  "address": "7 Lam Sơn, Q.1",
  "city": "TP.HCM",
  "country": "Việt Nam",
  "totalCapacity": 800,
  "hasSeatMap": true
}
```

---

## 11. Seats & Sections

### 11.1. Tạo section (Organizer/Admin)

| | |
|---|---|
| **URL** | `POST /api/seats/sections` |
| **Mô tả** | Tạo khu vực ngồi trong venue |
| **Authorization** | ✅ `Bearer <token>` |
| **Role** | 🔒 `ORGANIZER` hoặc `ADMIN` |
| **Header** | `Authorization: Bearer <token>`, `Content-Type: application/json` |

**Request Body:**
```json
{
  "venueId": 1,                        // ✅ Bắt buộc
  "name": "Khu VIP",                   // ✅ Bắt buộc (unique trong cùng venue)
  "description": "Khu vực hạng sang",  // ❌ Không bắt buộc
  "capacity": 100,                     // ❌ Không bắt buộc
  "hasNumberedSeats": true             // ❌ Không bắt buộc
}
```

> **Validation:**
> - Tên section phải unique trong cùng venue
> - Σ capacity các sections ≤ venue.totalCapacity

---

### 11.2. Lấy sections theo venue (Public)

| | |
|---|---|
| **URL** | `GET /api/seats/sections/venue/{venueId}` |
| **Mô tả** | Lấy danh sách sections của venue |
| **Authorization** | ✅ `Bearer <token>` |
| **Path Variable** | `venueId` — Venue ID |
| **Request Body** | Không có |

---

### 11.3. Tạo ghế (Organizer/Admin)

| | |
|---|---|
| **URL** | `POST /api/seats` |
| **Mô tả** | Tạo ghế ngồi trong venue |
| **Authorization** | ✅ `Bearer <token>` |
| **Role** | 🔒 `ORGANIZER` hoặc `ADMIN` |
| **Header** | `Authorization: Bearer <token>`, `Content-Type: application/json` |

**Request Body:**
```json
{
  "venueId": 1,                        // ✅ Bắt buộc
  "sectionId": 1,                      // ❌ Không bắt buộc
  "rowNumber": "A",                    // ❌ Không bắt buộc
  "seatNumber": "A01",                 // ✅ Bắt buộc
  "seatType": "VIP"                    // ❌ Không bắt buộc (VIP, REGULAR, WHEELCHAIR; mặc định: REGULAR)
}
```

> **Validation:** Số seat trong section ≤ section.capacity (áp dụng cho cả single và bulk create)

---

### 11.4. Lấy ghế theo venue

| | |
|---|---|
| **URL** | `GET /api/seats/venue/{venueId}` |
| **Mô tả** | Lấy tất cả ghế của venue |
| **Authorization** | ✅ `Bearer <token>` |
| **Path Variable** | `venueId` — Venue ID |
| **Request Body** | Không có |

---

### 11.5. Lấy ghế trống theo lịch chiếu

| | |
|---|---|
| **URL** | `GET /api/seats/available` |
| **Mô tả** | Lấy danh sách ghế kèm trạng thái available/reserved theo lịch chiếu |
| **Authorization** | ✅ `Bearer <token>` |
| **Query Param** | `scheduleId` (Long) ✅ Bắt buộc |
| **Request Body** | Không có |

---

## 12. Promo Codes

> ⚠️ **Promo codes được phân quyền theo role:**
> - **ADMIN:** CRUD tất cả promo codes, chỉ tạo loại `GLOBAL` (áp dụng toàn hệ thống)
> - **ORGANIZER:** CRUD promo codes **do mình tạo**, tạo loại `ORGANIZER_ALL` hoặc `SPECIFIC_EVENTS`
> - **Authenticated users:** Xem promo codes khả dụng cho đơn hàng (`POST /available`)

### 12.1. [ADMIN] Tạo mã giảm giá

| | |
|---|---|
| **URL** | `POST /api/promo-codes/admin` |
| **Mô tả** | Admin tạo promo code GLOBAL (áp dụng tất cả event) |
| **Authorization** | ✅ `Bearer <token>` |
| **Role** | 🔒 `ADMIN` |
| **Header** | `Authorization: Bearer <token>`, `Content-Type: application/json` |

**Request Body:**
```json
{
  "code": "SALE2026",                      // ✅ Bắt buộc (sẽ tự chuyển UPPERCASE)
  "description": "Giảm giá Tết 2026",     // ❌ Không bắt buộc
  "discountType": "PERCENTAGE",            // ✅ Bắt buộc (PERCENTAGE hoặc FIXED_AMOUNT)
  "discountValue": 20,                     // ✅ Bắt buộc, số dương
  "minOrderAmount": 100000,                // ❌ Không bắt buộc
  "maxDiscountAmount": 500000,             // ❌ Không bắt buộc
  "usageLimit": 100,                       // ❌ Không bắt buộc
  "validFrom": "2026-01-01T00:00:00",      // ✅ Bắt buộc (ISO 8601)
  "validTo": "2026-12-31T23:59:59",        // ✅ Bắt buộc (ISO 8601)
  "applicationType": "GLOBAL"              // ✅ Bắt buộc (Admin chỉ được tạo GLOBAL)
}
```

> **Lưu ý:** Admin chỉ được tạo `applicationType = GLOBAL`. Nếu truyền loại khác sẽ trả lỗi 400.

---

### 12.2. [ADMIN] Lấy tất cả promo codes

| | |
|---|---|
| **URL** | `GET /api/promo-codes/admin` |
| **Mô tả** | Lấy danh sách tất cả mã giảm giá (bao gồm cả của Organizer) |
| **Authorization** | ✅ `Bearer <token>` |
| **Role** | 🔒 `ADMIN` |
| **Request Body** | Không có |

---

### 12.3. [ADMIN] Lấy promo codes đang hoạt động

| | |
|---|---|
| **URL** | `GET /api/promo-codes/admin/active` |
| **Mô tả** | Lấy danh sách mã giảm giá đang ACTIVE |
| **Authorization** | ✅ `Bearer <token>` |
| **Role** | 🔒 `ADMIN` |
| **Request Body** | Không có |

---

### 12.4. [ADMIN] Xem chi tiết promo code

| | |
|---|---|
| **URL** | `GET /api/promo-codes/admin/{id}` |
| **Mô tả** | Lấy thông tin chi tiết một mã giảm giá bất kỳ |
| **Authorization** | ✅ `Bearer <token>` |
| **Role** | 🔒 `ADMIN` |
| **Path Variable** | `id` — Promo Code ID |
| **Request Body** | Không có |

---

### 12.5. [ADMIN] Cập nhật promo code

| | |
|---|---|
| **URL** | `PUT /api/promo-codes/admin/{id}` |
| **Mô tả** | Cập nhật thông tin promo code bất kỳ |
| **Authorization** | ✅ `Bearer <token>` |
| **Role** | 🔒 `ADMIN` |
| **Path Variable** | `id` — Promo Code ID |

**Request Body:** Giống request tạo (12.1)

---

### 12.6. [ADMIN] Vô hiệu hóa promo code

| | |
|---|---|
| **URL** | `PUT /api/promo-codes/admin/{id}/deactivate` |
| **Mô tả** | Chuyển mã giảm giá sang trạng thái DISABLED |
| **Authorization** | ✅ `Bearer <token>` |
| **Role** | 🔒 `ADMIN` |
| **Path Variable** | `id` — Promo Code ID |
| **Request Body** | Không có |

---

### 12.7. [ORGANIZER] Tạo mã giảm giá

| | |
|---|---|
| **URL** | `POST /api/promo-codes/organizer` |
| **Mô tả** | Organizer tạo promo code cho events của mình |
| **Authorization** | ✅ `Bearer <token>` |
| **Role** | 🔒 `ORGANIZER` |
| **Header** | `Authorization: Bearer <token>`, `Content-Type: application/json` |

**Request Body:**
```json
{
  "code": "MYEVENT20",
  "description": "Giảm 20% cho event của tôi",
  "discountType": "PERCENTAGE",
  "discountValue": 20,
  "minOrderAmount": 100000,
  "maxDiscountAmount": 500000,
  "usageLimit": 50,
  "validFrom": "2026-01-01T00:00:00",
  "validTo": "2026-06-30T23:59:59",
  "applicationType": "SPECIFIC_EVENTS",    // ✅ Bắt buộc (ORGANIZER_ALL hoặc SPECIFIC_EVENTS)
  "eventIds": [1, 3, 5]                    // ⚠️ Bắt buộc nếu SPECIFIC_EVENTS (events phải thuộc organizer)
}
```

> **Lưu ý:**
> - `ORGANIZER_ALL`: Áp dụng cho tất cả events của organizer hiện tại, không cần `eventIds`
> - `SPECIFIC_EVENTS`: Phải truyền `eventIds`, hệ thống validate tất cả events thuộc organizer
> - Organizer KHÔNG được tạo `GLOBAL` (trả lỗi 400)

---

### 12.8. [ORGANIZER] Lấy promo codes của mình

| | |
|---|---|
| **URL** | `GET /api/promo-codes/organizer` |
| **Mô tả** | Lấy danh sách promo codes do organizer hiện tại tạo |
| **Authorization** | ✅ `Bearer <token>` |
| **Role** | 🔒 `ORGANIZER` |
| **Request Body** | Không có |

---

### 12.9. [ORGANIZER] Xem chi tiết promo code của mình

| | |
|---|---|
| **URL** | `GET /api/promo-codes/organizer/{id}` |
| **Mô tả** | Lấy thông tin promo code (chỉ của mình, trả 403 nếu không phải) |
| **Authorization** | ✅ `Bearer <token>` |
| **Role** | 🔒 `ORGANIZER` |
| **Path Variable** | `id` — Promo Code ID |
| **Request Body** | Không có |

---

### 12.10. [ORGANIZER] Cập nhật promo code của mình

| | |
|---|---|
| **URL** | `PUT /api/promo-codes/organizer/{id}` |
| **Mô tả** | Cập nhật promo code (chỉ của mình) |
| **Authorization** | ✅ `Bearer <token>` |
| **Role** | 🔒 `ORGANIZER` |
| **Path Variable** | `id` — Promo Code ID |

**Request Body:** Giống request tạo (12.7)

---

### 12.11. [ORGANIZER] Vô hiệu hóa promo code của mình

| | |
|---|---|
| **URL** | `PUT /api/promo-codes/organizer/{id}/deactivate` |
| **Mô tả** | Chuyển promo code sang DISABLED (chỉ của mình) |
| **Authorization** | ✅ `Bearer <token>` |
| **Role** | 🔒 `ORGANIZER` |
| **Path Variable** | `id` — Promo Code ID |
| **Request Body** | Không có |

---

### 12.12. Xem promo codes khả dụng cho đơn hàng

| | |
|---|---|
| **URL** | `POST /api/promo-codes/available` |
| **Mô tả** | Lấy danh sách promo code có thể áp dụng cho đơn hàng, kèm preview số tiền giảm |
| **Authorization** | ✅ `Bearer <token>` |
| **Role** | 🔓 Bất kỳ user đã đăng nhập |
| **Header** | `Authorization: Bearer <token>`, `Content-Type: application/json` |

**Request Body:**
```json
{
  "eventId": 1,                        // ✅ Bắt buộc
  "items": [                           // ✅ Bắt buộc, ít nhất 1 item
    {
      "ticketTypeId": 1,               // ✅ Bắt buộc
      "quantity": 2                    // ✅ Bắt buộc, số dương
    }
  ]
}
```

**Response:** `AvailablePromoResponse`
```json
{
  "totalAmount": 1000000,
  "availablePromoCodes": [
    {
      "id": 1,
      "code": "SALE2026",
      "description": "Giảm giá Tết 2026",
      "applicationType": "GLOBAL",
      "discountAmount": 200000,
      "finalAmount": 800000
    },
    {
      "id": 5,
      "code": "MYEVENT20",
      "description": "Giảm 20% cho event này",
      "applicationType": "SPECIFIC_EVENTS",
      "discountAmount": 200000,
      "finalAmount": 800000
    }
  ]
}
```

> **Logic lọc promo codes khả dụng:**
> 1. `GLOBAL`: Áp dụng tất cả events → luôn hiển thị nếu ACTIVE, còn hạn, còn lượt, đủ minOrderAmount
> 2. `ORGANIZER_ALL`: Chỉ hiển thị nếu `createdBy.id == event.organizer.id`
> 3. `SPECIFIC_EVENTS`: Chỉ hiển thị nếu event đang đặt nằm trong bảng PromoCodeEventJoin
>
> Promo code của Organizer khác tạo cho event khác sẽ **không bao giờ** hiển thị.

---

## 13. Ticket Listings & Exchanges

### 13.1. Lấy danh sách rao bán vé (Public)

| | |
|---|---|
| **URL** | `GET /api/ticket-listings` |
| **Mô tả** | Lấy tất cả listing đang FOR_SALE |
| **Authorization** | ❌ Không cần |
| **Request Body** | Không có |

---

### 13.2. Xem chi tiết listing (Public)

| | |
|---|---|
| **URL** | `GET /api/ticket-listings/{id}` |
| **Mô tả** | Lấy thông tin chi tiết một listing |
| **Authorization** | ❌ Không cần |
| **Path Variable** | `id` — Listing ID |
| **Request Body** | Không có |

---

### 13.3. Lấy listing của tôi

| | |
|---|---|
| **URL** | `GET /api/ticket-listings/my-listings` |
| **Mô tả** | Lấy danh sách các vé mà user đang rao bán |
| **Authorization** | ✅ `Bearer <token>` |
| **Request Body** | Không có |

---

### 13.4. Đăng rao bán vé

| | |
|---|---|
| **URL** | `POST /api/ticket-listings` |
| **Mô tả** | Đăng rao bán/trao đổi vé trên marketplace |
| **Authorization** | ✅ `Bearer <token>` |
| **Header** | `Authorization: Bearer <token>`, `Content-Type: application/json` |

**Request Body:**
```json
{
  "ticketId": 1,                       // ✅ Bắt buộc
  "listingPrice": 600000,              // ✅ Bắt buộc
  "exchangeType": "SELL",              // ❌ Không bắt buộc (SELL, TRADE, BOTH; mặc định: SELL)
  "description": "Bán gấp vé VIP",    // ❌ Không bắt buộc
  "expiresAt": "2026-03-10T23:59:59"   // ❌ Không bắt buộc (ISO 8601)
}
```

---

### 13.5. Hủy rao bán

| | |
|---|---|
| **URL** | `DELETE /api/ticket-listings/{id}` |
| **Mô tả** | Hủy listing rao bán vé (chỉ chủ sở hữu) |
| **Authorization** | ✅ `Bearer <token>` |
| **Path Variable** | `id` — Listing ID |
| **Request Body** | Không có |

---

### 13.6. Tạo yêu cầu mua/trao đổi vé

| | |
|---|---|
| **URL** | `POST /api/ticket-listings/exchanges` |
| **Mô tả** | Tạo yêu cầu mua hoặc trao đổi vé từ listing |
| **Authorization** | ✅ `Bearer <token>` |
| **Header** | `Authorization: Bearer <token>`, `Content-Type: application/json` |

**Request Body:**
```json
{
  "ticketListingId": 1,                // ✅ Bắt buộc
  "transactionType": "PURCHASE",       // ❌ Không bắt buộc (PURCHASE, TRADE; mặc định: PURCHASE)
  "tradeTicketId": null,               // ❌ Không bắt buộc (bắt buộc nếu TRADE)
  "paymentMethod": "VNPAY"             // ❌ Không bắt buộc (VNPAY, MOMO, STRIPE — dùng cho PURCHASE)
}
```

---

### 13.7. Hoàn thành giao dịch trao đổi

| | |
|---|---|
| **URL** | `PUT /api/ticket-listings/exchanges/{id}/complete` |
| **Mô tả** | Xác nhận hoàn thành giao dịch, chuyển quyền sở hữu vé |
| **Authorization** | ✅ `Bearer <token>` |
| **Path Variable** | `id` — Exchange ID |
| **Request Body** | Không có |

---

### 13.8. Hủy giao dịch trao đổi

| | |
|---|---|
| **URL** | `DELETE /api/ticket-listings/exchanges/{id}` |
| **Mô tả** | Hủy giao dịch trao đổi vé |
| **Authorization** | ✅ `Bearer <token>` |
| **Path Variable** | `id` — Exchange ID |
| **Request Body** | Không có |

---

## 14. Transaction Histories (Lịch sử giao dịch)

### 14.1. Lấy lịch sử giao dịch của tôi

| | |
|---|---|
| **URL** | `GET /api/transaction-histories/my-transactions` |
| **Mô tả** | Lấy toàn bộ lịch sử giao dịch của user đang đăng nhập |
| **Authorization** | ✅ `Bearer <token>` |
| **Query Params** | `type` (String, optional) — Lọc theo loại: `PAYMENT`, `REFUND`, `EXCHANGE_PAYMENT`, `EXCHANGE_REFUND` |
| **Request Body** | Không có |

**Response:** `List<TransactionHistoryResponse>`
```json
[
  {
    "id": 1,
    "paymentId": 10,
    "userId": 5,
    "userFullName": "Nguyen Van A",
    "transactionType": "PAYMENT",
    "status": "SUCCESS",
    "amount": 500000,
    "description": "Thanh toán thành công booking BK20260303001",
    "paymentMethod": "PAYOS",
    "bookingId": 1,
    "bookingCode": "BK20260303001",
    "createdAt": "2026-03-03T10:30:00"
  }
]
```

---

### 14.2. Lấy lịch sử giao dịch theo booking

| | |
|---|---|
| **URL** | `GET /api/transaction-histories/booking/{bookingId}` |
| **Mô tả** | Lấy lịch sử giao dịch liên quan đến một booking cụ thể |
| **Authorization** | ✅ `Bearer <token>` |
| **Path Variable** | `bookingId` — Booking ID |
| **Request Body** | Không có |

---

### 14.3. Lấy lịch sử giao dịch theo payment

| | |
|---|---|
| **URL** | `GET /api/transaction-histories/payment/{paymentId}` |
| **Mô tả** | Lấy lịch sử thay đổi trạng thái của một payment |
| **Authorization** | ✅ `Bearer <token>` |
| **Path Variable** | `paymentId` — Payment ID |
| **Request Body** | Không có |

---

### 14.4. [ADMIN] Lấy tất cả lịch sử giao dịch

| | |
|---|---|
| **URL** | `GET /api/transaction-histories/admin` |
| **Mô tả** | Lấy toàn bộ lịch sử giao dịch trong hệ thống |
| **Authorization** | ✅ `Bearer <token>` |
| **Role** | 🔒 `ADMIN` |
| **Request Body** | Không có |

---

### 14.5. [ADMIN] Lấy lịch sử giao dịch của user bất kỳ

| | |
|---|---|
| **URL** | `GET /api/transaction-histories/admin/user/{userId}` |
| **Mô tả** | Lấy lịch sử giao dịch của một user cụ thể |
| **Authorization** | ✅ `Bearer <token>` |
| **Role** | 🔒 `ADMIN` |
| **Path Variable** | `userId` — User ID |
| **Request Body** | Không có |

---

## 15. Organizer E-Wallet (Ví điện tử Organizer)

### 15.1. Lấy thông tin ví (Organizer)

| | |
|---|---|
| **URL** | `GET /api/organizer/wallet` |
| **Mô tả** | Lấy thông tin ví của organizer đang đăng nhập. Nếu chưa có ví → tự động tạo mới |
| **Authorization** | ✅ `Bearer <token>` |
| **Role** | 🔒 `ORGANIZER` / `ADMIN` |
| **Request Body** | Không có |

**Response:** `OrganizerEWalletResponse`
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "id": 1,
    "userId": 5,
    "userFullName": "Nguyen Organizer",
    "balance": 1500000.00,
    "totalWithdrawn": 500000.00,
    "bankName": "Vietcombank",
    "bankAccountNumber": "1234567890",
    "bankAccountHolder": "NGUYEN VAN A",
    "createdAt": "2026-03-01T10:00:00",
    "updatedAt": "2026-03-04T15:30:00"
  }
}
```

---

### 15.2. Cập nhật thông tin ngân hàng (Organizer)

| | |
|---|---|
| **URL** | `PUT /api/organizer/wallet/bank-info` |
| **Mô tả** | Cập nhật thông tin ngân hàng cho ví organizer. Bắt buộc trước khi rút tiền |
| **Authorization** | ✅ `Bearer <token>` |
| **Role** | 🔒 `ORGANIZER` / `ADMIN` |

**Request Body:**
```json
{
  "bankName": "Vietcombank",              // ✅ Bắt buộc
  "bankAccountNumber": "1234567890",      // ✅ Bắt buộc
  "bankAccountHolder": "NGUYEN VAN A"     // ✅ Bắt buộc
}
```

**Response:** `OrganizerEWalletResponse` (thông tin ví đã cập nhật)

---

### 15.3. Yêu cầu rút tiền (Organizer)

| | |
|---|---|
| **URL** | `POST /api/organizer/wallet/withdraw` |
| **Mô tả** | Rút tiền từ ví organizer về tài khoản ngân hàng |
| **Authorization** | ✅ `Bearer <token>` |
| **Role** | 🔒 `ORGANIZER` / `ADMIN` |

**Request Body:**
```json
{
  "amount": 500000     // ✅ Bắt buộc, tối thiểu 10,000 VND
}
```

**Response:** `WalletTransactionResponse`
```json
{
  "success": true,
  "message": "Withdrawal successful",
  "data": {
    "id": 10,
    "walletId": 1,
    "transactionType": "WITHDRAWAL",
    "amount": 500000.00,
    "balanceAfter": 1000000.00,
    "description": "Rút tiền về Vietcombank - 1234567890",
    "referenceCode": null,
    "status": "SUCCESS",
    "createdAt": "2026-03-04T16:00:00"
  }
}
```

**Lỗi có thể xảy ra:**
- Chưa cập nhật thông tin ngân hàng (400)
- Số dư không đủ (400)
- Số tiền rút < 10,000 VND (400)

---

### 15.4. Lấy lịch sử giao dịch ví (Organizer)

| | |
|---|---|
| **URL** | `GET /api/organizer/wallet/transactions` |
| **Mô tả** | Lấy lịch sử giao dịch ví của organizer đang đăng nhập |
| **Authorization** | ✅ `Bearer <token>` |
| **Role** | 🔒 `ORGANIZER` / `ADMIN` |
| **Query Params** | `type` (String, optional) — Lọc theo loại: `REVENUE`, `WITHDRAWAL`, `REFUND_DEDUCTION` |

**Response:** `List<WalletTransactionResponse>`
```json
[
  {
    "id": 10,
    "walletId": 1,
    "transactionType": "REVENUE",
    "amount": 500000.00,
    "balanceAfter": 1500000.00,
    "description": "Doanh thu từ booking BK20260304001",
    "referenceCode": "BK20260304001",
    "status": "SUCCESS",
    "createdAt": "2026-03-04T14:00:00"
  },
  {
    "id": 9,
    "walletId": 1,
    "transactionType": "WITHDRAWAL",
    "amount": 200000.00,
    "balanceAfter": 1000000.00,
    "description": "Rút tiền về Vietcombank - 1234567890",
    "referenceCode": null,
    "status": "SUCCESS",
    "createdAt": "2026-03-03T10:00:00"
  }
]
```

---

### 15.5. [ADMIN] Lấy tất cả ví organizer

| | |
|---|---|
| **URL** | `GET /api/organizer/wallet/admin/all` |
| **Mô tả** | Lấy danh sách tất cả ví organizer trong hệ thống |
| **Authorization** | ✅ `Bearer <token>` |
| **Role** | 🔒 `ADMIN` |
| **Request Body** | Không có |

**Response:** `List<OrganizerEWalletResponse>`

---

### 15.6. [ADMIN] Lấy ví của organizer bất kỳ

| | |
|---|---|
| **URL** | `GET /api/organizer/wallet/admin/user/{userId}` |
| **Mô tả** | Lấy thông tin ví của organizer theo userId |
| **Authorization** | ✅ `Bearer <token>` |
| **Role** | 🔒 `ADMIN` |
| **Path Variable** | `userId` — User ID của organizer |

**Response:** `OrganizerEWalletResponse`

---

### 15.7. [ADMIN] Lấy lịch sử giao dịch ví của organizer bất kỳ

| | |
|---|---|
| **URL** | `GET /api/organizer/wallet/admin/user/{userId}/transactions` |
| **Mô tả** | Lấy lịch sử giao dịch ví của organizer bất kỳ theo userId |
| **Authorization** | ✅ `Bearer <token>` |
| **Role** | 🔒 `ADMIN` |
| **Path Variable** | `userId` — User ID của organizer |

**Response:** `List<WalletTransactionResponse>`

---

## Tổng hợp nhanh (93 endpoints)

| # | Method | URL | Auth | Role |
|---|--------|-----|------|------|
| 1 | POST | `/api/auth/register` | ❌ | — |
| 2 | POST | `/api/auth/verify-email` | ❌ | — |
| 3 | POST | `/api/auth/resend-otp` | ❌ | — |
| 4 | POST | `/api/auth/login` | ❌ | — |
| 5 | POST | `/api/auth/refresh-token` | ❌ | — |
| 6 | POST | `/api/auth/logout` | ❌ | — |
| 7 | GET | `/api/users/me` | ✅ | Any |
| 8 | PUT | `/api/users/me` | ✅ | Any |
| 9 | PUT | `/api/users/me/password` | ✅ | Any |
| 10 | GET | `/api/users` | ✅ | ADMIN |
| 11 | GET | `/api/users/role/{role}` | ✅ | ADMIN |
| 12 | PUT | `/api/users/{userId}/ban` | ✅ | ADMIN |
| 13 | PUT | `/api/users/{userId}/unban` | ✅ | ADMIN |
| 14 | PUT | `/api/users/{userId}/role?role=` | ✅ | ADMIN |
| 15 | GET | `/api/events` | ❌ | — |
| 16 | GET | `/api/events/{id}` | ❌ | — |
| 17 | POST | `/api/events` | ✅ | ORGANIZER/ADMIN |
| 18 | PUT | `/api/events/{id}` | ✅ | ORGANIZER/ADMIN |
| 19 | PUT | `/api/events/{id}/publish` | ✅ | ORGANIZER/ADMIN |
| 20 | PUT | `/api/events/{id}/cancel` | ✅ | ORGANIZER/ADMIN |
| 21 | GET | `/api/events/my-events` | ✅ | ORGANIZER/ADMIN |
| 22 | GET | `/api/events/all` | ✅ | ADMIN |
| 23 | GET | `/api/event-categories` | ❌ | — |
| 24 | GET | `/api/event-categories/{id}` | ❌ | — |
| 25 | POST | `/api/event-categories` | ✅ | ADMIN |
| 26 | PUT | `/api/event-categories/{id}` | ✅ | ADMIN |
| 27 | DELETE | `/api/event-categories/{id}` | ✅ | ADMIN |
| 28 | GET | `/api/event-schedules?eventId=` | ❌ | — |
| 29 | GET | `/api/event-schedules/available?eventId=` | ❌ | — |
| 30 | GET | `/api/event-schedules/{id}` | ❌ | — |
| 31 | POST | `/api/event-schedules` | ✅ | ORGANIZER/ADMIN |
| 32 | PUT | `/api/event-schedules/{id}/cancel` | ✅ | ORGANIZER/ADMIN |
| 33 | GET | `/api/ticket-types?eventId=` | ❌ | — |
| 34 | GET | `/api/ticket-types/available?eventId=` | ❌ | — |
| 35 | GET | `/api/ticket-types/{id}` | ❌ | — |
| 36 | POST | `/api/ticket-types` | ✅ | ORGANIZER/ADMIN |
| 37 | POST | `/api/bookings` | ✅ | Any |
| 38 | DELETE | `/api/bookings/{id}` | ✅ | Any |
| 39 | GET | `/api/bookings/my-bookings` | ✅ | Any |
| 40 | GET | `/api/bookings/{id}` | ✅ | Any |
| 41 | POST | `/api/payments` | ✅ | Any |
| 42 | GET | `/api/payments/booking/{bookingId}` | ✅ | Any |
| 43 | GET | `/api/payments/payos/{orderCode}` | ✅ | Any |
| 44 | PUT | `/api/payments/payos/{orderCode}/cancel` | ✅ | Any |
| 45 | POST | `/api/payments/payos/webhook` | ❌ | — |
| 46 | GET | `/api/payments/payos/success` | ❌ | — |
| 47 | GET | `/api/payments/payos/cancel` | ❌ | — |
| 48 | GET | `/api/tickets/my-tickets` | ✅ | Any |
| 49 | GET | `/api/tickets/booking/{bookingId}` | ✅ | Any |
| 50 | GET | `/api/tickets/code/{ticketCode}` | ✅ | Any |
| 51 | POST | `/api/tickets/check-in` | ✅ | STAFF/ORGANIZER/ADMIN |
| 52 | GET | `/api/venues` | ❌ | — |
| 53 | GET | `/api/venues/{id}` | ❌ | — |
| 54 | GET | `/api/venues/search?city=` | ❌ | — |
| 55 | POST | `/api/venues` | ✅ | ORGANIZER/ADMIN |
| 56 | PUT | `/api/venues/{id}` | ✅ | ORGANIZER/ADMIN |
| 57 | POST | `/api/seats/sections` | ✅ | ORGANIZER/ADMIN |
| 58 | GET | `/api/seats/sections/venue/{venueId}` | ✅ | Any |
| 59 | POST | `/api/seats` | ✅ | ORGANIZER/ADMIN |
| 60 | GET | `/api/seats/venue/{venueId}` | ✅ | Any |
| 61 | GET | `/api/seats/available?scheduleId=` | ✅ | Any |
| 62 | POST | `/api/promo-codes/admin` | ✅ | ADMIN |
| 63 | GET | `/api/promo-codes/admin` | ✅ | ADMIN |
| 64 | GET | `/api/promo-codes/admin/active` | ✅ | ADMIN |
| 65 | GET | `/api/promo-codes/admin/{id}` | ✅ | ADMIN |
| 66 | PUT | `/api/promo-codes/admin/{id}` | ✅ | ADMIN |
| 67 | PUT | `/api/promo-codes/admin/{id}/deactivate` | ✅ | ADMIN |
| 68 | POST | `/api/promo-codes/organizer` | ✅ | ORGANIZER |
| 69 | GET | `/api/promo-codes/organizer` | ✅ | ORGANIZER |
| 70 | GET | `/api/promo-codes/organizer/{id}` | ✅ | ORGANIZER |
| 71 | PUT | `/api/promo-codes/organizer/{id}` | ✅ | ORGANIZER |
| 72 | PUT | `/api/promo-codes/organizer/{id}/deactivate` | ✅ | ORGANIZER |
| 73 | POST | `/api/promo-codes/available` | ✅ | Any (đã đăng nhập) |
| 74 | GET | `/api/ticket-listings` | ❌ | — |
| 75 | GET | `/api/ticket-listings/{id}` | ❌ | — |
| 76 | GET | `/api/ticket-listings/my-listings` | ✅ | Any |
| 77 | POST | `/api/ticket-listings` | ✅ | Any |
| 78 | DELETE | `/api/ticket-listings/{id}` | ✅ | Any |
| 79 | POST | `/api/ticket-listings/exchanges` | ✅ | Any |
| 80 | PUT | `/api/ticket-listings/exchanges/{id}/complete` | ✅ | Any |
| 81 | DELETE | `/api/ticket-listings/exchanges/{id}` | ✅ | Any |
| 82 | GET | `/api/transaction-histories/my-transactions` | ✅ | Any |
| 83 | GET | `/api/transaction-histories/booking/{bookingId}` | ✅ | Any |
| 84 | GET | `/api/transaction-histories/payment/{paymentId}` | ✅ | Any |
| 85 | GET | `/api/transaction-histories/admin` | ✅ | ADMIN |
| 86 | GET | `/api/transaction-histories/admin/user/{userId}` | ✅ | ADMIN |
| 87 | GET | `/api/organizer/wallet` | ✅ | ORGANIZER/ADMIN |
| 88 | PUT | `/api/organizer/wallet/bank-info` | ✅ | ORGANIZER/ADMIN |
| 89 | POST | `/api/organizer/wallet/withdraw` | ✅ | ORGANIZER/ADMIN |
| 90 | GET | `/api/organizer/wallet/transactions` | ✅ | ORGANIZER/ADMIN |
| 91 | GET | `/api/organizer/wallet/admin/all` | ✅ | ADMIN |
| 92 | GET | `/api/organizer/wallet/admin/user/{userId}` | ✅ | ADMIN |
| 93 | GET | `/api/organizer/wallet/admin/user/{userId}/transactions` | ✅ | ADMIN |

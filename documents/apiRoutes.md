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

---

## 1. Authentication

### 1.1. Đăng ký tài khoản

| | |
|---|---|
| **URL** | `POST /api/auth/register` |
| **Mô tả** | Tạo tài khoản mới cho người dùng |
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

**Response:** `AuthResponse` (accessToken, refreshToken, user info)

---

### 1.2. Đăng nhập

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

### 1.3. Làm mới token

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

### 1.4. Đăng xuất

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
  "name": "VIP",                       // ✅ Bắt buộc
  "description": "Vé VIP hàng đầu",   // ❌ Không bắt buộc
  "price": 500000,                     // ✅ Bắt buộc, số dương
  "totalQuantity": 100,                // ✅ Bắt buộc, số dương
  "maxPerBooking": 5                   // ❌ Không bắt buộc (mặc định: 10)
}
```

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
  "seatIds": [10, 11, 12]             // ❌ Không bắt buộc (dùng cho event có seat map)
}
```

---

### 7.2. Áp dụng mã giảm giá

| | |
|---|---|
| **URL** | `POST /api/bookings/apply-promo` |
| **Mô tả** | Áp mã khuyến mãi vào booking |
| **Authorization** | ✅ `Bearer <token>` |
| **Header** | `Authorization: Bearer <token>`, `Content-Type: application/json` |

**Request Body:**
```json
{
  "bookingId": 1,                  // ✅ Bắt buộc
  "promoCode": "SALE2026"          // ✅ Bắt buộc
}
```

---

### 7.3. Hủy booking

| | |
|---|---|
| **URL** | `DELETE /api/bookings/{id}` |
| **Mô tả** | Hủy đơn đặt vé |
| **Authorization** | ✅ `Bearer <token>` |
| **Path Variable** | `id` — Booking ID |
| **Request Body** | Không có |

---

### 7.4. Lấy danh sách booking của tôi

| | |
|---|---|
| **URL** | `GET /api/bookings/my-bookings` |
| **Mô tả** | Lấy tất cả booking của user đang đăng nhập |
| **Authorization** | ✅ `Bearer <token>` |
| **Request Body** | Không có |

---

### 7.5. Xem chi tiết booking

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
  "paymentMethod": "VNPAY"             // ✅ Bắt buộc (VNPAY, MOMO, STRIPE)
}
```

---

### 8.2. Payment callback (xử lý kết quả thanh toán)

| | |
|---|---|
| **URL** | `POST /api/payments/callback` |
| **Mô tả** | Callback từ cổng thanh toán, xác nhận thành công hoặc thất bại. Khi thành công sẽ tự confirm booking và sinh ticket. |
| **Authorization** | ❌ Không cần (gọi từ payment gateway) |
| **Query Params** | `transactionId` (String) ✅ Bắt buộc |
| | `success` (boolean) ✅ Bắt buộc — `true` hoặc `false` |
| **Request Body** | Không có |

---

### 8.3. Xem thanh toán theo booking

| | |
|---|---|
| **URL** | `GET /api/payments/booking/{bookingId}` |
| **Mô tả** | Lấy thông tin thanh toán của một booking |
| **Authorization** | ✅ `Bearer <token>` |
| **Query Param** | `bookingId` (Long) ✅ Bắt buộc |
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
  "name": "Khu VIP",                   // ✅ Bắt buộc
  "description": "Khu vực hạng sang",  // ❌ Không bắt buộc
  "capacity": 100,                     // ❌ Không bắt buộc
  "hasNumberedSeats": true             // ❌ Không bắt buộc
}
```

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

> ⚠️ **Tất cả API trong nhóm này đều yêu cầu role `ADMIN`**

### 12.1. Tạo mã giảm giá

| | |
|---|---|
| **URL** | `POST /api/promo-codes` |
| **Mô tả** | Tạo mã khuyến mãi mới |
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
  "validTo": "2026-12-31T23:59:59"         // ✅ Bắt buộc (ISO 8601)
}
```

---

### 12.2. Lấy tất cả promo codes

| | |
|---|---|
| **URL** | `GET /api/promo-codes` |
| **Mô tả** | Lấy danh sách tất cả mã giảm giá |
| **Authorization** | ✅ `Bearer <token>` |
| **Role** | 🔒 `ADMIN` |
| **Request Body** | Không có |

---

### 12.3. Lấy promo codes đang hoạt động

| | |
|---|---|
| **URL** | `GET /api/promo-codes/active` |
| **Mô tả** | Lấy danh sách mã giảm giá đang ACTIVE |
| **Authorization** | ✅ `Bearer <token>` |
| **Role** | 🔒 `ADMIN` |
| **Request Body** | Không có |

---

### 12.4. Xem chi tiết promo code

| | |
|---|---|
| **URL** | `GET /api/promo-codes/{id}` |
| **Mô tả** | Lấy thông tin chi tiết một mã giảm giá |
| **Authorization** | ✅ `Bearer <token>` |
| **Role** | 🔒 `ADMIN` |
| **Path Variable** | `id` — Promo Code ID |
| **Request Body** | Không có |

---

### 12.5. Vô hiệu hóa promo code

| | |
|---|---|
| **URL** | `PUT /api/promo-codes/{id}/deactivate` |
| **Mô tả** | Chuyển mã giảm giá sang trạng thái DISABLED |
| **Authorization** | ✅ `Bearer <token>` |
| **Role** | 🔒 `ADMIN` |
| **Path Variable** | `id` — Promo Code ID |
| **Request Body** | Không có |

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

## Tổng hợp nhanh

| # | Method | URL | Auth | Role |
|---|--------|-----|------|------|
| 1 | POST | `/api/auth/register` | ❌ | — |
| 2 | POST | `/api/auth/login` | ❌ | — |
| 3 | POST | `/api/auth/refresh-token` | ❌ | — |
| 4 | POST | `/api/auth/logout` | ❌ | — |
| 5 | GET | `/api/users/me` | ✅ | Any |
| 6 | PUT | `/api/users/me` | ✅ | Any |
| 7 | PUT | `/api/users/me/password` | ✅ | Any |
| 8 | GET | `/api/users` | ✅ | ADMIN |
| 9 | GET | `/api/users/role/{role}` | ✅ | ADMIN |
| 10 | PUT | `/api/users/{userId}/ban` | ✅ | ADMIN |
| 11 | PUT | `/api/users/{userId}/unban` | ✅ | ADMIN |
| 12 | PUT | `/api/users/{userId}/role?role=` | ✅ | ADMIN |
| 13 | GET | `/api/events` | ❌ | — |
| 14 | GET | `/api/events/{id}` | ❌ | — |
| 15 | POST | `/api/events` | ✅ | ORGANIZER/ADMIN |
| 16 | PUT | `/api/events/{id}` | ✅ | ORGANIZER/ADMIN |
| 17 | PUT | `/api/events/{id}/publish` | ✅ | ORGANIZER/ADMIN |
| 18 | PUT | `/api/events/{id}/cancel` | ✅ | ORGANIZER/ADMIN |
| 19 | GET | `/api/events/my-events` | ✅ | ORGANIZER/ADMIN |
| 20 | GET | `/api/events/all` | ✅ | ADMIN |
| 21 | GET | `/api/event-categories` | ❌ | — |
| 22 | GET | `/api/event-categories/{id}` | ❌ | — |
| 23 | POST | `/api/event-categories` | ✅ | ADMIN |
| 24 | PUT | `/api/event-categories/{id}` | ✅ | ADMIN |
| 25 | DELETE | `/api/event-categories/{id}` | ✅ | ADMIN |
| 26 | GET | `/api/event-schedules?eventId=` | ❌ | — |
| 27 | GET | `/api/event-schedules/available?eventId=` | ❌ | — |
| 28 | GET | `/api/event-schedules/{id}` | ❌ | — |
| 29 | POST | `/api/event-schedules` | ✅ | ORGANIZER/ADMIN |
| 30 | PUT | `/api/event-schedules/{id}/cancel` | ✅ | ORGANIZER/ADMIN |
| 31 | GET | `/api/ticket-types?eventId=` | ❌ | — |
| 32 | GET | `/api/ticket-types/available?eventId=` | ❌ | — |
| 33 | GET | `/api/ticket-types/{id}` | ❌ | — |
| 34 | POST | `/api/ticket-types` | ✅ | ORGANIZER/ADMIN |
| 35 | POST | `/api/bookings` | ✅ | Any |
| 36 | POST | `/api/bookings/apply-promo` | ✅ | Any |
| 37 | DELETE | `/api/bookings/{id}` | ✅ | Any |
| 38 | GET | `/api/bookings/my-bookings` | ✅ | Any |
| 39 | GET | `/api/bookings/{id}` | ✅ | Any |
| 40 | POST | `/api/payments` | ✅ | Any |
| 41 | POST | `/api/payments/callback?transactionId=&success=` | ❌ | — |
| 42 | GET | `/api/payments/booking/{bookingId}` | ✅ | Any |
| 43 | GET | `/api/tickets/my-tickets` | ✅ | Any |
| 44 | GET | `/api/tickets/booking/{bookingId}` | ✅ | Any |
| 45 | GET | `/api/tickets/code/{ticketCode}` | ✅ | Any |
| 46 | POST | `/api/tickets/check-in` | ✅ | STAFF/ORGANIZER/ADMIN |
| 47 | GET | `/api/venues` | ❌ | — |
| 48 | GET | `/api/venues/{id}` | ❌ | — |
| 49 | GET | `/api/venues/search?city=` | ❌ | — |
| 50 | POST | `/api/venues` | ✅ | ORGANIZER/ADMIN |
| 51 | PUT | `/api/venues/{id}` | ✅ | ORGANIZER/ADMIN |
| 52 | POST | `/api/seats/sections` | ✅ | ORGANIZER/ADMIN |
| 53 | GET | `/api/seats/sections/venue/{venueId}` | ✅ | Any |
| 54 | POST | `/api/seats` | ✅ | ORGANIZER/ADMIN |
| 55 | GET | `/api/seats/venue/{venueId}` | ✅ | Any |
| 56 | GET | `/api/seats/available?scheduleId=` | ✅ | Any |
| 57 | POST | `/api/promo-codes` | ✅ | ADMIN |
| 58 | GET | `/api/promo-codes` | ✅ | ADMIN |
| 59 | GET | `/api/promo-codes/active` | ✅ | ADMIN |
| 60 | GET | `/api/promo-codes/{id}` | ✅ | ADMIN |
| 61 | PUT | `/api/promo-codes/{id}/deactivate` | ✅ | ADMIN |
| 62 | GET | `/api/ticket-listings` | ❌ | — |
| 63 | GET | `/api/ticket-listings/{id}` | ❌ | — |
| 64 | GET | `/api/ticket-listings/my-listings` | ✅ | Any |
| 65 | POST | `/api/ticket-listings` | ✅ | Any |
| 66 | DELETE | `/api/ticket-listings/{id}` | ✅ | Any |
| 67 | POST | `/api/ticket-listings/exchanges` | ✅ | Any |
| 68 | PUT | `/api/ticket-listings/exchanges/{id}/complete` | ✅ | Any |
| 69 | DELETE | `/api/ticket-listings/exchanges/{id}` | ✅ | Any |

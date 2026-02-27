# E-Ticket Booking System - Business Logic & Services

---

## 📋 **1. AUTHENTICATION & AUTHORIZATION SERVICE**

### **1.1. Registration Logic**
- Validate email format và unique
- Validate password strength (min 8 chars, có số, chữ hoa/thường)
- Hash password với BCrypt
- Tạo User với role mặc định = CUSTOMER, status = **INACTIVE** (chờ xác nhận email)
- Tạo OTP 6 số, lưu vào bảng `email_otps` (hết hạn sau 5 phút)
- Gửi OTP qua email cho user
- Return tokens + user info (status = INACTIVE)

### **1.2. Email Verification (OTP) Logic**
- User nhập email + OTP
- Tìm OTP trong DB (`email_otps`) theo email + otp + used = false
- Validate OTP chưa hết hạn (`expiresAt > now`)
- Đánh dấu OTP đã dùng (`used = true`)
- Cập nhật User.status = **ACTIVE**
- Return success message

### **1.3. Resend OTP Logic**
- Validate email tồn tại trong hệ thống
- Check user.status != ACTIVE (nếu đã active → reject)
- Xóa OTP cũ của email
- Tạo OTP mới 6 số, lưu DB (hết hạn 5 phút)
- Gửi OTP mới qua email

### **1.4. Login Logic**
- Validate email/password
- Check user status (ACTIVE/INACTIVE/BANNED)
- Nếu BANNED → reject với message
- Nếu INACTIVE → yêu cầu verify email
- Generate Access Token (exp: 1h)
- Generate Refresh Token (exp: 7 days)
- Lưu Refresh Token vào DB (optional, để revoke)
- Return tokens + user info

### **1.5. Refresh Token Logic**
- Validate Refresh Token
- Check token expiration
- Check token trong DB (nếu implement revoke)
- Generate new Access Token
- Return new Access Token

### **1.6. Logout Logic**
- Invalidate Refresh Token (xóa khỏi DB hoặc blacklist)
- Client xóa token ở local storage

### **1.7. Password Reset Logic**
- User request reset (nhập email)
- Generate reset token (exp: 15 phút)
- Gửi email với reset link
- User click link, nhập password mới
- Validate token, update password
- Invalidate tất cả refresh tokens cũ

### **1.8. Role-Based Access Control**
- Middleware check JWT token
- Extract role từ token
- Check permission cho từng endpoint
- Reject nếu không đủ quyền

---

## 👤 **2. USER MANAGEMENT SERVICE**

### **2.1. Get User Profile**
- Lấy thông tin user từ JWT
- Return user details (exclude password)

### **2.2. Update User Profile**
- Validate input (fullName, phoneNumber)
- Check email unique nếu user đổi email
- Update user info
- Return updated profile

### **2.3. Change Password**
- Validate current password
- Validate new password strength
- Hash new password
- Update password
- Invalidate tất cả refresh tokens (force re-login)

### **2.4. Admin: Manage Users**
- List users (với pagination, filter theo role/status)
- Ban/Unban user
- Change user role (CUSTOMER ↔ ORGANIZER)
- Delete user (soft delete)

---

## 🎭 **3. EVENT CATEGORY SERVICE**

### **3.1. List Categories**
- Get all active categories
- Return categories với icon_url

### **3.2. Admin: Create Category**
- Validate category name unique
- Upload icon (nếu có)
- Create category

### **3.3. Admin: Update/Delete Category**
- Update category info
- Soft delete (nếu không có event nào dùng)

---

## 🏟️ **4. VENUE MANAGEMENT SERVICE**

### **4.1. List Venues**
- Search venues theo city/country
- Pagination

### **4.2. Get Venue Details**
- Lấy venue info
- Nếu has_seat_map = true → load sections & seats

### **4.3. Organizer: Create Venue**
- Validate venue info
- Create venue với has_seat_map = false (default)

### **4.4. Organizer: Setup Seat Map**
- Create sections cho venue
- Create seats cho từng section
- Define row_number, seat_number cho mỗi ghế

---

## 📍 **5. SECTION & SEAT SERVICE**

### **5.1. Create Sections**
- Validate section name unique trong venue
- Create section với capacity, has_numbered_seats

### **5.2. Create Seats**
- Generate seats cho section
- Auto-generate seat_number (A1, A2, ..., B1, B2...)
- Assign seat_type (VIP, REGULAR, WHEELCHAIR)

### **5.3. Get Available Seats**
- Input: event_schedule_id
- Lấy tất cả seats của venue
- Check SeatReservation để loại ghế đã book
- Return available seats grouped by section

---

## 🎪 **6. EVENT MANAGEMENT SERVICE**

### **6.1. Create Event (Organizer)**
- Validate input (name, category, venue, dates)
- Check organizer_id = current user
- Upload banner & thumbnail images
- Create event với status = DRAFT
- Calculate total_tickets từ venue capacity (hoặc set manual)
- Set available_tickets = total_tickets
- Set allow_ticket_exchange = true (default)

### **6.2. Publish Event**
- Validate event có đủ info (venue, tickets, schedules)
- Change status: DRAFT → PUBLISHED
- Gửi notification đến followers (optional)

### **6.3. List Events (Public)**
- Filter theo category, city, date range
- Search by name
- Sort by date, popularity
- Pagination
- Chỉ show events với status = PUBLISHED

### **6.4. Get Event Details**
- Lấy event info
- Lấy ticket types
- Lấy schedules
- Lấy venue info
- Calculate available_tickets

### **6.5. Update Event (Organizer)**
- Chỉ owner mới update được
- Nếu đã có booking → một số field không được sửa (venue, dates)

### **6.6. Cancel Event**
- Change status → CANCELLED
- Trigger refund cho tất cả bookings
- Gửi notification

### **6.7. Admin: Approve/Reject Event**
- Optional workflow: event cần admin duyệt trước khi publish
- Admin approve → status = PUBLISHED
- Admin reject → status = DRAFT, gửi lý do

---

## 🎟️ **7. TICKET TYPE SERVICE**

### **7.1. Create Ticket Types**
- Organizer define ticket types cho event
- VIP, Regular, Early Bird, etc.
- Set price, total_quantity, max_per_booking
- Set available_quantity = total_quantity

### **7.2. Update Ticket Type**
- Nếu đã có booking → không được giảm total_quantity xuống dưới sold

### **7.3. Get Available Ticket Types**
- Lấy ticket types của event
- Filter available_quantity > 0

---

## 📅 **8. EVENT SCHEDULE SERVICE**

### **8.1. Create Schedule**
- Tạo lịch chiếu/suất diễn cho event
- Một event có thể có nhiều schedules (concert 3 ngày, phim nhiều suất)
- Set start_time, end_time
- Calculate total_seats từ venue
- Set available_seats = total_seats

### **8.2. Update Schedule**
- Nếu đã có booking → hạn chế sửa
- Có thể cancel schedule riêng lẻ

### **8.3. Get Available Schedules**
- Lấy schedules của event
- Filter available_seats > 0
- Filter status = SCHEDULED

---

## 🛒 **9. BOOKING SERVICE (CORE)**

### **9.1. Create Booking Flow**

#### **Step 1: Validate Input**
- Check event_id, schedule_id valid
- Check ticket_type_ids và quantities
- Check user_id (authenticated)

#### **Step 2: Check Availability**
- Lock ticket_type records (pessimistic lock hoặc optimistic lock)
- Foreach ticket_type:
  - Check available_quantity >= requested quantity
  - Check quantity <= max_per_booking
- Nếu event có seat map:
  - Check seats available
  - User phải chọn specific seats

#### **Step 3: Reserve Seats (nếu có seat map)**
- Foreach selected seat:
  - Create SeatReservation với status = HOLDING
  - Set hold_expires_at = now + 15 phút
  - Link seat_id, event_schedule_id, user_id

#### **Step 4: Create Booking**
- Generate unique booking_code
- Calculate total_amount = sum(ticket_price * quantity)
- Nếu có `promoCodeId`:
  - Validate promo code (status = ACTIVE, valid_from/valid_to, usageLimit vs usedCount, minOrderAmount)
  - **Validate applicationType:**
    - `GLOBAL`: Áp dụng mọi event → pass
    - `ORGANIZER_ALL`: Check `promo.createdBy.id == event.organizer.id` → nếu sai throw lỗi
    - `SPECIFIC_EVENTS`: Check event.id có trong bảng PromoCodeEventJoin → nếu không throw lỗi
  - Calculate discount:
    - PERCENTAGE: discount = total * (discount_value / 100)
    - FIXED_AMOUNT: discount = discount_value
    - Apply max_discount_amount nếu có
  - Set discount_amount = calculated discount
  - Create BookingPromoCode record (discountApplied = discount_amount)
  - **Lưu ý:** Chưa tăng `usedCount` ở bước này (sẽ tăng khi payment SUCCESS)
- Nếu không có promo: Set discount_amount = 0
- Set final_amount = total_amount - discount_amount
- Set status = PENDING
- Set hold_expires_at = now + 15 phút

#### **Step 5: Create Booking Details**
- Foreach ticket_type:
  - Create BookingDetail với quantity, unit_price, subtotal

#### **Step 6: Decrease Inventory**
- Foreach ticket_type:
  - available_quantity -= booked quantity
- If schedule:
  - available_seats -= number of seats

#### **Step 7: Return Booking Info**
- Return booking_id, booking_code, total_amount, hold_expires_at
- Client chuyển sang payment

### **9.2. Get Available Promo Codes (Preview)**
- User gửi danh sách items (ticketTypeId + quantity) và eventId
- Hệ thống tính totalAmount từ items
- Xác định organizer của event (từ event.organizer.id)
- **Lọc promo codes theo 3 loại (JPQL query):**
  1. `GLOBAL` (Admin tạo): status = ACTIVE, còn hạn → áp dụng tất cả events
  2. `ORGANIZER_ALL` (Organizer tạo): status = ACTIVE, còn hạn, `createdBy.id == event.organizer.id`
  3. `SPECIFIC_EVENTS` (Organizer tạo): status = ACTIVE, còn hạn, event phải có trong bảng PromoCodeEventJoin
- Sau đó filter thêm: chưa hết lượt dùng, đủ minOrderAmount
- Cho mỗi promo, tính preview: discountAmount, finalAmount
- Trả về danh sách promo khả dụng kèm số tiền giảm dự kiến và `applicationType`
- **Promo code của Organizer khác tạo cho event khác sẽ KHÔNG hiển thị**
- **Endpoint:** `POST /api/promo-codes/available` (chỉ cần đăng nhập)

### **9.3. Cancel Booking (User)**
- Check booking belongs to user
- Check booking status = PENDING hoặc CONFIRMED
- Check cancellation policy (time-based)
- If payment done:
  - Calculate refund amount theo policy
  - Create refund request
- Release seats (update SeatReservation status = RELEASED)
- Restore inventory (available_quantity, available_seats)
- Update booking status = CANCELLED

### **9.4. Expire Booking (Auto)**
- Scheduled job chạy mỗi 1 phút
- Find bookings: status = PENDING AND hold_expires_at < now
- Foreach expired booking:
  - Release seats
  - Restore inventory
  - Update status = EXPIRED
  - Cancel pending payment (nếu có)

### **9.5. Confirm Booking**
- Trigger sau khi payment success
- Update booking status = CONFIRMED
- Update SeatReservation status = CONFIRMED
- Generate tickets
- Send confirmation email/SMS

### **9.6. Get My Bookings**
- User xem lịch sử bookings
- Filter by status
- Pagination
- Return booking + tickets

### **9.7. Get Booking Details**
- Lấy full info: booking, booking_details, tickets, payment, event

---

## 💳 **10. PAYMENT SERVICE**

### **10.1. Create Payment**
- Input: booking_id, payment_method (VNPAY, MOMO, STRIPE)
- Validate booking exists, status = PENDING
- Create Payment record với status = PENDING
- Generate transaction_id unique

### **10.2. VNPay Payment Flow**
- Build VNPay request params
- Generate secure hash
- Return payment URL
- User redirect đến VNPay
- User thanh toán
- VNPay redirect về callback URL

### **10.3. Payment Callback (Webhook)**
- Receive callback từ payment gateway
- Validate signature/hash
- Parse transaction status
- If SUCCESS:
  - Update Payment: status = SUCCESS, paid_at = now
  - Update Booking: status = CONFIRMED
  - Generate tickets
  - **Tăng `usedCount` của promo code** (nếu booking có BookingPromoCode)
    - Query BookingPromoCode theo booking_id
    - Với mỗi promo đã áp dụng: promo.usedCount += 1
  - Send notification
- If FAILED:
  - Update Payment: status = FAILED
  - Có thể retry hoặc cancel booking

### **10.4. Refund Payment**
- Input: booking_id, refund_amount
- Validate payment exists, status = SUCCESS
- Call payment gateway refund API
- Update Payment: status = REFUNDED
- Create refund transaction record

---

## 🎫 **11. TICKET GENERATION SERVICE**

### **11.1. Generate Tickets**
- Trigger sau khi booking confirmed
- Foreach booking_detail:
  - For i = 1 to quantity:
    - Generate unique ticket_code
    - Generate QR code (embed ticket_code + event_id + schedule_id)
    - Create Ticket record
    - Set current_owner_id = booking.customer_id
    - Set is_transferable = (event.allow_ticket_exchange AND ticket_type.allow_transfer)
    - Set is_checked_in = false
    - Link ticket to booking, ticket_type

### **11.2. Get Ticket Details**
- Input: ticket_code hoặc ticket_id
- Return ticket info + event + schedule + seat (nếu có)
- Return QR code image

### **11.3. Download E-Ticket (PDF)**
- Generate PDF với QR code
- Include event info, seat info, terms
- Return downloadable PDF

---

## 🔍 **12. TICKET CHECK-IN SERVICE (Gate Staff)**

### **12.1. Scan QR Code**
- Staff scan QR code
- Parse ticket_code từ QR
- Lookup ticket by ticket_code

### **12.2. Validate Ticket**
- Check ticket exists
- Check ticket.is_checked_in = false (chưa sử dụng)
- Check event_schedule matches (đúng suất diễn)
- Check ticket chưa hết hạn (schedule.start_time chưa quá)

### **12.3. Check-In Ticket**
- Update ticket:
  - is_checked_in = true
  - checked_in_at = now
  - checked_in_by = staff_user_id
- Return success message
- Show seat info (nếu có)

### **12.4. Handle Duplicate Check-In**
- Nếu ticket đã check-in → reject
- Show thông tin đã check-in lúc nào, bởi ai
- Alert security (có thể fake ticket)

### **12.5. Check-In Statistics**
- Admin/Organizer xem stats:
  - Số người đã check-in
  - Số người chưa đến
  - Check-in by time (graph)

---

## 🔄 **13. TICKET EXCHANGE SERVICE (Secondary Market)**

### **13.1. Create Ticket Listing**
- User muốn bán/trao đổi vé
- Validate:
  - Ticket belongs to user (current_owner_id = user_id)
  - Ticket.is_transferable = true
  - Ticket.is_checked_in = false
  - Event.allow_ticket_exchange = true
- User set listing_price, exchange_type (SELL/TRADE/BOTH)
- Create TicketListing với status = FOR_SALE
- Set listed_at = now, expires_at (optional)

### **13.2. Browse Listings**
- Public list available tickets
- Filter by event, ticket_type, price range
- Sort by price
- Pagination

### **13.3. Create Exchange Request (Purchase)**
- Buyer chọn ticket listing
- Validate:
  - Listing status = FOR_SALE
  - Buyer != Seller
  - Listing chưa expired
- Create TicketExchange:
  - transaction_type = PURCHASE
  - status = PENDING
  - price = listing_price

### **13.4. Create Exchange Request (Trade)**
- User A muốn đổi vé A lấy vé B
- Validate:
  - Trade ticket exists, belongs to user A
  - Trade ticket is_transferable = true, not checked-in
- Create TicketExchange:
  - transaction_type = TRADE
  - trade_ticket_id = ticket A
  - status = PENDING

### **13.5. Process Payment for Exchange**
- If PURCHASE:
  - Buyer thanh toán listing_price
  - Create Payment record
  - Payment flow tương tự booking
- If TRADE:
  - Không cần payment
  - Require seller approval

### **13.6. Complete Exchange**
- Trigger sau khi payment success (PURCHASE) hoặc seller approve (TRADE)
- Update TicketExchange status = COMPLETED
- Update original ticket:
  - current_owner_id = buyer_id
  - Create TicketTransferLog
- Update trade ticket (nếu có):
  - current_owner_id = seller_id
  - Create TicketTransferLog
- Update TicketListing status = SOLD
- Send notifications

### **13.7. Cancel Exchange**
- Before payment: free cancel
- After payment: follow refund policy
- Release tickets
- Update status = CANCELLED

---

## 🎁 **14. PROMO CODE SERVICE**

> **Phân quyền CRUD:**
> - **ADMIN:** Chỉ tạo `GLOBAL` promo codes. Có quyền xem/sửa/deactivate **tất cả** promo codes (kể cả của Organizer)
> - **ORGANIZER:** Tạo `ORGANIZER_ALL` hoặc `SPECIFIC_EVENTS`. Chỉ CRUD promo codes **do chính mình tạo** (kiểm tra `createdBy.id`)

### **14.1. Admin: Create Promo Code**
- **Endpoint:** `POST /api/promo-codes/admin` (Role: ADMIN)
- Validate code unique (auto UPPERCASE)
- Validate `validTo` > `validFrom`
- **Admin chỉ được tạo `applicationType = GLOBAL`** → nếu truyền loại khác → lỗi 400
- Set discount_type (PERCENTAGE / FIXED_AMOUNT)
- Set discount_value, min_order_amount, max_discount_amount
- Set usage_limit, usedCount = 0
- Set valid_from, valid_to
- Set status = ACTIVE
- Set createdBy = admin user
- Lưu promo code vào DB

### **14.2. Organizer: Create Promo Code**
- **Endpoint:** `POST /api/promo-codes/organizer` (Role: ORGANIZER)
- Validate code unique (auto UPPERCASE)
- Validate `validTo` > `validFrom`
- **Organizer KHÔNG được tạo `GLOBAL`** → lỗi 400
- `applicationType` phải là `ORGANIZER_ALL` hoặc `SPECIFIC_EVENTS`
- Nếu `SPECIFIC_EVENTS`:
  - `eventIds` bắt buộc, không được rỗng
  - Validate tất cả events thuộc về organizer hiện tại (`event.organizer.id == organizerId`) → nếu sai → lỗi 403
  - Tạo records trong bảng `PromoCodeEventJoin` cho từng event
- Set createdBy = organizer user
- Các field khác giống 14.1

### **14.3. Admin: List/Get Promo Codes**
- `GET /api/promo-codes/admin` — Lấy **tất cả** promo codes (kể cả của Organizer)
- `GET /api/promo-codes/admin/active` — Lấy promo codes đang ACTIVE
- `GET /api/promo-codes/admin/{id}` — Xem chi tiết promo code bất kỳ

### **14.4. Organizer: List/Get Promo Codes**
- `GET /api/promo-codes/organizer` — Lấy promo codes **do mình tạo** (`createdBy.id == organizerId`)
- `GET /api/promo-codes/organizer/{id}` — Xem chi tiết promo code (chỉ của mình, trả 403 nếu không phải)

### **14.5. Admin: Update Promo Code**
- **Endpoint:** `PUT /api/promo-codes/admin/{id}` (Role: ADMIN)
- Admin có thể sửa bất kỳ promo code nào
- Validate `validTo` > `validFrom`, code unique nếu đổi code
- Cập nhật event mappings nếu `SPECIFIC_EVENTS` (xóa cũ, tạo mới)

### **14.6. Organizer: Update Promo Code**
- **Endpoint:** `PUT /api/promo-codes/organizer/{id}` (Role: ORGANIZER)
- Chỉ sửa promo code **do mình tạo** (`createdBy.id == organizerId`) → nếu sai → lỗi 403
- **Không được đổi sang `GLOBAL`** → lỗi 400
- Validate `validTo` > `validFrom`, code unique nếu đổi code
- Nếu `SPECIFIC_EVENTS`:
  - Xóa event mappings cũ, tạo mới
  - Validate tất cả events thuộc về organizer

### **14.7. Admin/Organizer: Deactivate Promo Code**
- **Admin:** `PUT /api/promo-codes/admin/{id}/deactivate` — Deactivate bất kỳ promo code
- **Organizer:** `PUT /api/promo-codes/organizer/{id}/deactivate` — Deactivate promo code **do mình tạo** (chỉ kiểm tra `createdBy.id`)
- Update status = DISABLED

### **14.8. Validate Promo Code (Internal)**
- Check code exists
- Check status = ACTIVE
- Check now between valid_from and valid_to
- Check used_count < usage_limit
- Check booking amount >= min_order_amount
- **Check applicationType:**
  - `GLOBAL`: Áp dụng mọi event → pass
  - `ORGANIZER_ALL`: Check `promo.createdBy.id == event.organizer.id`
  - `SPECIFIC_EVENTS`: Check event.id có trong bảng PromoCodeEventJoin

### **14.9. Get Available Promo Codes (Preview)**
- **Endpoint:** `POST /api/promo-codes/available` (Role: bất kỳ user đã đăng nhập)
- Input: eventId + list items (ticketTypeId, quantity)
- Tính totalAmount từ items (quantity × ticketType.price)
- Xác định organizer của event (từ event.organizer.id)
- **Lọc promo codes theo 3 loại:**
  1. `GLOBAL`: status = ACTIVE, còn hạn → áp dụng tất cả events
  2. `ORGANIZER_ALL`: status = ACTIVE, còn hạn, `createdBy.id == event.organizer.id`
  3. `SPECIFIC_EVENTS`: status = ACTIVE, còn hạn, event phải có trong bảng PromoCodeEventJoin
- Filter thêm: chưa hết lượt dùng (`usedCount < usageLimit`), đủ `minOrderAmount`
- Tính preview cho mỗi promo:
  - PERCENTAGE: discountAmount = totalAmount × (discountValue / 100), cap ở maxDiscountAmount
  - FIXED_AMOUNT: discountAmount = discountValue
- Return: totalAmount + list PromoPreview (id, code, description, **applicationType**, discountAmount, finalAmount)
- **Promo code của Organizer khác tạo cho event khác sẽ KHÔNG hiển thị**

### **14.10. Apply Promo in Booking**
- Gọi từ Booking Service (Step 9.1 - Step 4)
- Promo được áp dụng ngay khi tạo booking (truyền `promoCodeId` trong CreateBookingRequest)
- **Không còn API riêng `POST /api/bookings/apply-promo`**
- `usedCount` chỉ tăng khi payment callback SUCCESS (trong PaymentService)

### **14.11. Expire Promo Codes (Scheduled Job)**
- Chạy hàng ngày lúc 00:00 (`@Scheduled(cron = "0 0 0 * * *")`)
- Tìm promo codes: status = ACTIVE và `validTo < now`
- Update status = EXPIRED

---

## 📊 **15. REPORTING & ANALYTICS SERVICE**

### **15.1. Event Revenue Report**
- Input: event_id, date_range
- Calculate:
  - Total bookings
  - Total revenue (sum final_amount)
  - Revenue by ticket_type
  - Revenue by day
- Return data + chart

### **15.2. Ticket Sales Report**
- Số vé bán theo event
- Tỷ lệ lấp đầy (sold/total)
- Peak booking times

### **15.3. Refund Report**
- Total refunds
- Refund by event
- Refund reasons

### **15.4. Customer Analytics**
- Top customers (by spending)
- Customer lifetime value
- Booking frequency

### **15.5. Organizer Dashboard**
- Overview events của organizer
- Total revenue
- Upcoming events
- Event performance

### **15.6. Admin Dashboard**
- System-wide stats
- Total users
- Total events
- Total revenue
- Platform fees

---

## 🔔 **16. NOTIFICATION SERVICE**

### **16.1. Email Notifications**
- Registration welcome email
- Booking confirmation email (with ticket PDF)
- Payment success email
- Event reminder (1 day before)
- Cancellation email
- Refund confirmation email
- Ticket exchange confirmation

### **16.2. SMS Notifications**
- Booking confirmation (OTP-style)
- Event reminder
- QR code link

### **16.3. Push Notifications (Mobile)**
- Event recommendations
- Flash sales
- Event reminders

### **16.4. In-App Notifications**
- Booking status updates
- Exchange requests
- Admin announcements

---

## 🔐 **17. SECURITY & FRAUD PREVENTION SERVICE**

### **17.1. Rate Limiting**
- Limit booking requests per user (chống bot)
- Limit API calls per IP

### **17.2. Duplicate Booking Prevention**
- Check user không book quá nhiều lần cho cùng event
- Lock mechanism cho inventory

### **17.3. Fake Ticket Detection**
- Validate QR code signature
- Check ticket exists in DB
- Alert khi duplicate check-in attempts

### **17.4. Account Security**
- Login attempt tracking
- Lock account sau N failed attempts
- 2FA (optional)

---

## 🗄️ **18. FILE STORAGE SERVICE**

### **18.1. Upload Event Images**
- Upload banner, thumbnail
- Validate file type (jpg, png)
- Validate file size (max 5MB)
- Generate URL
- Store URL in Event record

### **18.2. Upload Category Icons**
- Similar to event images

### **18.3. Generate QR Code Images**
- Generate QR from ticket data
- Store as image or base64

### **18.4. Generate PDF Tickets**
- Create PDF template
- Embed QR code
- Include event details
- Return downloadable PDF

---

## 🔍 **19. SEARCH & FILTER SERVICE**

### **19.1. Event Search**
- Full-text search by event name
- Filter by:
  - Category
  - City/Country
  - Date range
  - Price range
  - Status
- Sort by:
  - Date
  - Price
  - Popularity

### **19.2. Venue Search**
- Search by name, city, country

### **19.3. User Search (Admin)**
- Search by email, name
- Filter by role, status

---

## 🕒 **20. SCHEDULED JOBS (Background Tasks)**

### **20.1. Expire Pending Bookings** ✅ Đã implement
- Run every 1 minute
- Find bookings: status=PENDING AND hold_expires_at < now
- Auto-cancel và release inventory

### **20.2. Expire Seat Reservations**
- Run every 1 minute
- Find reservations: status=HOLDING AND hold_expires_at < now
- Auto-release seats

### **20.3. Event Reminder**
- Run daily at 9AM
- Find events starting tomorrow
- Send reminder emails/SMS to ticket holders

### **20.4. Event Status Update** ✅ Đã implement
- Run every hour (`@Scheduled(cron = "0 0 * * * *")`)
- Update EventSchedule status:
  - SCHEDULED → ONGOING (nếu startTime đã qua)
  - ONGOING → COMPLETED (nếu endTime đã qua)
- Update Event status:
  - PUBLISHED → ONGOING (nếu bất kỳ schedule nào ONGOING)
  - ONGOING → COMPLETED (nếu tất cả schedules đã COMPLETED hoặc CANCELLED và không còn schedule SCHEDULED/ONGOING)

### **20.5. Promo Code Expiration** ✅ Đã implement
- Run daily at midnight (`@Scheduled(cron = "0 0 0 * * *")`)
- Update promo codes: status=ACTIVE → EXPIRED (nếu valid_to passed)

### **20.6. Ticket Listing Expiration** ✅ Đã implement
- Run every hour (`@Scheduled(cron = "0 0 * * * *")`)
- Update listings: status=FOR_SALE → EXPIRED (nếu expires_at passed)

---

## 📈 **21. WAITLIST SERVICE (Optional)**

### **21.1. Join Waitlist**
- User join waitlist khi event sold out
- Create waitlist record

### **21.2. Notify When Available**
- Khi có vé available (refund, release):
  - Notify users in waitlist
  - Priority by join time

---

## 🎯 **22. RECOMMENDATION SERVICE (Optional)**

### **22.1. Recommend Events**
- Based on user's booking history
- Based on category preferences
- Based on location

### **22.2. Trending Events**
- Track views, bookings
- Show trending events on homepage

---

## 🏷️ **23. LOYALTY PROGRAM SERVICE (Optional)**

### **23.1. Accumulate Points**
- User earn points for each booking
- Points = final_amount * 0.01

### **23.2. Redeem Points**
- User redeem points for discount
- 100 points = 1 USD discount

### **23.3. Tier System**
- Bronze, Silver, Gold, Platinum
- Benefits: early access, extra discounts

---

## 🔧 **24. ADMIN CONFIGURATION SERVICE**

### **24.1. System Settings**
- Booking timeout duration
- Payment timeout duration
- Refund policy rules
- Platform fee percentage
- Email templates
- SMS templates

### **24.2. Feature Toggles**
- Enable/disable ticket exchange
- Enable/disable promo codes
- Enable/disable loyalty program

---

## 📝 **SUMMARY: CORE SERVICES PRIORITY**

### **Phase 1 (MVP):**
1. Authentication Service
2. User Management Service
3. Event Management Service
4. Ticket Type Service
5. Booking Service
6. Payment Service
7. Ticket Generation Service
8. Ticket Check-In Service

### **Phase 2 (Enhanced):**
9. Promo Code Service
10. Event Schedule Service
11. Venue & Seat Service
12. Notification Service
13. Reporting Service

### **Phase 3 (Advanced):**
14. Ticket Exchange Service
15. Waitlist Service
16. Recommendation Service
17. Loyalty Program Service

---

## 🎯 **KEY BUSINESS RULES TO REMEMBER**

1. **Inventory Management**: Luôn dùng database locking khi update available_quantity/seats
2. **Timeout Logic**: Booking và SeatReservation phải có expiration
3. **Payment First**: Không generate ticket trước khi payment success
4. **Check-In Once**: Một ticket chỉ check-in được 1 lần
5. **Transferable Control**: Respect Event.allow_ticket_exchange và Ticket.is_transferable
6. **Refund Policy**: Time-based refund calculation
7. **Role-Based Access**: Strict permission checking
8. **Audit Trail**: Log tất cả ticket transfers và critical actions
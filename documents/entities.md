# Phân tích Entity và Logic Nghiệp Vụ - E-Ticket Booking System

## 1. User Entity
### Tác dụng
Quản lý tất cả người dùng trong hệ thống

### Logic nghiệp vụ
- **Phân quyền đa vai trò:** CUSTOMER (khách hàng), ORGANIZER (nhà tổ chức), ADMIN (quản trị viên), STAFF (nhân viên soát vé)
- **Quản lý trạng thái:** ACTIVE (hoạt động), INACTIVE (tạm ngưng), BANNED (bị cấm)
- **Email unique** để đăng nhập và liên hệ
- **Password** được mã hóa (khuyến nghị BCrypt)
- Lưu thông tin cơ bản: fullName, phoneNumber

---

## 2. EventCategory Entity
### Tác dụng
Phân loại sự kiện theo thể loại (Concert, Sports, Theater, Conference, Cinema, Tour)

### Logic nghiệp vụ
- **Name unique** để tránh trùng lặp danh mục
- Hỗ trợ **iconUrl** để hiển thị icon trên UI
- Dùng để filter/search events theo category
- Giúp user dễ dàng tìm kiếm sự kiện theo sở thích

---

## 3. Venue Entity
### Tác dụng
Quản lý địa điểm tổ chức sự kiện

### Logic nghiệp vụ
- Lưu thông tin địa điểm: name, address, city, country
- **totalCapacity**: Sức chứa tối đa của venue
- **hasSeatMap**: Flag quan trọng
  - `true`: Venue có sơ đồ ghế ngồi cố định (concert hall, cinema)
  - `false`: Standing area, không có ghế đánh số
- Dùng để filter sự kiện theo vùng địa lý

---

## 4. Section Entity
### Tác dụng
Phân khu vực trong venue (VIP Zone, Section A/B/C)

### Logic nghiệp vụ
- Một venue có nhiều sections để phân tầng giá và chỗ ngồi
- **capacity**: Sức chứa của từng section
- **hasNumberedSeats**: 
  - `true`: Có ghế đánh số (Section A, B, C)
  - `false`: Standing area (khu đứng)
- Giúp quản lý và pricing theo từng khu vực

---

## 5. Seat Entity
### Tác dụng
Quản lý từng ghế ngồi cụ thể trong venue

### Logic nghiệp vụ
- **rowNumber + seatNumber**: Định danh ghế (VD: A-15 = Hàng A, Ghế 15)
- **seatType**: Phân loại ghế (VIP, REGULAR, WHEELCHAIR)
- Thuộc về Venue và Section
- Chỉ tồn tại khi venue có seat map (venue.hasSeatMap = true)
- Dùng để hiển thị sơ đồ chỗ ngồi cho user chọn

---

## 6. Event Entity
### Tác dụng
Entity trung tâm - Quản lý sự kiện

### Logic nghiệp vụ
- Liên kết với EventCategory, User (organizer), Venue
- **Status workflow:** DRAFT → PUBLISHED → ONGOING → COMPLETED (hoặc CANCELLED)
- **totalTickets & availableTickets**: Real-time inventory tracking
- **allowTicketExchange**: Flag kiểm soát secondary market
  - `true`: Cho phép user bán/trao đổi vé trên platform
  - `false`: Vé không thể chuyển nhượng (event VIP/restricted)
  - Impact: Nếu false, tất cả tickets của event sẽ có isTransferable = false
- Lưu hình ảnh: bannerImageUrl, thumbnailImageUrl cho marketing

---

## 7. EventSchedule Entity
### Tác dụng
Quản lý lịch chiếu/suất diễn của event

### Logic nghiệp vụ
- Một Event có thể có nhiều EventSchedules (concert nhiều ngày, rạp phim nhiều suất)
- **startTime, endTime**: Thời gian bắt đầu/kết thúc suất diễn
- **totalSeats, availableSeats**: Quản lý inventory theo từng suất
- **Status:** SCHEDULED → ONGOING → COMPLETED (hoặc CANCELLED)
- Dùng để user chọn suất diễn phù hợp

---

## 8. TicketType Entity
### Tác dụng
Định nghĩa các loại vé và giá cho event

### Logic nghiệp vụ
- Một Event có nhiều TicketTypes (VIP, Regular, Early Bird)
- **price**: Giá cho từng loại vé
- **totalQuantity & availableQuantity**: Quản lý inventory theo loại vé
- **maxPerBooking**: Giới hạn số vé/giao dịch (chống mua gom, bot)
- Real-time update availableQuantity khi booking/cancel
- Cho phép phân tầng giá linh hoạt

---

## 9. PromoCode Entity
### Tác dụng
Quản lý mã giảm giá/voucher

### Logic nghiệp vụ
- **discountType**: PERCENTAGE (%) hoặc FIXED_AMOUNT (số tiền cố định)
- **discountValue**: Giá trị giảm (20 = 20% hoặc 50000đ)
- **minOrderAmount**: Đơn tối thiểu để áp dụng
- **maxDiscountAmount**: Giới hạn số tiền giảm tối đa (cho PERCENTAGE)
- **usageLimit & usedCount**: Kiểm soát số lần sử dụng
- **validFrom & validTo**: Thời hạn sử dụng
- **Status:** ACTIVE, EXPIRED, DISABLED
- Tự động update usedCount khi booking confirmed

---

## 10. Booking Entity
### Tác dụng
Đơn đặt vé - Entity trung tâm của booking flow

### Logic nghiệp vụ
- **bookingCode**: Unique identifier để user tra cứu
- **Pricing breakdown:**
  - totalAmount = Tổng tiền gốc
  - discountAmount = Số tiền giảm từ promo code
  - finalAmount = totalAmount - discountAmount
- **Status workflow:** PENDING (hold ghế) → CONFIRMED (đã thanh toán) → CANCELLED/EXPIRED
- **holdExpiresAt**: Timeout tự động release nếu không thanh toán (10-15 phút)
- Liên kết với customer, event, schedule
- Background job tự động chuyển PENDING → EXPIRED khi hết timeout

---

## 11. BookingDetail Entity
### Tác dụng
Chi tiết các loại vé trong một booking (Order Line Items)

### Logic nghiệp vụ
- Một Booking có nhiều BookingDetails (1:N)
- Lưu quantity, unitPrice, subtotal cho từng loại vé
- **Calculation:** subtotal = quantity × unitPrice
- **Immutable pricing:** unitPrice là snapshot giá tại thời điểm booking
- Cho phép mua nhiều loại vé khác nhau trong một booking

---

## 12. BookingPromoCode Entity
### Tác dụng
Junction table - Liên kết booking với promo code đã sử dụng

### Logic nghiệp vụ
- Track promo code nào được apply cho booking nào
- **discountApplied**: Số tiền thực tế được giảm (sau khi tính toán + cap)
- Audit trail để báo cáo hiệu quả của marketing campaigns
- Hỗ trợ analytics: "Promo X tạo ra bao nhiêu booking?"

---

## 13. Payment Entity
### Tác dụng
Quản lý giao dịch thanh toán

### Logic nghiệp vụ
- **One-to-One với Booking** (booking_id có UNIQUE constraint)
- **paymentMethod**: Hỗ trợ nhiều gateway (VNPAY, MOMO, STRIPE, etc.)
- **Status workflow:** PENDING → SUCCESS/FAILED → REFUNDED
- **transactionId**: UNIQUE, ID từ payment gateway (dùng cho reconciliation)
- **paidAt**: Timestamp chính xác khi thanh toán thành công
- Khi SUCCESS → trigger update Booking status và generate Tickets

---

## 14. Ticket Entity
### Tác dụng
Vé điện tử cá nhân - Sản phẩm cuối cùng user nhận được

### Logic nghiệp vụ
- **ticketCode**: Human-readable unique identifier
- **qrCode**: Encrypted string/Base64 image để check-in (format: TICKET_ID|EVENT_ID|SCHEDULE_ID|HMAC)
- **currentOwner**: Track chủ sở hữu hiện tại
  - Ban đầu = booking.customer
  - Update khi vé được trao đổi trên secondary market
- **isTransferable**: Flag kiểm soát khả năng chuyển nhượng
  - `true`: Vé có thể list/sell trên marketplace
  - `false`: Vé locked, không thể chuyển (VIP event)
- **isCheckedIn**: CRITICAL FLAG
  - Khi `true`: Vé đã sử dụng, KHÔNG thể list, KHÔNG thể exchange, KHÔNG thể thay đổi owner
- **checkedInAt, checkedInBy**: Audit trail cho check-in
- Mỗi BookingDetail tạo ra quantity Tickets tương ứng

---

## 15. SeatReservation Entity
### Tác dụng
Quản lý việc hold/reserve ghế - Giải quyết race condition

### Logic nghiệp vụ
- **Purpose:** Tránh overselling khi nhiều user đặt cùng ghế đồng thời
- **Status workflow:** HOLDING (lock 10-15 phút) → CONFIRMED (đã thanh toán) / RELEASED (timeout/cancel)
- **holdExpiresAt**: Timeout tự động release ghế
- **Concurrency control:** Sử dụng database locking (SELECT FOR UPDATE)
- Background job tự động release HOLDING → RELEASED khi hết timeout
- Liên kết với seat, eventSchedule, user, booking

---

## 16. TicketListing Entity
### Tác dụng
Secondary market - User đăng vé để bán/trao đổi

### Logic nghiệp vụ
- **BUSINESS RULES (validate ở Service Layer):**
  - Ticket KHÔNG thể listed nếu isCheckedIn = true (đã sử dụng)
  - Ticket KHÔNG thể listed nếu isTransferable = false
  - Event PHẢI có allowTicketExchange = true
- **exchangeType:** SELL (bán), TRADE (đổi), BOTH (vừa bán vừa đổi)
- **listingPrice:** Giá người bán đặt (market-driven, có thể > hoặc < giá gốc)
- **Status:** FOR_SALE → SOLD/CANCELLED/EXPIRED
- **expiresAt:** Auto-expire listing (VD: 1 ngày trước event)
- Background job tự động expire listings

---

## 17. TicketExchange Entity
### Tác dụng
Ghi nhận giao dịch trao đổi vé trên secondary market

### Logic nghiệp vụ
- **transactionType:**
  - PURCHASE: Mua bán bằng tiền (buyer → platform → seller)
  - TRADE: Đổi vé với vé (no money, swap ownership)
- **Status workflow:** PENDING → PAYMENT_PENDING → COMPLETED/FAILED/CANCELLED
- Track seller, buyer, price, payment method
- **Flow khi COMPLETED:**
  1. Update Ticket.currentOwner = buyer
  2. Create TicketTransferLog
  3. Update TicketListing.status = SOLD
  4. Platform giữ commission, payout cho seller
- Liên kết với TicketListing (vé được bán), Payment (nếu có tiền), tradeTicket (nếu TRADE)

---

## 18. TicketTransferLog Entity
### Tác dụng
Audit trail - Immutable log cho việc chuyển nhượng vé

### Logic nghiệp vụ
- **Immutable logging:** Chỉ có createdAt, KHÔNG có updatedAt
- Mỗi lần vé đổi tay → tạo 1 record mới, KHÔNG BAO GIỜ update/delete
- **transferType:** EXCHANGE (secondary market), RETURN (refund), UPGRADE (nâng cấp vé)
- Track fromUser → toUser
- Link với TicketExchange nếu là giao dịch chính thức
- Dùng để:
  - Audit trail: Vé đã đổi tay bao nhiêu lần
  - Fraud detection: Phát hiện flipping quá nhiều lần
  - Chain of custody tracking

---
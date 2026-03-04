# Phân tích Entity và Logic Nghiệp Vụ - E-Ticket Booking System

## 1. User Entity
### Tác dụng
Quản lý tất cả người dùng trong hệ thống

### Logic nghiệp vụ
- **Phân quyền đa vai trò:** CUSTOMER (khách hàng), ORGANIZER (nhà tổ chức), ADMIN (quản trị viên), STAFF (nhân viên soát vé)
- **Quản lý trạng thái:** ACTIVE (hoạt động), INACTIVE (chờ xác nhận email), BANNED (bị cấm)
- **Email unique** để đăng nhập và liên hệ
- **Password** được mã hóa (BCrypt)
- Lưu thông tin cơ bản: fullName, phoneNumber
- Khi đăng ký mới, user có status = **INACTIVE** cho đến khi xác nhận email qua OTP

---

## 2. EmailOtp Entity
### Tác dụng
Lưu trữ mã OTP xác thực email khi đăng ký tài khoản

### Logic nghiệp vụ
- **email**: Email cần xác thực
- **otp**: Mã OTP 6 số ngẫu nhiên
- **expiresAt**: Thời điểm hết hạn (mặc định 5 phút sau khi tạo)
- **used**: Flag đánh dấu OTP đã được sử dụng hay chưa
- Khi tạo OTP mới cho cùng email → xóa OTP cũ trước
- OTP chỉ valid khi: `used = false` AND `expiresAt > now()`
- Sau khi verify thành công → đánh dấu `used = true`

---

## 3. EventCategory Entity
### Tác dụng
Phân loại sự kiện theo thể loại (Concert, Sports, Theater, Conference, Cinema, Tour)

### Logic nghiệp vụ
- **Name unique** để tránh trùng lặp danh mục
- Hỗ trợ **iconUrl** để hiển thị icon trên UI
- Dùng để filter/search events theo category
- Giúp user dễ dàng tìm kiếm sự kiện theo sở thích

---

## 4. Venue Entity
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

## 5. Section Entity
### Tác dụng
Phân khu vực trong venue (VIP Zone, Section A/B/C)

### Logic nghiệp vụ
- **venue_id** *(FK → venues.id, NOT NULL)*: Section phải thuộc về một Venue cụ thể
- **name**: Tên khu vực (VIP Zone, Section A, B, C...)
- **capacity**: Sức chứa của từng section
- **hasNumberedSeats**:
  - `true`: Có ghế đánh số (Section A, B, C)
  - `false`: Standing area (khu đứng)
- Giúp quản lý và pricing theo từng khu vực

---

## 6. Seat Entity
### Tác dụng
Quản lý từng ghế ngồi cụ thể trong venue

### Logic nghiệp vụ
- **venue_id** *(FK → venues.id, NOT NULL)*: Ghế phải thuộc về một Venue. Dùng để query toàn bộ ghế của venue khi hiển thị sơ đồ chỗ ngồi
- **section_id** *(FK → sections.id, NULLABLE)*: Ghế có thể thuộc một Section cụ thể. Nullable vì có venue không phân section
- **rowNumber + seatNumber**: Định danh ghế (VD: A-15 = Hàng A, Ghế 15)
- **seatType**: Phân loại ghế (VIP, REGULAR, WHEELCHAIR)
- Chỉ tồn tại khi venue có seat map (venue.hasSeatMap = true)
- Dùng để hiển thị sơ đồ chỗ ngồi cho user chọn

---

## 7. Event Entity
### Tác dụng
Entity trung tâm - Quản lý sự kiện

### Logic nghiệp vụ
- **category_id** *(FK → event_categories.id, NOT NULL)*: Event phải thuộc một thể loại. Dùng để filter/search event theo danh mục
- **organizer_id** *(FK → users.id, NOT NULL)*: Event phải có người tạo với role ORGANIZER. Chỉ organizer sở hữu mới được chỉnh sửa event
- **venue_id** *(FK → venues.id, NOT NULL)*: Event phải diễn ra tại một địa điểm cụ thể. Dùng để load seat map nếu venue.hasSeatMap = true
- **Status workflow:** DRAFT → PUBLISHED → ONGOING → COMPLETED (hoặc CANCELLED)
- **totalTickets & availableTickets**: Real-time inventory tracking
- **allowTicketExchange**: Flag kiểm soát secondary market
  - `true`: Cho phép user bán/trao đổi vé trên platform
  - `false`: Vé không thể chuyển nhượng (event VIP/restricted)
  - Impact: Nếu false, tất cả tickets của event sẽ có transferable = false
- Lưu hình ảnh: bannerImageUrl, thumbnailImageUrl cho marketing

---

## 8. EventSchedule Entity
### Tác dụng
Quản lý lịch chiếu/suất diễn của event

### Logic nghiệp vụ
- **event_id** *(FK → events.id, NOT NULL)*: Schedule phải thuộc về một Event. Khi Event bị hủy, tất cả schedules của nó cũng phải bị cancel theo
- **startTime, endTime**: Thời gian bắt đầu/kết thúc suất diễn
- **totalSeats, availableSeats**: Quản lý inventory theo từng suất
- **Status:** SCHEDULED → ONGOING → COMPLETED (hoặc CANCELLED)
- Dùng để user chọn suất diễn phù hợp

---

## 9. TicketType Entity
### Tác dụng
Định nghĩa các loại vé và giá cho event

### Logic nghiệp vụ
- **event_id** *(FK → events.id, NOT NULL)*: TicketType phải gắn với một Event cụ thể. Hệ thống validate ticket_type phải thuộc đúng event khi tạo booking, tránh mua nhầm loại vé của event khác
- **name**: Tên loại vé (VIP, Regular, Early Bird...)
- **price**: Giá cho từng loại vé
- **totalQuantity & availableQuantity**: Quản lý inventory theo loại vé
- **maxPerBooking**: Giới hạn số vé/giao dịch (chống mua gom, bot)
- Real-time update availableQuantity khi booking/cancel
- Cho phép phân tầng giá linh hoạt

---

## 10. PromoCode Entity
### Tác dụng
Quản lý mã giảm giá/voucher — hỗ trợ phân quyền Admin/Organizer và phạm vi áp dụng linh hoạt

### Logic nghiệp vụ
- **discountType**: PERCENTAGE (%) hoặc FIXED_AMOUNT (số tiền cố định)
- **discountValue**: Giá trị giảm (20 = 20% hoặc 50000đ)
- **minOrderAmount**: Đơn tối thiểu để áp dụng
- **maxDiscountAmount**: Giới hạn số tiền giảm tối đa (cho PERCENTAGE)
- **usageLimit & usedCount**: Kiểm soát số lần sử dụng
- **validFrom & validTo**: Thời hạn sử dụng
- **Status:** ACTIVE, EXPIRED, DISABLED
- **created_by** *(FK → users.id, NOT NULL)*: Người tạo promo code. Admin tạo GLOBAL, Organizer tạo ORGANIZER_ALL hoặc SPECIFIC_EVENTS
- **applicationType**: Phân loại phạm vi áp dụng:
  - `GLOBAL`: Admin tạo, áp dụng cho **tất cả** event trên hệ thống
  - `ORGANIZER_ALL`: Organizer tạo, áp dụng cho **tất cả event** do organizer đó tổ chức
  - `SPECIFIC_EVENTS`: Organizer tạo, chỉ áp dụng cho **các event cụ thể** (qua bảng PromoCodeEventJoin)
- **Phân quyền CRUD:**
  - ADMIN: Chỉ tạo GLOBAL promo codes. Có quyền xem/sửa/deactivate tất cả promo codes
  - ORGANIZER: Tạo ORGANIZER_ALL hoặc SPECIFIC_EVENTS. Chỉ CRUD promo codes do chính mình tạo (kiểm tra `createdBy.id`)
- Tự động update `usedCount` khi **payment callback SUCCESS** (không phải lúc tạo booking)
- Scheduled job tự động chuyển ACTIVE → EXPIRED khi `validTo` đã qua (chạy hàng ngày lúc 00:00)

---

## 10.1. PromoCodeEventJoin Entity
### Tác dụng
Bảng join many-to-many giữa PromoCode và Event — chỉ dùng khi `applicationType = SPECIFIC_EVENTS`

### Logic nghiệp vụ
- **promo_code_id** *(FK → promo_codes.id, NOT NULL)*: Promo code nào
- **event_id** *(FK → events.id, NOT NULL)*: Event nào được áp dụng
- **Unique constraint:** Một cặp (promo_code_id, event_id) chỉ tồn tại 1 lần
- **Khi nào cần bảng này:**
  - Organizer có 5 events nhưng chỉ muốn áp promo cho event 2 và 4 → cần lưu cụ thể
- **Khi nào KHÔNG cần:**
  - `GLOBAL`: Áp dụng tất cả → không cần lưu gì thêm
  - `ORGANIZER_ALL`: Lọc theo `createdBy.id == event.organizer.id` là đủ
- Khi Organizer cập nhật promo từ SPECIFIC_EVENTS sang loại khác, xóa hết records cũ

---

## 11. Booking Entity
### Tác dụng
Đơn đặt vé - Entity trung tâm của booking flow

### Logic nghiệp vụ
- **customer_id** *(FK → users.id, NOT NULL)*: Booking phải thuộc về một user cụ thể. Dùng để kiểm tra quyền sở hữu khi user xem/hủy booking của mình
- **event_id** *(FK → events.id, NOT NULL)*: Booking gắn với Event để lấy thông tin sự kiện và kiểm tra trạng thái (chỉ PUBLISHED mới được đặt)
- **schedule_id** *(FK → event_schedules.id, NULLABLE)*: Booking gắn với suất diễn cụ thể. Nullable vì một số event không có nhiều suất
- **bookingCode**: Unique identifier để user tra cứu
- **Pricing breakdown:**
  - totalAmount = Tổng tiền gốc
  - discountAmount = Số tiền giảm từ promo code
  - finalAmount = totalAmount - discountAmount
- **Promo code:** Có thể truyền `promoCodeId` khi tạo booking (`POST /api/bookings`). Nếu có, hệ thống validate promo code (status, thời hạn, usage limit, minOrderAmount) **và kiểm tra applicationType** (GLOBAL → tất cả event; ORGANIZER_ALL → đúng organizer; SPECIFIC_EVENTS → đúng event trong bảng PromoCodeEventJoin), tính discount và tạo BookingPromoCode record. `usedCount` chỉ tăng khi thanh toán thành công.
- **Status workflow:** PENDING (hold ghế) → CONFIRMED (đã thanh toán) → CANCELLED/EXPIRED
- **holdExpiresAt**: Timeout tự động release nếu không thanh toán (10-15 phút)
- Background job tự động chuyển PENDING → EXPIRED khi hết timeout

---

## 12. BookingDetail Entity
### Tác dụng
Chi tiết các loại vé trong một booking (Order Line Items)

### Logic nghiệp vụ
- **booking_id** *(FK → bookings.id, NOT NULL)*: Detail phải thuộc về một Booking. Dùng để lấy toàn bộ line items của một đơn đặt
- **ticket_type_id** *(FK → ticket_types.id, NOT NULL)*: Ghi nhận loại vé nào được mua. Dùng để restore inventory (availableQuantity) khi booking bị cancel/expire
- **quantity**: Số lượng vé của loại này trong booking
- **unitPrice**: Snapshot giá tại thời điểm booking — **immutable**, không thay đổi dù giá gốc sau này thay đổi
- **subtotal**: quantity × unitPrice
- Cho phép mua nhiều loại vé khác nhau trong một booking

---

## 13. BookingPromoCode Entity
### Tác dụng
Junction table - Liên kết booking với promo code đã sử dụng

### Logic nghiệp vụ
- **booking_id** *(FK → bookings.id, NOT NULL)*: Ghi nhận promo được apply cho booking nào
- **promo_code_id** *(FK → promo_codes.id, NOT NULL)*: Ghi nhận promo code cụ thể nào đã được dùng. Dùng để báo cáo: "Promo X tạo ra bao nhiêu booking?"
- **discountApplied**: Số tiền thực tế được giảm (sau khi tính toán + cap)
- Audit trail để báo cáo hiệu quả của marketing campaigns

---

## 14. Payment Entity
### Tác dụng
Quản lý giao dịch thanh toán

### Logic nghiệp vụ
- **booking_id** *(FK → bookings.id, NOT NULL + UNIQUE)*: Quan hệ One-to-One với Booking. UNIQUE đảm bảo một booking chỉ có đúng một payment record. Khi payment SUCCESS thì trigger confirm booking và generate tickets
- **paymentMethod**: Hiện tại chỉ hỗ trợ PAYOS
- **Status workflow:** PENDING → SUCCESS/FAILED → REFUNDED
- **payosOrderCode**: UNIQUE, mã đơn hàng trên PayOS (dùng cho reconciliation)
- **paidAt**: Timestamp chính xác khi thanh toán thành công

---

## 15. Ticket Entity
### Tác dụng
Vé điện tử cá nhân - Sản phẩm cuối cùng user nhận được

### Logic nghiệp vụ
- **booking_id** *(FK → bookings.id, NOT NULL)*: Ticket được sinh ra từ Booking đã CONFIRMED. Dùng để truy vấn tất cả vé của một đơn đặt
- **ticket_type_id** *(FK → ticket_types.id, NOT NULL)*: Xác định loại vé (VIP/Regular/...) để hiển thị đúng thông tin trên vé
- **current_owner_id** *(FK → users.id, NOT NULL)*: Chủ sở hữu hiện tại của vé. Ban đầu = người đặt vé, thay đổi khi vé được chuyển nhượng trên secondary market
- **seat_id** *(FK → seats.id, NULLABLE)*: Ghế cụ thể của vé. Nullable vì event không có seat map thì không gắn seat
- **checked_in_by** *(FK → users.id, NULLABLE)*: ID của STAFF đã quét vé. Nullable vì chỉ có giá trị sau khi check-in xảy ra
- **ticketCode**: Human-readable unique identifier
- **qrCode**: Encrypted string/Base64 image để check-in (format: TICKET_ID|EVENT_ID|SCHEDULE_ID|HMAC)
- **transferable**: Flag kiểm soát khả năng chuyển nhượng
  - `true`: Vé có thể list/sell trên marketplace
  - `false`: Vé locked, không thể chuyển (VIP event)
- **checkedIn**: CRITICAL FLAG
  - Khi `true`: Vé đã sử dụng, KHÔNG thể list, KHÔNG thể exchange, KHÔNG thể thay đổi owner
- **checkedInAt**: Timestamp khi vé được quét vào cửa
- Mỗi BookingDetail tạo ra quantity Tickets tương ứng

---

## 16. SeatReservation Entity
### Tác dụng
Quản lý việc hold/reserve ghế - Giải quyết race condition

### Logic nghiệp vụ
- **seat_id** *(FK → seats.id, NOT NULL)*: Ghế nào đang bị hold. Khi check booking mới, query SeatReservation theo seat_id + event_schedule_id + status IN (HOLDING, CONFIRMED) để biết ghế còn trống không
- **event_schedule_id** *(FK → event_schedules.id, NOT NULL)*: Suất diễn nào đang giữ ghế. Cùng ghế A19 nhưng suất khác nhau vẫn độc lập, không ảnh hưởng nhau
- **user_id** *(FK → users.id, NOT NULL)*: User nào đang hold ghế
- **booking_id** *(FK → bookings.id, NULLABLE)*: Booking gắn với reservation này. Nullable vì trong edge case reservation có thể được tạo trước khi booking được lưu thành công
- **Status workflow:** HOLDING (lock 10-15 phút) → CONFIRMED (đã thanh toán) / RELEASED (timeout/cancel)
- **holdExpiresAt**: Timeout tự động release ghế
- **Concurrency control:** Sử dụng database locking (SELECT FOR UPDATE)
- Background job tự động release HOLDING → RELEASED khi hết timeout

---

## 17. TicketListing Entity
### Tác dụng
Secondary market - User đăng vé để bán/trao đổi

### Logic nghiệp vụ
- **ticket_id** *(FK → tickets.id, NOT NULL)*: Vé nào đang được đăng bán/trao đổi. Service layer phải validate ticket.checkedIn = false và ticket.transferable = true trước khi tạo listing
- **seller_id** *(FK → users.id, NOT NULL)*: Người đăng bán. Validate seller phải là currentOwner của ticket, tránh đăng bán vé không phải của mình
- **BUSINESS RULES (validate ở Service Layer):**
  - Ticket KHÔNG thể listed nếu checkedIn = true (đã sử dụng)
  - Ticket KHÔNG thể listed nếu transferable = false
  - Event PHẢI có allowTicketExchange = true
- **exchangeType:** SELL (bán), TRADE (đổi), BOTH (vừa bán vừa đổi)
- **listingPrice:** Giá người bán đặt (market-driven, có thể > hoặc < giá gốc)
- **Status:** FOR_SALE → SOLD/CANCELLED/EXPIRED
- **expiresAt:** Auto-expire listing (VD: 1 ngày trước event)
- Scheduled job tự động expire listings: FOR_SALE → EXPIRED khi `expiresAt` đã qua (chạy hàng giờ)

---

## 18. TicketExchange Entity
### Tác dụng
Ghi nhận giao dịch trao đổi vé trên secondary market

### Logic nghiệp vụ
- **ticket_listing_id** *(FK → ticket_listings.id, NOT NULL)*: Listing nào đang được giao dịch. Validate listing.status = FOR_SALE và chưa expired trước khi tạo exchange
- **seller_id** *(FK → users.id, NOT NULL)*: Người bán — phải trùng với listing.seller_id
- **buyer_id** *(FK → users.id, NOT NULL)*: Người mua — phải khác seller_id, tránh tự mua vé của chính mình
- **trade_ticket_id** *(FK → tickets.id, NULLABLE)*: Chỉ có khi transactionType = TRADE. Vé mà buyer dùng để đổi lại cho seller. Phải validate transferable = true và checkedIn = false
- **payment_id** *(FK → payments.id, NULLABLE)*: Chỉ có khi transactionType = PURCHASE. Nullable vì TRADE không cần payment
- **transactionType:**
  - PURCHASE: Mua bán bằng tiền (buyer → platform → seller)
  - TRADE: Đổi vé với vé (no money, swap ownership)
- **Status workflow:** PENDING → PAYMENT_PENDING → COMPLETED/FAILED/CANCELLED
- **Flow khi COMPLETED:**
  1. Update Ticket.currentOwner = buyer
  2. Create TicketTransferLog
  3. Update TicketListing.status = SOLD
  4. Platform giữ commission, payout cho seller

---

## 19. TicketTransferLog Entity
### Tác dụng
Audit trail - Immutable log cho việc chuyển nhượng vé

### Logic nghiệp vụ
- **ticket_id** *(FK → tickets.id, NOT NULL)*: Vé nào được chuyển nhượng. Dùng để query toàn bộ lịch sử đổi chủ của một vé cụ thể
- **from_user_id** *(FK → users.id, NOT NULL)*: Chủ cũ của vé trước khi chuyển
- **to_user_id** *(FK → users.id, NOT NULL)*: Chủ mới của vé sau khi chuyển
- **ticket_exchange_id** *(FK → ticket_exchanges.id, NULLABLE)*: Liên kết với giao dịch exchange chính thức nếu có. Nullable vì transfer kiểu RETURN (hoàn vé khi event hủy) hoặc UPGRADE không qua secondary market
- **Immutable logging:** Chỉ có createdAt, KHÔNG có updatedAt
- Mỗi lần vé đổi tay → tạo 1 record mới, KHÔNG BAO GIỜ update/delete
- **transferType:** EXCHANGE (secondary market), RETURN (refund), UPGRADE (nâng cấp vé)
- Dùng để:
  - Audit trail: Vé đã đổi tay bao nhiêu lần
  - Fraud detection: Phát hiện flipping quá nhiều lần
  - Chain of custody tracking

---

## 20. OrganizerEWallet Entity
### Tác dụng
Ví điện tử của Organizer - Quản lý doanh thu từ bán vé và rút tiền

### Logic nghiệp vụ
- **user_id** *(FK → users.id, NOT NULL, UNIQUE)*: Mỗi organizer chỉ có 1 ví. OneToOne relationship
- **balance**: Số dư hiện tại (doanh thu chưa rút). Tăng khi payment SUCCESS, giảm khi rút tiền hoặc hoàn vé
- **totalWithdrawn**: Tổng số tiền đã rút về ngân hàng (chỉ tăng, dùng để thống kê)
- **bankName / bankAccountNumber / bankAccountHolder**: Thông tin ngân hàng để rút tiền. Phải cập nhật trước khi rút tiền
- Ví được tạo tự động khi organizer truy cập lần đầu (lazy creation)
- **Flow doanh thu:**
  1. Customer thanh toán booking thành công (Payment.status = SUCCESS)
  2. Hệ thống tự động cộng `booking.finalAmount` vào `balance` của organizer
  3. Tạo WalletTransaction record loại REVENUE
- **Flow rút tiền:**
  1. Organizer gọi API rút tiền
  2. Validate: đủ balance, đã cập nhật bank info
  3. Trừ balance, cộng totalWithdrawn
  4. Tạo WalletTransaction record loại WITHDRAWAL
- **Flow hoàn tiền:**
  1. Khi refund booking → trừ balance organizer
  2. Tạo WalletTransaction record loại REFUND_DEDUCTION
  3. Balance có thể âm nếu organizer đã rút tiền trước khi có refund

---

## 21. WalletTransaction Entity
### Tác dụng
Immutable log ghi nhận mọi biến động số dư ví Organizer

### Logic nghiệp vụ
- **wallet_id** *(FK → organizer_e_wallets.id, NOT NULL)*: Ví liên quan. Dùng để query lịch sử giao dịch của một ví
- **transactionType**: Phân loại giao dịch:
  - `REVENUE`: Doanh thu từ bán vé (tiền vào ví)
  - `WITHDRAWAL`: Rút tiền về tài khoản ngân hàng (tiền ra ví)
  - `REFUND_DEDUCTION`: Trừ tiền khi hoàn vé cho khách (tiền ra ví)
- **amount**: Số tiền giao dịch (luôn dương, precision 15, scale 2)
- **balanceAfter**: Snapshot số dư SAU giao dịch
- **description**: Mô tả chi tiết (VD: "Doanh thu từ booking BK20260303001")
- **referenceCode**: Mã tham chiếu (bookingCode)
- **status**: `SUCCESS`, `PENDING`, `FAILED`
- **Immutable logging:** Chỉ có createdAt, KHÔNG có updatedAt
- Mỗi lần balance thay đổi → tạo 1 record mới, KHÔNG BAO GIỜ update/delete
- Dùng để:
  - Audit trail: Toàn bộ lịch sử biến động ví organizer
  - Đối soát doanh thu
  - Báo cáo tài chính cho organizer

---

## 22. TransactionHistory Entity
### Tác dụng
Lịch sử giao dịch - Immutable log ghi nhận mọi thay đổi trạng thái thanh toán trong hệ thống

### Logic nghiệp vụ
- **payment_id** *(FK → payments.id, NOT NULL)*: Payment liên quan đến giao dịch. Dùng để query toàn bộ lịch sử thay đổi trạng thái của một payment
- **user_id** *(FK → users.id, NOT NULL)*: User thực hiện hoặc liên quan (người mua/người được hoàn tiền)
- **booking_id** *(FK → bookings.id, NULLABLE)*: Booking liên quan để query nhanh mà không cần join qua Payment. Nullable vì giao dịch marketplace có thể không gắn booking trực tiếp
- **transactionType**: Phân loại giao dịch:
  - `PAYMENT`: Thanh toán booking thông thường
  - `REFUND`: Hoàn tiền booking
  - `EXCHANGE_PAYMENT`: Thanh toán mua vé trên marketplace
  - `EXCHANGE_REFUND`: Hoàn tiền giao dịch marketplace
- **status**: Trạng thái giao dịch: `PENDING`, `SUCCESS`, `FAILED`, `CANCELLED`
- **amount**: Số tiền giao dịch (precision 15, scale 2)
- **description**: Mô tả chi tiết giao dịch (TEXT)
- **paymentMethod**: Phương thức thanh toán (PAYOS)
- **Immutable logging:** Chỉ có createdAt, KHÔNG có updatedAt
- Mỗi lần payment chuyển trạng thái → tạo 1 record mới, KHÔNG BAO GIỜ update/delete
- **Các thời điểm ghi log:**
  1. Khi tạo payment mới (PAYMENT + PENDING)
  2. Khi thanh toán thành công (PAYMENT + SUCCESS)
  3. Khi thanh toán thất bại (PAYMENT + FAILED)
  4. Khi hủy thanh toán (PAYMENT + CANCELLED)
  5. Khi hoàn tiền (REFUND + SUCCESS)
  6. Khi mua vé trên marketplace (EXCHANGE_PAYMENT + status tương ứng)
- Dùng để:
  - Audit trail: Toàn bộ lịch sử thanh toán của user
  - Báo cáo doanh thu, giao dịch
  - Reconciliation với payment gateway
  - Hỗ trợ dispute/chargeback
# Ticket Booking Platform - Idea Overview

---

## **1. Các loại vé và sự kiện**

### **Phân loại sự kiện**
- Concert  
- Sân khấu  
- Thể thao  
- Hội thảo  
- Cinema  
- Tour du lịch  

### **Cấu trúc vé**
- Vé đơn  
- Vé combo  
- Vé VIP / Vé thường  
- Early Bird  

### **Thời gian**
- Sự kiện một lần  
- Sự kiện định kỳ  
- Nhiều suất chiếu  

---

## **2. Quản lý chỗ ngồi / Slot**

### **Sơ đồ chỗ ngồi**
- Ghế có số cố định (concert hall, rạp phim)  
- Không số ghế (standing area)  

### **Phân khu**
- VIP  
- Thường  
- Hạng A / B / C (giá khác nhau)  

### **Trạng thái ghế**
- **Available**  
- **Reserved** (tạm giữ)  
- **Sold**  
- **Blocked**  

### **Thời gian giữ ghế**
- Khi user chọn ghế → hệ thống **lock tạm thời (10–15 phút)** để thanh toán  

---

## **3. Luồng đặt vé (Booking Flow)**

1. Tìm kiếm sự kiện (theo địa điểm, thời gian, thể loại)  
2. Chọn sự kiện và suất diễn  
3. Chọn loại vé / khu vực  
4. Chọn ghế cụ thể (nếu có sơ đồ chỗ ngồi)  
5. **Hold ghế tạm thời** (tránh overselling)  
6. Nhập thông tin khách hàng  
7. Thanh toán  
8. Xác nhận và gửi vé điện tử  

---

## **4. Xử lý thanh toán**

### **Payment Gateway**
- VNPay  
- Momo  
- ZaloPay  
- Stripe  
- PayPal  

### **Trạng thái thanh toán**
- **Pending**  
- **Success**  
- **Failed**  
- **Refunded**  

### **Timeout**
- Hủy booking nếu không thanh toán trong thời gian quy định  

### **Refund Logic**
- Trước 7 ngày: **100%**  
- Trước 3 ngày: **50%**  

---

## **5. Quản lý tồn kho (Inventory)**

### **Overselling Prevention**
- Xử lý đồng thời nhiều user đặt cùng ghế  

### **Release Mechanism**
- Tự động release ghế đã hold nhưng không thanh toán  

### **Quota Management**
- Giới hạn số vé per user  
- Giới hạn số vé per transaction  

---

## **6. Vé điện tử (E-ticket)**

### **QR Code / Barcode**
- Unique identifier cho mỗi vé  

### **Thông tin vé**
- Tên sự kiện  
- Thời gian  
- Địa điểm  
- Số ghế  
- Mã booking  

### **Check-in**
- Quét QR khi vào cửa  
- Đánh dấu vé đã sử dụng  

### **Duplicate Prevention**
- Một vé chỉ check-in được **1 lần**  

---

## **7. Vai trò người dùng (User Roles)**

### **Customer**
- Đặt vé  
- Xem lịch sử  
- Hủy vé  

### **Event Organizer**
- Tạo / quản lý sự kiện  
- Thiết lập giá  
- Xem báo cáo  

### **Admin**
- Quản lý toàn hệ thống  

### **Gate Staff**
- Quét vé check-in tại cổng vào  

---

## **8. Các tính năng bổ sung**

### **Promo Code / Voucher**
- Giảm giá theo code  

### **Loyalty Program**
- Tích điểm  
- Ranking khách hàng  

### **Notification**
- Email xác nhận  
- SMS xác nhận  
- Nhắc nhở sự kiện  

### **Waitlist**
- Danh sách chờ khi sold out  

### **Transfer Ticket**
- Chuyển vé cho người khác  

---

## **9. Xử lý đồng thời (Concurrency)**

### **Locking**
- Optimistic Locking  
- Pessimistic Locking  

### **Queue System**
- Xử lý traffic cao trong đợt mở bán  

### **Rate Limiting**
- Chống spam  
- Chống bot  

---

## **10. Báo cáo và phân tích**

- Doanh thu theo sự kiện  
- Doanh thu theo thời gian  
- Số vé bán  
- Tỷ lệ lấp đầy  
- Thống kê hủy vé  
- Thống kê refund  
- Phân tích hành vi khách hàng  

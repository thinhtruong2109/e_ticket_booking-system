# Actors in Ticket Booking System

---

## **1. Guest (Khách vãng lai)**

### **Quyền hạn**
- Xem danh sách sự kiện công khai  
- Tìm kiếm sự kiện  
- Xem chi tiết sự kiện  

### **Giới hạn**
- Không thể đặt vé (bắt buộc đăng ký / đăng nhập)

---

## **2. Customer (Khách hàng đã đăng ký)**

### **Bao gồm tất cả quyền của Guest**

### **Chức năng**
- Đặt vé (chọn vé, chọn ghế, thanh toán)  
- Xem lịch sử đặt vé  
- Hủy vé (theo chính sách)  
- Tải / xem vé điện tử (QR code)  
- Quản lý thông tin cá nhân  
- Sử dụng promo code / voucher (nếu có)  
- Chuyển vé cho người khác (nếu hệ thống hỗ trợ)  

---

## **3. Event Organizer (Nhà tổ chức sự kiện)**

### **Quản lý sự kiện**
- Tạo / sửa / xóa sự kiện  
- Thiết lập thông tin sự kiện (tên, mô tả, thời gian, địa điểm)  
- Cấu hình loại vé và giá  
- Thiết lập sơ đồ chỗ ngồi / khu vực  
- Quản lý inventory (số lượng vé)  

### **Quản lý kinh doanh**
- Xem báo cáo bán vé theo sự kiện của mình  
- Tạo promo code cho sự kiện riêng  
- Xem danh sách người đặt vé  
- Export dữ liệu check-in  

---

## **4. Gate Staff / Scanner (Nhân viên soát vé)**

### **Chức năng**
- Quét QR code vé tại cổng vào  
- Check-in vé (đánh dấu đã sử dụng)  
- Xem trạng thái vé:
  - **Valid**
  - **Đã sử dụng**
  - **Fake**
  - **Expired**
- Xử lý vé trùng / gian lận  
- Thống kê số người đã vào  

---

## **5. Admin (Quản trị viên)**

### **Quyền cao nhất trong hệ thống**

### **Quản lý người dùng**
- Quản lý Customer  
- Quản lý Organizer  
- Quản lý Staff  

### **Quản lý sự kiện**
- Quản lý toàn bộ sự kiện  
- Phê duyệt / từ chối sự kiện (nếu có quy trình duyệt)  

### **Quản lý vận hành**
- Xử lý khiếu nại, tranh chấp  
- Quản lý payment gateway  
- Tạo / quản lý promo code toàn hệ thống  
- Xem tất cả báo cáo, thống kê  
- Cấu hình hệ thống (phí, chính sách hoàn tiền, timeout...)  
- Quản lý nội dung (banner, categories...)  

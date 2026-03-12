# DevOps Journey - E-Ticket Booking System

---

## 1. Chuẩn bị Dockerfile

### Backend (Spring Boot)

Dockerfile backend sử dụng multi-stage build để tối ưu image size:

- **Stage 1 - Build:** Dùng image `maven:3.9.13-eclipse-temurin-21`, copy `pom.xml` và `src/`, chạy `mvn clean package -DskipTests` để build file `.jar`
- **Stage 2 - Runtime:** Dùng image `eclipse-temurin:21-jre-jammy` (nhẹ hơn), copy file `.jar` từ stage build sang, expose port `8080`, chạy bằng `ENTRYPOINT ["java", "-jar", "app.jar"]`

### Frontend (React + Vite)

Dockerfile frontend cũng dùng multi-stage build:

- **Stage 1 - Build:** Dùng `node` image, chạy `npm install` và `npm run build` để tạo ra thư mục `dist/`
- **Stage 2 - Runtime:** Dùng `nginx:alpine`, copy thư mục `dist/` vào thư mục serve của Nginx, expose port `80`

---

## 2. Thiết lập Docker Compose

File `docker-compose.yml` ở root project định nghĩa các services chạy cùng nhau:

- **postgres** - Database PostgreSQL, mount volume để dữ liệu không mất khi restart container, expose port `5432`
- **backend** - Build từ Dockerfile backend, phụ thuộc vào `postgres`, expose port `8080`, load biến môi trường từ file `.env`
- **frontend** - Build từ Dockerfile frontend, phụ thuộc vào `backend`, expose port `80` (hoặc `3000`)

Cấu hình kết nối database trong `application.properties` dùng hostname `postgres` (tên service trong Docker Compose) thay vì `localhost`:

```
spring.datasource.url=jdbc:postgresql://postgres:5432/e-ticket-booking
```

---

## 3. Thiết lập file .env

Tách biệt cấu hình nhạy cảm ra file `.env` ở root project, không commit lên Git:

- Biến môi trường cho database: `POSTGRES_USER`, `POSTGRES_PASSWORD`, `POSTGRES_DB`
- Biến môi trường cho backend: JWT secret, PayOS keys, mail credentials
- Biến môi trường cho frontend: `VITE_API_BASE_URL` trỏ về backend URL thực tế khi deploy

File `.env` frontend ban đầu dùng `http://localhost:8080` cho môi trường local, sau khi deploy lên AWS phải cập nhật thành URL thực của server.

---

## 4. Triển khai lên AWS EC2

### 4.1. Tạo EC2 Instance

- Chọn AMI: **Ubuntu Server 22.04 LTS**
- Instance type: `t2.micro` (free tier) hoặc `t2.small` tùy tải
- Tạo hoặc chọn **Key Pair** (.pem file) để SSH vào server
- Lưu file `.pem` ở máy local, chmod 400 để bảo mật

### 4.2. Cấu hình Security Group

Mở các inbound rules sau trong Security Group của EC2:

- Port `22` (SSH) - source: My IP hoặc Anywhere (0.0.0.0/0)
- Port `80` (HTTP) - source: Anywhere
- Port `443` (HTTPS) - source: Anywhere
- Port `8080` (Backend API tạm thời, sau khi có Nginx thì đóng lại) - tuỳ chọn

### 4.3. SSH vào EC2

```bash
ssh -i "ten-file.pem" ubuntu@<EC2_PUBLIC_IP>
```

### 4.4. Cài đặt môi trường trên EC2

Cập nhật package và cài Docker, Docker Compose:

```bash
sudo apt update && sudo apt upgrade -y

# Cài Docker
sudo apt install -y docker.io

# Cài Docker Compose
sudo apt install -y docker-compose

# Thêm user ubuntu vào group docker (không cần sudo mỗi lần)
sudo usermod -aG docker ubuntu
newgrp docker
```

---

## 5. Đưa code lên EC2

Có hai cách chính:

**Cách 1 - Clone từ GitHub:**

```bash
git clone https://github.com/<username>/<repo>.git
cd <repo>
```

**Cách 2 - Copy file bằng SCP:**

```bash
scp -i "ten-file.pem" -r ./project ubuntu@<EC2_IP>:~/
```

Sau khi có code trên server, tạo file `.env` thực tế (không commit lên Git) với các giá trị production.

---

## 6. Build và chạy Container

### 6.1. Build images và chạy tất cả services

```bash
docker-compose up -d --build
```

- `-d`: chạy ở background (detach mode)
- `--build`: bắt buộc build lại images từ Dockerfile

### 6.2. Kiểm tra container đang chạy

```bash
docker ps
```

### 6.3. Xem logs của từng service

```bash
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f postgres
```

### 6.4. Restart một service cụ thể

```bash
docker-compose restart backend
```

### 6.5. Dừng và xóa toàn bộ containers

```bash
docker-compose down
```

Thêm `-v` nếu muốn xóa cả volumes (mất data database):

```bash
docker-compose down -v
```

---

## 7. Thiết lập Nginx làm Reverse Proxy

Mục tiêu: người dùng truy cập domain, Nginx nhận request và forward về đúng service (frontend port 80, backend port 8080).

### 7.1. Cài Nginx trên EC2

```bash
sudo apt install -y nginx
```

### 7.2. Tạo file cấu hình Nginx

Tạo file tại `/etc/nginx/sites-available/e-ticket`:

```nginx
server {
    listen 80;
    server_name yourdomain.com www.yourdomain.com;

    # Frontend
    location / {
        proxy_pass http://localhost:3000;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_cache_bypass $http_upgrade;
    }

    # Backend API
    location /api/ {
        proxy_pass http://localhost:8080;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

### 7.3. Kích hoạt cấu hình

```bash
# Tạo symlink sang sites-enabled
sudo ln -s /etc/nginx/sites-available/e-ticket /etc/nginx/sites-enabled/

# Kiểm tra cú pháp cấu hình
sudo nginx -t

# Reload Nginx
sudo systemctl reload nginx
```

---

## 8. Trỏ tên miền về EC2

### 8.1. Lấy Public IP của EC2

Vào AWS Console, tab EC2 Instances, copy **Public IPv4 address**.

### 8.2. Cấu hình DNS tại nhà cung cấp domain

Vào trang quản lý domain (Namecheap, GoDaddy, Inet, PA Vietnam, v.v.), thêm DNS records:

- **A record**: `@` (root domain) trỏ về `<EC2_PUBLIC_IP>`
- **A record**: `www` trỏ về `<EC2_PUBLIC_IP>`

DNS thường mất 5 phút đến 24 giờ để propagate toàn cầu.

### 8.3. Kiểm tra DNS đã propagate chưa

```bash
nslookup yourdomain.com
# hoặc
ping yourdomain.com
```

---

## 9. Cài đặt SSL/HTTPS với Certbot (Let's Encrypt)

### 9.1. Cài Certbot

```bash
sudo apt install -y certbot python3-certbot-nginx
```

### 9.2. Tạo SSL certificate

```bash
sudo certbot --nginx -d yourdomain.com -d www.yourdomain.com
```

Certbot sẽ tự động:
- Tạo certificate miễn phí từ Let's Encrypt
- Chỉnh sửa file cấu hình Nginx để thêm HTTPS (port 443)
- Redirect HTTP sang HTTPS tự động

### 9.3. Kiểm tra auto-renewal

Certificate Let's Encrypt có hiệu lực 90 ngày, Certbot tự cấu hình cronjob để renew:

```bash
sudo certbot renew --dry-run
```

---

## 10. Cập nhật biến môi trường sau khi có domain

Sau khi có HTTPS và domain thực, cần cập nhật các biến trong `.env` và `application.properties`:

- `VITE_API_BASE_URL` trong `.env` frontend: đổi từ `http://localhost:8080` thành `https://yourdomain.com`
- `payos.return-url` và `payos.cancel-url` trong `application.properties`: đổi thành domain thực
- `app.frontend-url`: đổi thành `https://yourdomain.com`

Sau khi cập nhật, rebuild containers:

```bash
docker-compose down
docker-compose up -d --build
```

---

## 11. Xử lý các vấn đề thường gặp

### Container backend không start được

Nguyên nhân thường gặp: postgres chưa sẵn sàng khi backend khởi động. Giải pháp: thêm `healthcheck` cho postgres trong `docker-compose.yml` hoặc dùng `depends_on` với condition `service_healthy`.

### Lỗi CORS

Backend Spring Boot cần cấu hình CORS cho phép frontend domain gọi API. Cập nhật `CorsConfig` cho phép origin là domain thực.

### Lỗi 502 Bad Gateway từ Nginx

Kiểm tra container backend/frontend có đang chạy không (`docker ps`), kiểm tra port trong cấu hình Nginx có khớp với port container đang expose không.

### Xem logs để debug

```bash
# Logs Nginx
sudo tail -f /var/log/nginx/error.log
sudo tail -f /var/log/nginx/access.log

# Logs Docker containers
docker-compose logs -f --tail=100
```

---

## Tóm tắt luồng triển khai

1. Viết Dockerfile cho backend và frontend
2. Thiết lập docker-compose.yml kết nối các services
3. Tạo EC2 instance trên AWS, cấu hình Security Group
4. SSH vào EC2, cài Docker và Docker Compose
5. Clone code lên server, tạo file .env production
6. Chạy `docker-compose up -d --build`
7. Cài Nginx, viết cấu hình reverse proxy
8. Trỏ domain về IP của EC2
9. Cài Certbot để có HTTPS miễn phí
10. Cập nhật biến môi trường với domain thực, rebuild containers
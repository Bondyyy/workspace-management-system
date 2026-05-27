<p align="center">
  <a href="https://www.uit.edu.vn/" title="Trường Đại học Công nghệ Thông tin" style="border: 5;">
    <img src="https://i.imgur.com/WmMnSRt.png" alt="Trường Đại học Công nghệ Thông tin | University of Information Technology">
  </a>
</p>

<!-- Title -->
<h1 align="center"><b>IS216 - LẬP TRÌNH JAVA</b></h1>



## BẢNG MỤC LỤC
* [ Giới thiệu môn học](#gioithieumonhoc)
* [ Giảng viên hướng dẫn](#giangvien)
* [ Thành viên nhóm](#thanhvien)
* [ Đồ án môn học](#doan)
* [ Công nghệ sử dụng](#congnghe)
* [ Hướng dẫn cài đặt](#caidat)


## GIỚI THIỆU MÔN HỌC
<a name="gioithieumonhoc"></a>
* **Tên môn học**: Lập trình Java - Java Programming
* **Mã môn học**: IS216
* **Lớp học**: IS216.Q23
* **Năm học**: 2025-2026


## GIẢNG VIÊN HƯỚNG DẪN
<a name="giangvien"></a>
* ThS. **Tạ Việt Phương** - *phuongtv@uit.edu.vn*


## THÀNH VIÊN NHÓM
<a name="thanhvien"></a>
| STT    | MSSV          | Họ và Tên              | Email                   |
| ------ |:-------------:| ----------------------:|-------------------------:
| 1      | 24520322      | Nguyễn Thành Đức       |24520322@gm.uit.edu.vn   |
| 2      | 24520409      | Sơn Nguyễn Kỳ Duyên    |24520409@gm.uit.edu.vn   |
| 3      | 24520336      | Huỳnh Đức Dũng         |24520336@gm.uit.edu.vn   |
| 4      | 24520663      | Lai Mộc Huy            |laimochuy@gmail.com      |


## ĐỒ ÁN MÔN HỌC
<a name="doan"></a>
Đồ án nhóm: Hệ thống quản lý không gian học tập và làm việc.


## CÔNG NGHỆ SỬ DỤNG
<a name="congnghe"></a>
* **Ngôn ngữ**: Java 17
* **Framework**: Spring Boot, Spring MVC, Thymeleaf
* **Giao diện**: Java Swing, HTML/CSS/JavaScript
* **Cơ sở dữ liệu**: Oracle Database, JDBC
* **Build tool**: Maven
* **Thư viện hỗ trợ**: JasperReports, iTextPDF, ZXing, BCrypt


## HƯỚNG DẪN CÀI ĐẶT
<a name="caidat"></a>
1. Clone project về máy và đi vào thư mục gốc của project:
```bash
git clone <repository-url>
cd Sem4
```

2. Tạo user/schema Oracle Database riêng cho project WMS, sau đó mở SQLcl hoặc SQL*Plus và kết nối tới schema đó:
```bash
sql /nolog
CONNECT <username>/<password>@<host>:<port>/<service_name>
```

Hoặc:
```bash
sqlplus <username>/<password>@<host>:<port>/<service_name>
```

3. Từ thư mục gốc `Sem4`, chạy các file SQL trong thư mục `Database` theo đúng thứ tự thư mục:
```text
Database/01_table
Database/02_constraint
Database/03_function
Database/04_procedures
Database/05_triggers
Database/06_migrations
```

Trong đó:
* `01_table`: tạo bảng và mã tự động.
* `02_constraint`: tạo ràng buộc dữ liệu.
* `03_function`: tạo các function tính toán.
* `04_procedures`: tạo stored procedure.
* `05_triggers`: tạo trigger xử lý nghiệp vụ tự động.

4. Quay lại thư mục gốc project `Sem4`, cấu hình kết nối Oracle Database trong file `db.properties`:
```properties
db.url=jdbc:oracle:thin:@<host>:<port>/<service_name>
db.username=<username>
db.password=<password>
```

5. Build project bằng Maven:
```bash
mvn clean package
```

6. Chạy ứng dụng từ thư mục gốc `Sem4`:
```bash
mvn spring-boot:run
```

7. Truy cập cổng thông tin web tại:
```text
http://localhost:8080
```


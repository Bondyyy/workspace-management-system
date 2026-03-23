MODULE    : report
MUC DICH  : Xuat bao cao theo yeu cau SOW muc 1.3 (Bao cao Doanh thu & Ty le lap day)
MAP ORACLE: Query tong hop tu INVOICES, SESSIONS, SESSION_ORDER_DETAILS, SPACES

FILES CAN TAO
=============

controller/ReportController.java
  - GET /api/reports/daily?date=YYYY-MM-DD
      Doanh thu trong ngay, tach ro: nguon tu SPACE vs FNB
  - GET /api/reports/occupancy?from=&to=
      Ty le lap day theo khung gio (Vi du: 8h-10h: 80%, 10h-12h: 95%)
  - GET /api/reports/fnb-top?limit=10
      Top mon ban chay nhat trong thang
  - GET /api/reports/shift?employeeId=&date=
      Bao cao theo ca lam viec cua nhan vien
  - GET /api/reports/daily/export?date=
      Tra ve file CSV de quan ly doi soat cuoi ngay

service/ReportService.java
  - getDailyRevenue(date)
      SELECT type, SUM(amount) FROM invoice_lines
      WHERE DATE(created_at) = date GROUP BY type

  - getOccupancyRate(from, to)
      Dem Session theo tung khung gio / tong so Space
      Tra ve Map<TimeSlot, Double> ty le phan tram

  - getTopFnbItems(limit)
      SELECT item_id, SUM(quantity) FROM session_order_details
      GROUP BY item_id ORDER BY SUM(quantity) DESC LIMIT limit

  - exportToCsv(reportData)
      Dung Apache Commons CSV
      Them dependency: commons-csv:1.10.0 vao pom.xml

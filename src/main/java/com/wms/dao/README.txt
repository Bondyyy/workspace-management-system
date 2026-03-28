THU MUC: dao/ (Data Access Object)
====================================
Tang 3 trong kien truc 3 tang: chua cac class TRUC TIEP truy van Oracle bang JDBC.
Day la noi DUY NHAT duoc viet SQL trong toan bo du an.

CACH VIET MOT PHUONG THUC DAO (TEMPLATE)
==========================================

public Space findById(int spaceId) throws SQLException {
    String sql = 'SELECT * FROM SPACES WHERE SPACE_ID = ?';
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setInt(1, spaceId);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                Space s = new Space();
                s.setSpaceId(rs.getInt('SPACE_ID'));
                s.setName(rs.getString('SPACE_NAME'));
                s.setPricePerHour(rs.getDouble('PRICE_PER_HOUR'));
                return s;
            }
        }
    }
    return null;
}

QUY TAC BAT BUOC
================
  1. Dung PreparedStatement (? thay vi ghep chuoi) -> CHONG SQL INJECTION
  2. Dung try-with-resources -> tu dong close PreparedStatement va ResultSet
  3. Ten cot Oracle VIET_HOA: rs.getString('SPACE_NAME')
  4. KHONG dong Connection - de DatabaseConnection Singleton quan ly
  5. Moi method throws SQLException -> Controller tu xu ly loi

DANH SACH FILE VA NHIEM VU
===========================

UserDAO.java
  findByUsername(String) -> User         Tim user de dang nhap
  checkLogin(String, String) -> boolean  Kiem tra username + passwordHash
  findById(int) -> User                  Lay thong tin user hien tai

CustomerDAO.java
  register(Customer) -> int              Dang ky tai khoan moi
  findById(int) -> Customer              Lay thong tin + diem tich luy
  addPoints(int, int) -> void            Cong diem sau thanh toan
  upgradeTier(int) -> void               Kiem tra va nang hang thanh vien

BranchDAO.java
  findAll() -> List<Branch>              Danh sach chi nhanh
  findById(int) -> Branch

SpaceDAO.java
  findByBranch(int) -> List<Space>       Tat ca ban cua 1 chi nhanh (cho so do)
  findAvailable(int) -> List<Space>      Chi ban AVAILABLE (cho khach chon)
  updateStatus(int, SpaceStatus) -> void Doi trang thai (BOOKED/OCCUPIED/CLEANING...)
  findById(int) -> Space

BookingDAO.java
  create(Booking, BookingDetail) -> int  TAO DON - CO SELECT FOR UPDATE CHONG DOUBLE-BOOKING
    -- Thuat toan:
    -- 1. SELECT SPACE_ID FROM SPACES WHERE SPACE_ID=? AND STATUS='AVAILABLE' FOR UPDATE
    -- 2. Neu khong tim thay -> throw BookingConflictException
    -- 3. INSERT INTO BOOKINGS...
    -- 4. INSERT INTO BOOKING_DETAILS... (luu priceAtBooking = gia hien tai)
    -- 5. UPDATE SPACES SET STATUS='BOOKED'
  findByQrCode(String) -> Booking        Le tan quet QR -> lay booking
  markQrUsed(int) -> void               Danh dau QR da dung 1 lan
  updateStatus(int, BookingStatus)
  cancelExpired() -> void               @Scheduled: huy PENDING qua 10 phut

SessionDAO.java
  checkin(int bookingId) -> Session      Tao Session + Space->OCCUPIED + Booking->ACTIVE
  findActive() -> List<Session>          Danh sach phien dang chay (man hinh POS)
  extend(int, int) -> SessionExtension   Gia han: them minutes, tinh extra_cost
  checkout(int) -> Session               Space->CLEANING, tinh total_duration
  findById(int) -> Session

MenuDAO.java
  findAllCategories() -> List<MenuCategory>
  findByCategory(int) -> List<MenuItem>  Menu theo danh muc
  findAvailable() -> List<MenuItem>      Chi mon dang phuc vu (is_available=1)

OrderDAO.java
  createOrder(int sessionId) -> int orderId
  addItem(int, int, int) -> SessionOrderDetail
    subtotal = qty * unitPrice
    UPDATE SESSION_ORDERS SET TOTAL_PRICE = TOTAL_PRICE + subtotal  (DELTA)
  updateItem(int, int) -> void
    delta = (newQty - oldQty) * unitPrice
    UPDATE SESSION_ORDERS SET TOTAL_PRICE = TOTAL_PRICE + delta     (DELTA)
  removeItem(int) -> void
    UPDATE SESSION_ORDERS SET TOTAL_PRICE = TOTAL_PRICE - subtotal  (DELTA)
  findBySession(int) -> List<SessionOrder>

InvoiceDAO.java
  generate(int sessionId) -> Invoice     Tao hoa don tong hop:
    -- LINE SPACE:     duration * (priceAtBooking / 60.0)
    -- LINE FNB:       SUM(SESSION_ORDERS.TOTAL_PRICE)
    -- LINE EXTENSION: SUM(SESSION_EXTENSIONS.EXTRA_COST)
    -- LINE DEPOSIT:   -depositAmount (am - tru coc)
    -- LINE VOUCHER:   -fn_CalculateDiscountAmount() (am - giam gia)
    -- total = SUM tat ca lines
  applyVoucher(int, String) -> double    Goi Oracle function fn_CalculateDiscountAmount
  findBySession(int) -> Invoice

PaymentDAO.java
  create(int, double, PaymentMethod) -> Payment
  updateStatus(int, String, String) -> void  Cap nhat sau callback VNPay

VoucherDAO.java
  findByCode(String) -> Voucher
  isValid(String) -> boolean            IS_ACTIVE=1 AND EXPIRED_AT>SYSDATE AND USED_COUNT<USAGE_LIMIT
  incrementUsed(int) -> void            USED_COUNT++ sau khi ap dung

ReportDAO.java
  getDailyRevenue(Date) -> Map<String,Double>    Doanh thu theo ngay, tach SPACE vs FNB
  getOccupancyByHour(Date) -> List<Object[]>     Ty le lap day theo khung gio
  getTopFnbItems(int) -> List<Object[]>          Top mon ban chay
  exportToCsv(Date, String filePath) -> void     Xuat bao cao ra file CSV

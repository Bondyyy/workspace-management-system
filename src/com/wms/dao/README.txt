THU MUC: dao/ (Data Access Object)
====================================
Chua cac class truy van truc tiep Oracle bang JDBC.
Day la noi DUY NHAT duoc phep viet SQL va xu ly ResultSet.

QUY TAC VIET DAO
================
  1. Lay ket noi: Connection conn = DatabaseConnection.getInstance().getConnection()
  2. Tao PreparedStatement voi SQL co tham so: ?
  3. Dien tham so: ps.setString(1, username)
  4. Thuc thi: ResultSet rs = ps.executeQuery()
  5. Doc ResultSet: User u = new User(); u.setId(rs.getInt("USER_ID"))
  6. Dong tai nguyen trong finally: rs.close(), ps.close()
  7. KHONG dong Connection (de DatabaseConnection quan ly)

TEN COT ORACLE
==============
  Oracle dung TEN_COT_VIET_HOA: USER_ID, FULL_NAME, PRICE_PER_HOUR
  Java dung camelCase: userId, fullName, pricePerHour
  Khi doc ResultSet: rs.getString("USER_ID") -> dat vao user.setUserId()

DANH SACH FILE
==============

UserDAO.java
  - findByUsername(String username) -> User
    SQL: SELECT * FROM USERS WHERE USERNAME = ?
  - findById(int userId) -> User
  - checkPassword(String username, String hashedPassword) -> boolean
    SQL: SELECT COUNT(*) FROM USERS WHERE USERNAME=? AND PASSWORD_HASH=?
  - updateLastLogin(int userId)
  - Dung boi: LoginController de xac thuc dang nhap

SpaceDAO.java
  - findAll(int branchId) -> List<Space>
    SQL: SELECT * FROM SPACES WHERE BRANCH_ID = ? ORDER BY SPACE_NAME
  - findAvailable(int branchId) -> List<Space>
    SQL: SELECT * FROM SPACES WHERE BRANCH_ID=? AND STATUS='AVAILABLE'
  - findById(int spaceId) -> Space
  - updateStatus(int spaceId, SpaceStatus status)
    SQL: UPDATE SPACES SET STATUS=? WHERE SPACE_ID=?
  - Dung boi: SpaceMapController hien thi so do mat bang

BranchDAO.java
  - findAll() -> List<Branch>
  - findById(int branchId) -> Branch
  - Dung boi: man hinh chon chi nhanh

BookingDAO.java
  - create(Booking b, BookingDetail d) -> int bookingId
    LUU Y: Dung SELECT FOR UPDATE de chong double-booking
    SQL: SELECT SPACE_ID FROM SPACES WHERE SPACE_ID=? AND STATUS='AVAILABLE' FOR UPDATE
    Neu space da bi chiem: throw BookingConflictException
    Sau do: INSERT INTO BOOKINGS..., INSERT INTO BOOKING_DETAILS..., UPDATE SPACES SET STATUS='BOOKED'
  - findById(int bookingId) -> Booking
  - findByCustomerId(int customerId) -> List<Booking>
  - findByQrCode(String qrCode) -> Booking
    SQL: SELECT * FROM BOOKINGS WHERE QR_CODE = ?  (dung khi Le tan quet QR)
  - updateStatus(int bookingId, BookingStatus status)
  - cancelExpiredBookings()
    SQL: UPDATE BOOKINGS SET STATUS='CANCELLED' WHERE STATUS='PENDING'
         AND CREATED_AT < SYSDATE - INTERVAL '10' MINUTE

SessionDAO.java
  - checkin(int bookingId) -> Session
    Tao Session moi, UPDATE SPACES STATUS='OCCUPIED', UPDATE BOOKINGS STATUS='ACTIVE'
  - findById(int sessionId) -> Session
  - findActive() -> List<Session>
    SQL: SELECT * FROM SESSIONS WHERE STATUS='ACTIVE'  (man hinh POS)
  - extend(int sessionId, int minutes) -> SessionExtension
    INSERT INTO SESSION_EXTENSIONS(session_id, extended_minutes, extra_cost)
    extra_cost = minutes * (pricePerHour / 60.0)
  - checkout(int sessionId) -> Session
    UPDATE SESSIONS SET CHECKOUT_TIME=SYSDATE, STATUS='COMPLETED'
    UPDATE SPACES SET STATUS='CLEANING'
    Goi tinh total_duration_minutes = TIMESTAMPDIFF phut

MenuDAO.java
  - findAllCategories() -> List<MenuCategory>
  - findItemsByCategory(int categoryId) -> List<MenuItem>
  - findAllAvailable() -> List<MenuItem>
    SQL: SELECT * FROM MENU_ITEMS WHERE IS_AVAILABLE = 1

OrderDAO.java
  - createOrder(int sessionId) -> int orderId
  - addItem(int orderId, int itemId, int qty) -> SessionOrderDetail
    subtotal = qty * unitPrice
    UPDATE SESSION_ORDERS SET TOTAL_PRICE = TOTAL_PRICE + subtotal
    LUU Y: THUAT TOAN DELTA - cong them, khong tinh lai tu dau
  - updateItem(int detailId, int newQty)
    delta = (newQty - oldQty) * unitPrice
    UPDATE SESSION_ORDERS SET TOTAL_PRICE = TOTAL_PRICE + delta
  - removeItem(int detailId)
    UPDATE SESSION_ORDERS SET TOTAL_PRICE = TOTAL_PRICE - subtotal
    DELETE FROM SESSION_ORDER_DETAILS WHERE DETAIL_ID = ?
  - findBySession(int sessionId) -> List<SessionOrder>

InvoiceDAO.java
  - generate(int sessionId) -> Invoice
    Tao Invoice, insert cac InvoiceLine theo thu tu:
    1. LINE SPACE   : durationMin * (priceAtBooking / 60.0)
    2. LINE FNB     : SUM(SESSION_ORDERS.TOTAL_PRICE) WHERE SESSION_ID=?
    3. LINE EXT     : SUM(SESSION_EXTENSIONS.EXTRA_COST) WHERE SESSION_ID=?
    4. LINE DEPOSIT : -depositAmount (so am - tru tien coc)
    5. LINE VOUCHER : -discountAmount (so am - tinh qua fn_CalculateDiscountAmount)
    total = SUM tat ca InvoiceLine.amount
  - findBySession(int sessionId) -> Invoice
  - applyVoucher(int invoiceId, String code) -> double discountAmount
    Goi Oracle function: fn_CalculateDiscountAmount (da co trong Database/03_functions)

PaymentDAO.java
  - create(int invoiceId, double amount, PaymentMethod method) -> Payment
  - updateStatus(int paymentId, String status, String transactionId)
  - findByInvoiceId(int invoiceId) -> Payment

VoucherDAO.java
  - findByCode(String code) -> Voucher
  - validateVoucher(String code) -> boolean
    Kiem tra: IS_ACTIVE=1, EXPIRED_AT > SYSDATE, USED_COUNT < USAGE_LIMIT
  - incrementUsedCount(int voucherId)
    UPDATE VOUCHERS SET USED_COUNT = USED_COUNT + 1 WHERE VOUCHER_ID = ?

CustomerDAO.java
  - addLoyaltyPoints(int customerId, int points)
    UPDATE CUSTOMERS SET LOYALTY_POINTS = LOYALTY_POINTS + ? WHERE CUSTOMER_ID = ?
  - checkAndUpgradeTier(int customerId)
    SELECT * FROM MEMBERSHIP_TIERS WHERE MIN_POINTS <= loyaltyPoints ORDER BY MIN_POINTS DESC
  - findById(int customerId) -> Customer

ReportDAO.java
  - getDailyRevenue(Date date) -> Map<String, Double>
    SELECT TYPE, SUM(AMOUNT) FROM INVOICE_LINES
    WHERE TRUNC(CREATED_AT) = TRUNC(?) GROUP BY TYPE
  - getOccupancyRate(Date from, Date to) -> List<OccupancyReport>
  - getTopFnbItems(int limit) -> List<Object[]>
    SELECT ITEM_ID, SUM(QUANTITY) FROM SESSION_ORDER_DETAILS
    GROUP BY ITEM_ID ORDER BY SUM(QUANTITY) DESC

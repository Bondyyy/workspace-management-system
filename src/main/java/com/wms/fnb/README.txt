MODULE    : fnb (Food and Beverage)
MUC DICH  : Quan ly menu, cho mon tai ban, tu dong cap nhat tong tien
MAP ORACLE: MENU_CATEGORIES, MENU_ITEMS, SESSION_ORDERS, SESSION_ORDER_DETAILS

FILES CAN TAO
=============

entity/MenuCategory.java
  - Fields: id, name, display_order, is_active
  - Vi du: Do uong, Banh ngot, An nhe

entity/MenuItem.java
  - Fields: id, category_id, name, price, is_available, image_url, description

entity/SessionOrder.java
  - Fields: id, session_id, total_price, status(OPEN/CLOSED), created_by

entity/SessionOrderDetail.java
  - Fields: id, order_id, item_id, quantity, unit_price, subtotal

controller/MenuController.java
  - GET    /api/menu             : Lay toan bo menu nhom theo category
  - POST   /api/menu             : Them mon moi (chi ADMIN)
  - PUT    /api/menu/{id}        : Sua thong tin mon
  - PATCH  /api/menu/{id}/toggle : Bat/tat mon khoi menu

controller/OrderController.java
  - POST   /api/orders                      : Tao order moi cho session
  - PATCH  /api/orders/{id}/items           : Them/bo/sua so luong mon
  - GET    /api/orders/session/{sessionId}  : Lay tat ca order cua 1 phien

service/OrderService.java

  THUAT TOAN DELTA (quan trong - khong tinh lai tu dau moi lan):
  ==============================================================
  addItem(orderId, itemId, qty)
  - subtotal_moi = qty * unit_price
  - INSERT OrderDetail
  - UPDATE SessionOrder: total_price += subtotal_moi

  updateItem(detailId, newQty)
  - delta = (newQty - oldQty) * unit_price
  - UPDATE OrderDetail.quantity = newQty, subtotal = newQty * unit_price
  - UPDATE SessionOrder: total_price += delta

  removeItem(detailId)
  - lay subtotal_cu cua dong do
  - DELETE OrderDetail
  - UPDATE SessionOrder: total_price -= subtotal_cu

  getOrderTotal(sessionId)
  - SELECT SUM(total_price) FROM session_orders WHERE session_id = ?
  - Dung cho InvoiceService tinh hoa don tong

LUU Y OFFLINE:
  Web POS phai luu tam order chua gui khi mat mang
  OrderController tra 202 Accepted neu request dang xu ly
  FE tu retry khi co mang lai

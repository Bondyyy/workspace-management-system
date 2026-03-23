MODULE    : space
MUC DICH  : Quan ly chi nhanh, ban/phong va so do mat bang
            Broadcast trang thai real-time qua WebSocket (< 2 giay theo SOW)
MAP ORACLE: BRANCHES, SPACES

FILES CAN TAO
=============

entity/Branch.java
  - Fields: id, name, address, phone, status, opening_hours

entity/Space.java
  - Fields: id, branch_id, name, type(SINGLE/GROUP/ROOM), capacity, price_per_hour, status
  - Status flow: AVAILABLE -> BOOKED -> OCCUPIED -> CLEANING -> AVAILABLE

controller/SpaceController.java
  - GET /api/spaces?branchId=&status=  : Lay danh sach ban/phong (co filter)
  - GET /api/spaces/{id}               : Chi tiet 1 khong gian
  - PUT /api/spaces/{id}/status        : Cap nhat trang thai (ADMIN/RECEPTIONIST)
  - GET /api/branches                  : Danh sach chi nhanh

service/SpaceService.java
  - getAvailableSpaces(branchId)
      Tra ve danh sach space co status = AVAILABLE

  - updateStatus(spaceId, newStatus)
      Luu trang thai moi vao DB
      Sau do goi ngay SpaceStatusHandler.broadcastStatusChange(dto)

  - broadcastStatusChange(dto)
      Gui SpaceStatusDto qua WebSocket len topic /topic/spaces/{branchId}
      Tat ca FE dang xem so do se tu cap nhat ma khong can refresh

dto/SpaceStatusDto.java
  - Fields: { spaceId, branchId, newStatus, timestamp }
  - Dung lam payload WebSocket message

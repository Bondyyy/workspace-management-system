MODULE    : common
MUC DICH  : Cac class nen tang dung chung toan bo project

FILES CAN TAO
=============

BaseEntity.java (abstract)
  - @MappedSuperclass
  - @CreatedDate created_at
  - @LastModifiedDate updated_at
  - Can them @EnableJpaAuditing trong WmsApplication.java

ApiResponse.java
  - Wrapper chuan cho moi API response
  - Format: { success: boolean, message: String, data: Object, timestamp: String }
  - Static factory: ApiResponse.ok(data), ApiResponse.error("message")
  - Tat ca Controller phai tra ve kieu nay

GlobalExceptionHandler.java
  - @RestControllerAdvice
  - Bat cac exception va tra ve HTTP status phu hop:
      ResourceNotFoundException     -> 404 Not Found
      BookingConflictException       -> 409 Conflict (double-booking)
      QrAlreadyUsedException         -> 400 Bad Request
      InvalidVoucherException        -> 400 Bad Request
      ValidationException            -> 422 Unprocessable Entity
      SecurityException              -> 403 Forbidden
      RuntimeException (con lai)     -> 500 Internal Server Error
  - Tat ca loi tra ve dung format ApiResponse.error(message)

enums/SpaceStatus.java
  - AVAILABLE, BOOKED, OCCUPIED, CLEANING, MAINTENANCE

enums/BookingStatus.java
  - PENDING, BOOKED, ACTIVE, COMPLETED, CANCELLED

enums/SessionStatus.java
  - ACTIVE, COMPLETED

enums/PaymentMethod.java
  - VNPAY, CASH, TRANSFER

enums/UserRole.java
  - ADMIN, RECEPTIONIST, CLEANER, CUSTOMER

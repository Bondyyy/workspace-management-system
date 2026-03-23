MODULE    : auth
MUC DICH  : Xu ly dang nhap, cap phat va xac thuc JWT token
            Phan quyen RBAC 4 role: ADMIN / RECEPTIONIST / CLEANER / CUSTOMER

FILES CAN TAO
=============

controller/AuthController.java
  - POST /api/auth/login     : Nhan username+password, tra ve JWT token
  - POST /api/auth/register  : Dang ky tai khoan moi (Customer)
  - POST /api/auth/refresh   : Lam moi token khi het han

service/AuthService.java
  - loadUserByUsername()     : Tim user trong DB, tra ve UserDetails cho Spring Security
  - login()                  : Xac thuc mat khau BCrypt, goi JwtUtil de sinh token
  - register()               : Ma hoa mat khau, luu User moi vao DB

security/JwtUtil.java
  - generateToken(user)      : Sinh JWT, nhung role + userId vao claims, TTL 24h
  - validateToken(token)     : Kiem tra chu ky HMAC va thoi han token
  - extractUsername(token)   : Doc username tu payload cua token

security/JwtFilter.java
  - doFilterInternal()       : Chay truoc moi HTTP request
                               Doc header "Authorization: Bearer <token>"
                               Neu hop le -> set SecurityContext de Spring biet ai dang goi

security/SecurityConfig.java
  - filterChain()            : Duong dan public: /api/auth/**
                               ADMIN: tat ca
                               RECEPTIONIST: /api/spaces, /api/bookings, /api/sessions, /api/fnb
                               CUSTOMER: chi xem va dat cho cua ban than

dto/LoginRequest.java        : { username, password }
dto/LoginResponse.java       : { accessToken, refreshToken, expiresIn, role }

# Admin & Identity Service for LMS

This is a comprehensive Admin & Identity Service for the Learning Management System (LMS). It handles user authentication, registration, OTP verification, and admin operations.

## Features

### 1. **Authentication & Authorization**

- JWT-based stateless authentication
- Spring Security with role-based access control
- Support for ROLE_USER, ROLE_ADMIN, and ROLE_SUPER_ADMIN roles
- Protected /admin/\*\* routes (only ADMIN and SUPER_ADMIN can access)

### 2. **User Registration**

- Public registration API with email and phone number
- Password hashing using BCrypt
- Email uniqueness validation
- Phone number uniqueness validation

### 3. **OTP Verification**

- Mock phone OTP service for phone number verification
- 6-digit OTP generation
- 10-minute OTP expiration
- Dual storage (database + Redis for performance)
- OTP verification counter to prevent brute force attacks

### 4. **Admin Capabilities**

- **Course Assignment** (`POST /admin/courses/assign`): Assign courses to users (calls Task Service)
- **Dashboard Stats** (`GET /admin/dashboard/stats`): Get global progress statistics
- **Content Updates** (`PUT /admin/courses/{id}/content`): Update course content (calls Catalog Service)

### 5. **Security**

- JWT token generation and validation
- Refresh token support
- CORS configuration for cross-origin requests
- Input validation using Jakarta validation annotations

## Project Structure

```
adminservice/
├── src/main/java/com/eeki/adminservice/
│   ├── config/                          # Configuration classes
│   │   ├── JwtTokenProvider.java        # JWT token generation/validation
│   │   ├── SecurityConfig.java          # Spring Security configuration
│   │   ├── JwtAuthenticationFilter.java # JWT authentication filter
│   │   ├── JwtAuthenticationEntryPoint.java
│   │   ├── CustomUserDetailsService.java
│   │   ├── RedisConfig.java            # Redis configuration
│   │   └── RestTemplateConfig.java     # RestTemplate for microservice calls
│   ├── controller/                      # REST controllers
│   │   ├── AuthController.java         # Authentication endpoints
│   │   ├── AdminController.java        # Admin endpoints
│   │   └── UserController.java         # User management endpoints
│   ├── service/                         # Business logic
│   │   ├── AuthService.java           # Authentication service
│   │   ├── OtpService.java            # OTP verification service
│   │   ├── AdminService.java          # Admin operations
│   │   └── UserService.java           # User management
│   ├── entity/                          # JPA entities
│   │   ├── User.java                  # User entity
│   │   ├── UserRole.java              # Role enumeration
│   │   ├── OtpVerification.java       # OTP entity
│   │   └── AdminStats.java            # Admin statistics
│   ├── repository/                      # Spring Data JPA repositories
│   │   ├── UserRepository.java
│   │   ├── OtpVerificationRepository.java
│   │   └── AdminStatsRepository.java
│   ├── dto/                             # Data Transfer Objects
│   │   ├── UserDTO.java
│   │   ├── RegisterRequest.java
│   │   ├── LoginRequest.java
│   │   ├── AuthResponse.java
│   │   ├── SendOtpRequest.java
│   │   ├── VerifyOtpRequest.java
│   │   ├── CourseAssignmentRequest.java
│   │   ├── UpdateCourseContentRequest.java
│   │   └── AdminStatsDTO.java
│   ├── exception/                       # Exception handling
│   │   ├── ResourceNotFoundException.java
│   │   ├── UnauthorizedException.java
│   │   ├── ErrorResponse.java
│   │   └── GlobalExceptionHandler.java
│   └── AdminserviceApplication.java    # Main Spring Boot application
└── src/main/resources/
    └── application.properties           # Application configuration
```

## API Endpoints

### Authentication Endpoints (Public)

#### Register User

```
POST /api/v1/auth/register
Content-Type: application/json

{
  "fullName": "John Doe",
  "email": "john@gmail.com",
  "phoneNumber": "9876543210",
  "password": "SecurePassword123"
}

Response: 201 Created
{
  "token": "eyJhbGc...",
  "refreshToken": "eyJhbGc...",
  "user": {
    "id": 1,
    "email": "john@gmail.com",
    "fullName": "John Doe",
    "phoneNumber": "9876543210",
    "role": "ROLE_USER",
    ...
  },
  "message": "User registered successfully"
}
```

#### Login

```
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "john@gmail.com",
  "password": "SecurePassword123"
}

Response: 200 OK
{
  "token": "eyJhbGc...",
  "refreshToken": "eyJhbGc...",
  "user": {...},
  "message": "Login successful"
}
```

#### Send OTP

```
POST /api/v1/auth/send-otp
Content-Type: application/json

{
  "phoneNumber": "9876543210"
}

Response: 200 OK
{
  "message": "OTP sent to 9876543210"
}
```

#### Verify OTP

```
POST /api/v1/auth/verify-otp
Content-Type: application/json

{
  "phoneNumber": "9876543210",
  "otpCode": "123456"
}

Response: 200 OK
{
  "message": "OTP verified successfully"
}
```

### Admin Endpoints (Protected - ADMIN/SUPER_ADMIN only)

#### Assign Course to User

```
POST /api/v1/admin/courses/assign
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

{
  "userId": 1,
  "courseId": 10,
  "notes": "Assign Python course to user"
}

Response: 201 Created
{
  "message": "Course assigned successfully"
}
```

#### Get Dashboard Stats

```
GET /api/v1/admin/dashboard/stats
Authorization: Bearer <JWT_TOKEN>

Response: 200 OK
{
  "totalUsers": 150,
  "activeUsers": 120,
  "totalCoursesAssigned": 500,
  "completedCourses": 250,
  "averageProgress": 75.5,
  "lastUpdated": "2026-03-03T14:30:00"
}
```

#### Update Course Content

```
PUT /api/v1/admin/courses/{id}/content
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

{
  "courseId": 10,
  "title": "Advanced Python",
  "description": "Learn advanced Python concepts",
  "category": "Programming",
  "estimatedHours": 40
}

Response: 200 OK
{
  "message": "Course content updated successfully"
}
```

### User Endpoints

#### Get User by ID

```
GET /api/v1/users/{id}
Authorization: Bearer <JWT_TOKEN>

Response: 200 OK
{
  "id": 1,
  "email": "john@gmail.com",
  "fullName": "John Doe",
  ...
}
```

#### Get All Users (Admin only)

```
GET /api/v1/users
Authorization: Bearer <JWT_TOKEN>

Response: 200 OK
[...]
```

## Configuration

### application.properties

```properties
# Server
spring.application.name=adminservice
server.port=8082

# Database
spring.datasource.url=jdbc:postgresql://...
spring.datasource.username=...
spring.datasource.password=...

# JWT
jwt.secret=MyVerySecureSecretKey...
jwt.expiration=86400000  # 24 hours in ms
jwt.refresh-expiration=604800000  # 7 days

# Redis
spring.redis.host=localhost
spring.redis.port=6379

# Mail (optional)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
```

## Dependencies

- **Spring Boot 3.2.0**: Core framework
- **Spring Data JPA**: Database operations
- **Spring Security**: Authentication and authorization
- **JWT (JJWT 0.11.5)**: Token generation and validation
- **Redis**: OTP caching and performance
- **PostgreSQL**: Database
- **Lombok**: Reduce boilerplate code
- **Validation**: Jakarta validation API

## Running the Service

### Build

```bash
mvn clean install
```

### Run

```bash
mvn spring-boot:run
```

Or:

```bash
java -jar target/adminservice-0.0.1-SNAPSHOT.jar
```

### Default Port

The service runs on **port 8082** by default.

## Integration with Other Services

### Task Service (Port 8080)

- Called when assigning courses to users
- Endpoint: `POST http://localhost:8080/api/v1/tasks/create`

### Catalog Service (Port 8081)

- Called when updating course content
- Endpoint: `PUT http://localhost:8081/api/v1/courses/{id}`

## Security Notes

1. **JWT Secret**: Change the default secret in production
2. **CORS**: Configured to allow all origins (update for production)
3. **Password**: Minimum 8 characters, hashed with BCrypt
4. **OTP**: 6-digit code, valid for 10 minutes
5. **Rate Limiting**: Implement in production

## TODO/Future Enhancements

- [ ] Implement email verification
- [ ] Add rate limiting for OTP requests
- [ ] Integrate with actual SMS provider (Twilio, AWS SNS)
- [ ] Add audit logging
- [ ] Implement OAuth2 with Google/GitHub
- [ ] Add user profile management
- [ ] Implement password reset functionality
- [ ] Add two-factor authentication (2FA)
- [ ] Database migration scripts (Flyway/Liquibase)

## Testing

Run tests with:

```bash
mvn test
```

## License

This project is part of the LMS system.

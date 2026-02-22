# Healthcare Clinic System

Spring Boot REST API for managing patients, doctors, appointments, and authentication using JWT.

## Tech Stack

- Java 21, Spring Boot 3.4, Maven
- Spring Data JPA / Hibernate
- Spring Security + JWT (jjwt)
- H2 (dev/test) / MySQL (prod)
- Caffeine Cache
- MapStruct, Lombok
- Swagger / OpenAPI (springdoc)
- JUnit 5 + Mockito

## Prerequisites

- Java 21+
- Maven 3.8+

## Run the Application

```bash
cd healthcare-clinic
mvn spring-boot:run
```

The app starts on **http://localhost:8080** with the `dev` profile (H2 in-memory database).

## Useful Links

| Link | URL |
|------|-----|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |
| H2 Console | http://localhost:8080/h2-console |

### H2 Console Settings

- JDBC URL: `jdbc:h2:mem:clinicdb`
- Username: `sa`
- Password: *(empty)*

## Default Users (dev profile)

| Username | Password | Role |
|----------|----------|------|
| admin | admin123 | ADMIN |
| receptionist | recep123 | RECEPTIONIST |

## Seeded Doctors (dev profile)

| Name | Specialty | Duration |
|------|-----------|----------|
| Dr. Ahmed Hassan | Cardiology | 30 min |
| Dr. Fatima Al-Rashid | Dermatology | 20 min |
| Dr. Omar Khalil | Pediatrics | 25 min |
| Dr. Sara Nouri | General Practice | 15 min |
| Dr. Khalid Al-Mansour | Orthopedics | 30 min |

## API Endpoints

### Authentication (no auth required)

```
POST /api/v1/auth/login       - Login, get JWT token
POST /api/v1/auth/logout      - Logout, invalidate token (requires token)
```

### Doctors (no auth required)

```
GET  /api/v1/doctors                      - Get all doctors
GET  /api/v1/doctors/{id}                 - Get doctor by ID
GET  /api/v1/doctors/specialty/{specialty} - Filter by specialty
GET  /api/v1/doctors/search?name=         - Search by name
```

### Patients (auth required)

```
POST   /api/v1/patients                   - Register new patient
GET    /api/v1/patients/{id}              - Get patient by ID
GET    /api/v1/patients?page=0&size=10    - Get all patients with appointments (paginated)
DELETE /api/v1/patients/{id}              - Soft delete patient
```

### Appointments (auth required)

```
POST /api/v1/appointments                 - Schedule appointment
GET  /api/v1/appointments/{id}            - Get appointment by ID
PUT  /api/v1/appointments/{id}            - Update appointment
GET  /api/v1/appointments/patient/{id}    - Get appointments by patient
```

## Testing the APIs

### 1. Login

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

Copy the `accessToken` from the response. Use it in the `Authorization` header for all protected endpoints.

### 2. Register a Patient

```bash
curl -X POST http://localhost:8080/api/v1/patients \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <YOUR_TOKEN>" \
  -d '{
    "fullNameEn": "Ahmed Ali",
    "fullNameAr": "أحمد علي",
    "email": "ahmed@example.com",
    "mobileNumber": "+96512345678",
    "dateOfBirth": "1990-05-15",
    "nationalId": "12345678",
    "street": "Main St",
    "city": "Kuwait City",
    "region": "Capital"
  }'
```

### 3. Get Doctors

```bash
curl http://localhost:8080/api/v1/doctors
```

### 4. Schedule Appointment

```bash
curl -X POST http://localhost:8080/api/v1/appointments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <YOUR_TOKEN>" \
  -d '{
    "patientId": 1,
    "doctorId": 1,
    "appointmentDateTime": "2026-03-15T10:00:00",
    "reason": "General checkup"
  }'
```

### 5. Get All Patients with Appointments

```bash
curl http://localhost:8080/api/v1/patients?page=0&size=10 \
  -H "Authorization: Bearer <YOUR_TOKEN>"
```

### 6. Update Appointment

```bash
curl -X PUT http://localhost:8080/api/v1/appointments/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <YOUR_TOKEN>" \
  -d '{
    "status": "CONFIRMED",
    "reason": "Updated reason"
  }'
```

### 7. Soft Delete Patient

```bash
curl -X DELETE http://localhost:8080/api/v1/patients/1 \
  -H "Authorization: Bearer <YOUR_TOKEN>"
```

### 8. Logout

```bash
curl -X POST http://localhost:8080/api/v1/auth/logout \
  -H "Authorization: Bearer <YOUR_TOKEN>"
```

## Postman Collection

A complete Postman collection is included: `Healthcare-Clinic.postman_collection.json`

**20 requests** organized in 4 folders:

| Folder | Requests | Tests Included |
|--------|----------|----------------|
| Authentication | Login, Login (invalid), Logout | 3 |
| Patients | Register, Validation Error, Duplicate, Get by ID, Get All (paginated), Soft Delete, Verify Delete | 7 |
| Doctors | Get All, Get by ID, By Specialty, Search, Not Found | 5 |
| Appointments | Schedule, Time Conflict, Get by ID, Update, By Patient | 5 |

**How to use:**
1. Import `Healthcare-Clinic.postman_collection.json` into Postman
2. Run **"Login"** first — the token is saved automatically to a collection variable
3. All other requests use the saved token
4. Run requests in order for the best flow

## Run Unit Tests

```bash
mvn test
```

**50 tests** covering:

| Layer | Tests |
|-------|-------|
| Controllers | AuthController, PatientController, DoctorController, AppointmentController |
| Services | AuthService, PatientService, DoctorService, AppointmentService |
| Repositories | PatientRepository, AppointmentRepository |
| Security | JwtService |
| Application | Context load |

## Spring Profiles

| Profile | Database | Logs | DevTools |
|---------|----------|------|----------|
| `dev` (default) | H2 in-memory | DEBUG | Enabled |
| `test` | H2 in-memory | WARN | Disabled |
| `prod` | MySQL | INFO | Disabled |

### Run with a specific profile

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

For `prod`, set these environment variables:

```
DB_HOST, DB_PORT, DB_NAME, DB_USERNAME, DB_PASSWORD, JWT_SECRET
```

## Project Structure

```
src/main/java/com/clinic/
├── config/          Security, Cache, Async, OpenAPI, CORS
├── controller/      REST controllers
├── dto/
│   ├── request/     Input DTOs with validation
│   └── response/    Output DTOs
├── entity/          JPA entities + enums
├── exception/       Custom exceptions + GlobalExceptionHandler
├── mapper/          MapStruct mappers
├── repository/      Spring Data JPA repositories
├── security/        JWT filter, service, token blacklist
├── service/         Business logic
└── init/            Data seeder (dev/test only)
```

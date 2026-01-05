
# 🗓️ Life Manager - Backend

> 일정과 가계부를 한 번에 관리하는 통합 라이프 매니저 백엔드 API

[![Java](https://img.shields.io/badge/Java-17-007396?style=flat-square&logo=java)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-6DB33F?style=flat-square&logo=spring)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-336791?style=flat-square&logo=postgresql)](https://www.postgresql.org/)

## 📌 프로젝트 소개

Life Manager는 개인의 일정과 금전 관리를 하나의 플랫폼에서 통합 관리할 수 있는 웹 애플리케이션입니다.
이 저장소는 Spring Boot 기반의 RESTful API 서버를 포함하고 있습니다.
[//]: # (**🌐 배포 URL**: https://life-manager.duckdns.org)
---

## ✨ 주요 기능

### 👤 사용자 관리
- 회원가입 / 로그인 (JWT 기반 인증)
- 프로필 관리 (이름 변경)
- 비밀번호 변경

### 📅 일정 관리
- 일정 CRUD (생성, 조회, 수정, 삭제)
- 페이지네이션 & 정렬
- 색상 태그 & 카테고리
- 종일 일정 지원

### 💰 가계부 관리
- 수입/지출 기록 관리
- 기간별 조회 & 요약
- 페이지네이션 & 정렬
- 카테고리별 분류

---

## 🛠️ 기술 스택

### Core
- **Java 17** - 프로그래밍 언어
- **Spring Boot 3.5** - 백엔드 프레임워크
- **Spring Security** - 인증 & 보안
- **Spring Data JPA** - ORM

### Database
- **PostgreSQL 15** - 메인 데이터베이스
- **AWS RDS** - 클라우드 데이터베이스

### Security
- **JWT (JSON Web Token)** - 토큰 기반 인증
- **BCrypt** - 비밀번호 암호화

### Deployment
- **AWS EC2** - 서버 호스팅
- **Nginx** - 리버스 프록시
- **Let's Encrypt** - SSL/TLS 인증서
- **DuckDNS** - 동적 DNS
- **GitHub Actions** - CI/CD

---

## 📁 프로젝트 구조
```
src/main/java/com/lifemanager/life_manager/
├── config/              # 설정 파일
│   ├── CurrentUserId.java
│   ├── JwtAuthenticationFilter.java
│   ├── SecurityConfig.java
│   └── UserIdArgumentResolver.java
├── controller/          # REST API 컨트롤러
│   ├── AuthController.java
│   ├── ScheduleController.java
│   ├── TransactionController.java
│   └── UserController.java
├── domain/             # 엔티티
│   ├── User.java
│   ├── Schedule.java
│   └── Transaction.java
├── dto/                # 데이터 전송 객체
│   ├── auth/
│   ├── schedule/
│   ├── transaction/
│   └── user/
├── repository/         # JPA 레포지토리
│   ├── UserRepository.java
│   ├── ScheduleRepository.java
│   └── TransactionRepository.java
├── service/            # 비즈니스 로직
│   ├── AuthService.java
│   ├── ScheduleService.java
│   ├── TransactionService.java
│   └── UserService.java
└── util/              # 유틸리티
    └── JwtUtil.java
```

---

## 🚀 시작하기

### 필수 요구사항
- Java 17 이상
- PostgreSQL 15 이상
- Gradle 8.0 이상

### 환경 변수 설정

`application.properties` 또는 환경 변수에 다음을 설정:
```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/life_manager
spring.datasource.username=your_username
spring.datasource.password=your_password

# JWT
jwt.secret=your-secret-key-here
jwt.expiration=86400000
```

### 로컬 실행
```bash
# 1. 저장소 클론
git clone https://github.com/panda981018/life-manager.git
cd life-manager

# 2. 빌드
./gradlew clean build

# 3. 실행
./gradlew bootRun
```

서버는 `http://localhost:9000`에서 실행됩니다.

---

## 📡 API 엔드포인트

### 🔐 인증
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/auth/signup` | 회원가입 |
| POST | `/api/auth/login` | 로그인 |

### 👤 사용자
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/users/me` | 내 정보 조회 |
| PUT | `/api/users/me` | 이름 변경 |
| PUT | `/api/users/me/password` | 비밀번호 변경 |

### 📅 일정
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/schedules` | 일정 목록 조회 (페이지네이션) |
| POST | `/api/schedules` | 일정 생성 |
| PUT | `/api/schedules/{id}` | 일정 수정 |
| DELETE | `/api/schedules/{id}` | 일정 삭제 |

### 💰 거래
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/transactions` | 거래 목록 조회 (페이지네이션) |
| GET | `/api/transactions/summary` | 기간별 수입/지출 요약 |
| POST | `/api/transactions` | 거래 생성 |
| DELETE | `/api/transactions/{id}` | 거래 삭제 |

---

## 🔒 보안

- **JWT 인증**: 모든 API는 JWT 토큰 기반 인증 사용
- **비밀번호 암호화**: BCrypt 알고리즘 사용
- **CORS 설정**: 허용된 오리진만 접근 가능
- **@CurrentUserId**: 커스텀 어노테이션으로 안전한 사용자 식별

---

## 📊 데이터베이스 스키마

### Users (사용자)
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL
);
```

### Schedules (일정)
```sql
CREATE TABLE schedules (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    start_datetime TIMESTAMP NOT NULL,
    end_datetime TIMESTAMP NOT NULL,
    is_all_day BOOLEAN DEFAULT FALSE,
    category VARCHAR(100),
    color VARCHAR(7),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### Transactions (거래)
```sql
CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    category VARCHAR(100),
    description TEXT,
    transaction_date DATE NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

---

## 🚢 배포

### AWS EC2 배포
```bash
# 1. JAR 파일 빌드
./gradlew clean build -x test

# 2. EC2에 업로드
scp build/libs/*.jar ec2-user@your-server:/home/ec2-user/

# 3. EC2에서 실행
java -jar life-manager-0.0.1-SNAPSHOT.jar
```

### GitHub Actions CI/CD
`.github/workflows/deploy.yml` 참고

---

## 🤝 기여

이 프로젝트는 개인 포트폴리오 프로젝트입니다.

---

## 📝 라이선스

이 프로젝트는 개인 학습 목적으로 만들어졌습니다.

---

## 👨‍💻 개발자

**panda981018** - [GitHub](https://github.com/panda981018)

---

## 🔗 관련 링크

- [프론트엔드 저장소](https://github.com/panda981018/life-manager-frontend)
- [라이브 데모](https://life-manager-frontend-ruddy.vercel.app)
- [API 문서](https://life-manager.duckdns.org/swagger-ui.html) (준비 중)
# Life Manager 🗓️💰

스마트한 일상 관리를 위한 통합 스케줄링 및 지출 추적 애플리케이션

## 📋 프로젝트 소개

Life Manager는 개인의 일정과 재정을 한 곳에서 효율적으로 관리할 수 있는 풀스택 웹 애플리케이션입니다.
직관적인 UI와 강력한 기능으로 일상의 생산성을 높이고, 재정 관리를 쉽게 할 수 있도록 도와줍니다.

### 🌟 주요 기능

#### 📅 일정 관리
- 일정 생성, 조회, 수정, 삭제 (CRUD)
- 시작/종료 시간 기반 일정 관리
- 페이지네이션 및 정렬 지원
- 대시보드에서 다가오는 일정 요약 확인

#### 💳 지출 관리
- 수입/지출 거래 기록 및 추적
- 날짜 범위별 거래 조회
- 수입/지출 요약 통계
- 카테고리별 거래 분류

#### 🔐 인증 및 보안
- JWT 기반 토큰 인증
- OAuth2 소셜 로그인 지원
    - Google 로그인
    - Kakao 로그인
    - Naver 로그인
- 비밀번호 암호화 (BCrypt)
- CORS 설정으로 안전한 API 통신

#### 📊 대시보드
- 실시간 요약 정보
    - 이번 달 수입/지출 통계
    - 다가오는 일정 미리보기
    - 최근 거래 내역
- 직관적인 데이터 시각화

## 🛠 기술 스택

### Backend
- **Framework**: Spring Boot 3.3.5
- **Language**: Java 17
- **Database**: PostgreSQL
- **ORM**: Spring Data JPA (Hibernate)
- **Security**: Spring Security + JWT
- **OAuth2**: Spring Security OAuth2 Client
- **Build Tool**: Gradle
- **Deployment**: AWS EC2 (Ubuntu)

### Frontend
- **Framework**: React 18
- **Styling**: Tailwind CSS
- **Routing**: React Router DOM
- **HTTP Client**: Axios
- **Deployment**: Vercel

### Infrastructure
- **Database**: AWS RDS (PostgreSQL)
- **Backend Server**: AWS EC2
- **Frontend Hosting**: Vercel
- **Domain**: DuckDNS
- **Reverse Proxy**: Nginx
- **SSL**: Let's Encrypt
- **CI/CD**: GitHub Actions

## 📐 시스템 아키텍처
```
┌─────────────┐         ┌──────────────┐         ┌─────────────┐
│   Vercel    │         │   AWS EC2    │         │   AWS RDS   │
│  (Frontend) │◄───────►│  (Backend)   │◄───────►│ (PostgreSQL)│
│   React     │  HTTPS  │ Spring Boot  │  JDBC   │             │
└─────────────┘         └──────────────┘         └─────────────┘
                               │
                               │ OAuth2
                               ▼
                    ┌──────────────────────┐
                    │ OAuth2 Providers     │
                    │ - Google             │
                    │ - Kakao              │
                    │ - Naver              │
                    └──────────────────────┘
```

## 🚀 시작하기

### 필수 요구사항

- Java 17 이상
- PostgreSQL 12 이상
- Gradle 7.0 이상
- Node.js 16 이상 (프론트엔드)

### 환경 변수 설정

`application-prod.properties` 또는 시스템 환경 변수에 다음 설정 필요:
```properties
# Database
DB_URL=jdbc:postgresql://localhost:5432/life_manager
DB_USERNAME=your_username
DB_PASSWORD=your_password

# JWT
JWT_SECRET=your-secret-key-min-256-bits

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:3000

# OAuth2 - Google
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret

# OAuth2 - Kakao
KAKAO_CLIENT_ID=your-kakao-rest-api-key
KAKAO_CLIENT_SECRET=your-kakao-client-secret

# OAuth2 - Naver
NAVER_CLIENT_ID=your-naver-client-id
NAVER_CLIENT_SECRET=your-naver-client-secret

# OAuth2 Redirect
OAUTH2_REDIRECT_URI=http://localhost:3000/api/oauth2/redirect
```

### 데이터베이스 설정
```sql
-- PostgreSQL 데이터베이스 생성
CREATE DATABASE life_manager;

-- 테이블은 애플리케이션 실행 시 자동 생성됨 (JPA)
```

### 로컬 실행
```bash
# 1. 저장소 클론
git clone https://github.com/your-username/life-manager.git
cd life-manager

# 2. 환경 변수 설정 (.env 또는 시스템 환경 변수)

# 3. 빌드
./gradlew clean build

# 4. 실행
./gradlew bootRun

# 애플리케이션이 http://localhost:9000 에서 실행됩니다
```

### 프로덕션 빌드
```bash
# JAR 파일 생성
./gradlew clean build -x test

# 생성된 파일: build/libs/life-manager-*.jar
```

## 🔑 OAuth2 소셜 로그인 설정

### Google Cloud Console

1. [Google Cloud Console](https://console.cloud.google.com/) 접속
2. 프로젝트 생성 → API 및 서비스 → 사용자 인증 정보
3. OAuth 2.0 클라이언트 ID 생성
4. **승인된 리디렉션 URI** 추가:
    - `http://localhost:9000/api/login/oauth2/code/google` (로컬)
    - `https://your-domain.com/api/login/oauth2/code/google` (운영)
5. Client ID와 Client Secret 저장

### Kakao Developers

1. [Kakao Developers](https://developers.kakao.com/) 접속
2. 내 애플리케이션 → 앱 생성
3. 플랫폼 → Web → Redirect URI 설정:
    - `http://localhost:9000/api/login/oauth2/code/kakao` (로컬)
    - `https://your-domain.com/api/login/oauth2/code/kakao` (운영)
4. 카카오 로그인 → 활성화 설정
5. 동의 항목 → 닉네임, 이메일 필수 동의 설정
6. REST API 키와 Client Secret 저장

### Naver Developers

1. [Naver Developers](https://developers.naver.com/) 접속
2. 애플리케이션 등록
3. API 설정 → Callback URL:
    - `http://localhost:9000/api/login/oauth2/code/naver` (로컬)
    - `https://your-domain.com/api/login/oauth2/code/naver` (운영)
4. 사용 API → 회원 이름, 이메일 체크
5. Client ID와 Client Secret 저장

## 📚 API 문서

### 인증 API

| Method | Endpoint                               | Description |
|--------|----------------------------------------|-------------|
| POST | `/api/auth/signup`                     | 회원가입 |
| POST | `/api/auth/login`                      | 로그인 |
| GET | `/api/oauth2/authorization/{provider}` | 소셜 로그인 시작 |

### 일정 API

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/schedules` | 일정 목록 조회 | Required |
| POST | `/api/schedules` | 일정 생성 | Required |
| PUT | `/api/schedules/{id}` | 일정 수정 | Required |
| DELETE | `/api/schedules/{id}` | 일정 삭제 | Required |

### 거래 API

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/transactions` | 거래 목록 조회 | Required |
| GET | `/api/transactions/summary` | 거래 요약 통계 | Required |
| POST | `/api/transactions` | 거래 생성 | Required |
| DELETE | `/api/transactions/{id}` | 거래 삭제 | Required |

### 사용자 API

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/users/me` | 내 정보 조회 | Required |
| PUT | `/api/users/me` | 내 정보 수정 | Required |
| PUT | `/api/users/me/password` | 비밀번호 변경 | Required |

## 🔐 보안

- **JWT 토큰**: 모든 API 요청에 `Authorization: Bearer {token}` 헤더 필요
- **비밀번호 암호화**: BCrypt 알고리즘 사용
- **CORS 설정**: 허용된 오리진만 접근 가능
- **환경 변수**: 민감한 정보는 환경 변수로 관리
- **HTTPS**: 프로덕션 환경에서 SSL 인증서 적용

## 🌐 배포

### 백엔드 (AWS EC2)
```bash
# systemd 서비스 설정
sudo vi /etc/systemd/system/life-manager.service

# 서비스 시작
sudo systemctl start life-manager
sudo systemctl enable life-manager

# Nginx 리버스 프록시 설정
sudo vi /etc/nginx/sites-available/life-manager
```

### 프론트엔드 (Vercel)

- GitHub 저장소와 연동
- 환경 변수 설정
- 자동 배포 활성화

## 📝 프로젝트 구조
```
src/
├── main/
│   ├── java/com/lifemanager/life_manager/
│   │   ├── config/          # 설정 (Security, CORS, JWT, OAuth2)
│   │   ├── controller/      # REST API 컨트롤러
│   │   ├── domain/          # JPA 엔티티
│   │   ├── dto/             # 데이터 전송 객체
│   │   ├── repository/      # JPA 레포지토리
│   │   ├── security/        # JWT, OAuth2 관련
│   │   ├── service/         # 비즈니스 로직
│   │   └── resolver/        # 커스텀 어노테이션 리졸버
│   └── resources/
│       └── application.properties  # 설정 파일
└── test/                    # 테스트 코드
```

## 🤝 기여

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 라이선스

이 프로젝트는 개인 학습 목적으로 만들어졌습니다.

## 👨‍💻 개발자

**Jiwon**
- GitHub: [@panda981018](https://github.com/your-username)

## 📞 문의

프로젝트에 대한 질문이나 제안사항이 있으시면 이슈를 등록해주세요.

---

⭐ 이 프로젝트가 도움이 되셨다면 Star를 눌러주세요!

# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 프로젝트 개요

**여수 가로등 배너 예약 시스템** — 여수시의 가로등 배너(가로기) 게첨 구간을 온라인으로 예약·관리하는 Spring Boot 웹 애플리케이션.

- 일반 사용자: 구간 조회, 예약 신청, 내 신청 현황 확인
- 관리자(`admin` 계정): 구간 설정, 전체 예약 내역 관리·삭제, 보고서 출력

## 빌드 및 실행

```bash
# 빌드 (테스트 제외)
./gradlew build -x test

# 테스트 실행
./gradlew test

# 단일 테스트 클래스 실행
./gradlew test --tests "jpabook.jpashop.Service.OrderServiceTest"

# 로컬 실행
./gradlew bootRun
```

테스트는 `src/test/resources/application.yml`에 정의된 H2 인메모리 DB(MySQL 호환 모드)를 사용한다. 운영은 AWS RDS MySQL을 사용한다(`src/main/resources/application.yml`).

## 기술 스택

- Java 11 / Spring Boot 2.7.7
- Spring Data JPA + Hibernate (ddl-auto: none, 스키마는 DB에서 직접 관리)
- Spring Security 5.6 (폼 로그인, BCrypt 암호화)
- Thymeleaf + thymeleaf-layout-dialect (서버사이드 렌더링)
- MySQL(운영) / H2(테스트)
- p6spy로 SQL 파라미터 로깅
- GitHub Actions → AWS EC2 자동 배포 (master push 시 트리거)

## 아키텍처

### 레이어 구조

```
Controller → Service → Repository → DB
              ↑
           Domain (Entity)
```

- **Controller** (`controller/`, `report/controller/`): `@Controller` + Thymeleaf 뷰 반환. 로그인 체크는 `UserSecurityService.LoginUserCheck()`를 직접 호출한다.
- **Service** (`Service/`): `@Transactional`. 비즈니스 로직과 도메인 조작 담당.
- **Repository** (`repository/`): EntityManager 직접 사용. Spring Data JPA `JpaRepository`와 커스텀 JPQL 쿼리 혼용.
- **Domain** (`domain/`, `domain/item/`): JPA 엔티티. 비즈니스 로직을 엔티티 안에 배치하는 도메인 모델 패턴 사용.

### 핵심 도메인 관계

```
Member ─── Order ─── OrderItem ─── Item(FlagSection)
```

- `Item`은 `SINGLE_TABLE` 상속 전략. 현재 실제 사용되는 구체 타입은 `FlagSection`(구간) 뿐이다. `Book`, `Album`, `Movie`는 초기 튜토리얼 잔재로 미사용.
- `Order`는 `orderStartDate`, `orderEndDate`(문자열 `yyyy-MM-dd`)로 게첨 기간을 표현한다.
- `OrderStatus`: `ORDER`(신청), `CANCEL`(취소), `PAYED`(결제완료). `PAYED` 상태면 일반 사용자는 취소 불가; `admin`은 항상 삭제 가능.

### 주문 흐름 (핵심 비즈니스)

1. 사용자가 날짜 선택 → `OrderService.findItemsOfPossible()` 호출로 구간별 잔여 수량 계산
2. 주문 제출 시 `OrderService.order()`는 `synchronized` + DB 비관적 락으로 동시 중복 예약 방지
3. 예약 기간이 15일이면 정가의 절반, 30일이면 정가 적용

### 인증 / 권한

- `UserSecurityService`가 `UserDetailsService` 구현. `loginId == "admin"`이면 `ROLE_ADMIN`, 나머지는 `ROLE_USER`.
- 비밀번호는 DB에 평문으로 저장되어 있으나 Spring Security에 넘길 때 BCrypt로 인코딩하는 방식 사용 (로그인 시 매번 인코딩하여 `UserDetails` 생성).
- 컨트롤러에서 로그인 체크 시 `== "anonymousUser"` 비교를 사용하는 코드가 있으나, `equals()`로 교체해야 하는 알려진 버그이다.

### 보고서 기능

`report/` 패키지는 별도 레이어로 분리: `ReportController` → `ReportService` → `ReportRepository`.

## DB 설정 전환

- **운영(RDS MySQL)**: `src/main/resources/application.yml` 상단 설정이 활성화 상태
- **로컬 MySQL**: 같은 파일에서 주석 처리된 localhost 설정으로 전환
- **테스트(H2)**: `src/test/resources/application.yml` — `MODE=MYSQL` 사용, `ddl-auto: create-drop`

## 배포

**인프라**: NHN Cloud VM + Docker Compose

```
master push → GitHub Actions (JAR 빌드)
            → SCP: jpashop-app.jar + Dockerfile + docker-compose.yml → Oracle VM
            → SSH: docker compose up --build -d
```

**컨테이너 구성** (`docker-compose.yml`)
- `app`: Spring Boot (포트 8080)
- `db`: MySQL 8.0 (볼륨 `mysql_data`로 데이터 영속화)

**GitHub Secrets 목록**
| Secret | 설명 |
|---|---|
| `SERVER_HOST` | VM 공인 IP (Floating IP) |
| `SERVER_USER` | VM 접속 사용자 (`ubuntu`) |
| `SERVER_SSH_KEY` | VM SSH 개인키 |
| `MYSQL_ROOT_PASSWORD` | MySQL root 비밀번호 |

**로그 확인** (VM SSH 접속 후)
```bash
docker compose logs app -f     # 앱 로그
docker compose logs db  -f     # DB 로그
docker compose ps              # 컨테이너 상태
```

**Oracle Cloud VM 사전 준비** (최초 1회)
```bash
# Ubuntu VM에 Docker 설치
sudo apt update && sudo apt install -y docker.io docker-compose-plugin
sudo usermod -aG docker $USER   # 재로그인 필요
```
Oracle Cloud 보안 규칙에서 포트 8080 (TCP) 인바운드 허용 필요.

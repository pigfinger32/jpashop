# Plan.md — 업무 지시서

이 파일은 Claude Code에게 작업을 지시하기 위한 파일입니다.
작업 지시를 여기에 작성하고 Claude Code에게 전달하세요.

> Claude Code는 작업 시작 전 반드시 CLAUDE.md와 Plan.md를 먼저 읽을 것.

---

## 작업 지시 방법

1. 아래 "현재 작업" 섹션에 지시 내용을 작성한다.
2. Claude Code에게 "Plan.md 보고 작업해줘" 라고 요청한다.
3. 작업 완료 후 "완료된 작업" 섹션으로 이동시킨다.

---

## 현재 작업

### [TASK-001] Docker + Oracle Cloud 배포 환경 구축

**설계 결론 (확정)**

Oracle Cloud Free Tier ARM VM에 Docker Compose로 Spring Boot + MySQL을 올린다.

```
GitHub (master push)
       ↓ GitHub Actions (JAR 빌드)
       ↓ SSH로 Oracle Cloud ARM VM에 JAR 전송
Oracle Cloud ARM VM
       ↓ docker compose up --build
   ┌──────────────────────────────┐
   │  app  (Spring Boot, :8080)   │
   │  db   (MySQL 8.0, :3306)     │
   └──────────────────────────────┘
```

**작업 목록**

- [x] `Dockerfile` 작성 (eclipse-temurin:11-jre, ARM 호환)
- [x] `docker-compose.yml` 작성 (app + mysql, healthcheck, .env로 비밀번호 주입)
- [x] `application.yml` 수정 (환경변수 주입 방식으로 변경, 로컬 fallback 포함)
- [x] `.github/workflows/deploy.yml` 수정 (Oracle Cloud + Docker Compose 방식)
- [x] CLAUDE.md 배포 섹션 업데이트

**남은 작업 (사용자가 직접 해야 하는 수동 작업)**
- [ ] Oracle Cloud 계정 생성 및 ARM VM 인스턴스 생성
- [ ] VM에 Docker 설치
- [ ] Oracle Cloud 보안 규칙에서 포트 8080 인바운드 허용
- [ ] GitHub Secrets 등록: `ORACLE_HOST`, `ORACLE_USER`, `ORACLE_SSH_KEY`, `MYSQL_ROOT_PASSWORD`

**제약사항**
- Oracle ARM VM이므로 Docker 이미지는 `linux/arm64` 또는 멀티플랫폼 지원 이미지 사용
- MySQL 데이터는 Docker volume으로 영속화 필수
- DB 비밀번호 등 민감정보는 GitHub Secrets → 환경변수로 주입

---

## 완료된 작업

<!-- 완료된 작업은 여기로 이동 -->

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

<!-- 여기에 작업 지시를 작성하세요 -->

---

## 완료된 작업

### [TASK-001] Docker + NHN Cloud 배포 환경 구축 ✅ (2026-05-07)

**인프라**
- NHN Cloud VM: `m2.c2m4` (2코어 4GB), Ubuntu 22.04
- 공인 IP: `180.210.82.98`
- Docker Compose: Spring Boot(8080) + MySQL 8.0

**배포 흐름**
```
master push → GitHub Actions (JAR 빌드)
            → SCP: jar + Dockerfile + docker-compose.yml → VM
            → SSH: docker compose up --build -d
```

**GitHub Secrets**
- `SERVER_HOST` / `SERVER_USER` / `SERVER_SSH_KEY` / `MYSQL_ROOT_PASSWORD`

**초기 데이터**
- admin 계정 직접 INSERT 필요 (DB가 비어있으므로)
```sql
INSERT INTO Member (loginId, pw, name, company, phone, bizRegiNo)
VALUES ('admin', 'admin1234', '관리자', '관리자', '010-0000-0000', '000-00-00000');
```

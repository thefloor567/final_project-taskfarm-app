# 🌱 taskfarm — App

> 할일을 완료하면 AI가 경험치를 책정하고, 그 경험치로 농장을 키우는 **게이미피케이션 투두앱**
> EKS 위에 GitOps 방식으로 배포

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green)
![EKS](https://img.shields.io/badge/AWS-EKS%201.35-FF9900)

<!-- 배포 후 추가 -->
<!-- **[Site](배포 후 ALB 주소)** -->
<!-- **[ArgoCD](배포 후)** / **[Grafana](배포 후)** (비밀번호는 문의) -->

---

## 📌 서비스 소개

taskfarm은 **할일 관리 + 농장 키우기**를 결합한 게이미피케이션 투두앱입니다.

1. 할일을 완료한다
2. 추천 버튼을 누르면 **Gemini AI가 그 할일의 경험치를 책정**한다 (유저가 원할 때만, 온디맨드)
3. 쌓인 경험치로 **농장을 운영**(작물 심기·수확 등)한다

> 단순 할일 체크를 넘어, 완료의 보상을 농장 성장으로 시각화해 꾸준함을 유도합니다.

---

## 🛠 기술 스택

| 영역 | 기술 |
|------|------|
| Backend | Spring Boot, JPA, Spring Security (JWT) |
| Frontend | HTML/CSS/JS (경량 연동) |
| DB | MySQL 8.4 (RDS, Multi-AZ) |
| Cache | Redis (ElastiCache) — Gemini 응답 캐싱 |
| AI | Google Gemini API (경험치 책정) |
| Secret | AWS Secrets Manager + ESO |
| Container | Docker, Amazon ECR |
| Orchestration | Amazon EKS 1.35 |
| IaC | Terraform |
| CI | GitHub Actions (OIDC) |
| CD | ArgoCD (GitOps) |
| Monitoring | kube-prometheus-stack, Grafana |

---

## 🗂 레포 구조 (3-repo)

GitOps 정석에 따라 **CI는 app, CD/매니페스트는 config, 인프라는 infra** 로 분리합니다.

| 레포 | 책임 | 주요 내용 |
|------|------|-----------|
| **team4-taskfarm-app** (현재) | 애플리케이션 + CI | Spring Boot 소스, Dockerfile, GitHub Actions (OIDC → ECR) |
| **team4-taskfarm-config** | 배포 (CD) | k8s 매니페스트(kustomize), ArgoCD Application, ExternalSecret |
| **team4-taskfarm-infra** | 인프라 | Terraform (modules + envs/dev·prod) |

> 💡 ArgoCD가 config 레포를 단일 진실 공급원(SSoT)으로 추적합니다.

---

## 🌿 브랜치 전략

> **환경은 브랜치가 아닙니다.** main 단일 + feature 로만 운영하고,
> 환경(dev/prod) 구분은 config·infra 레포의 **디렉터리/오버레이**로 합니다. (develop 미사용)

| 브랜치 | 설명 |
|--------|------|
| `main` | 통합·배포 브랜치 (PR로만 merge) |
| `feature/*` | 기능 개발 |
| `fix/*` | 버그 수정 |
| `chore/*` | 설정/빌드 변경 |
| `ci/*` | CI/CD 변경 |

### 네이밍 형식: `타입/이름_날짜/기능설명`
```bash
feature/NOEUL_20260604/todo-crud
feature/NOEUL_20260604/gemini-exp-api
fix/NOEUL_20260604/jwt-expire-error
ci/NOEUL_20260604/oidc-ecr-push
```

---

## 📝 커밋 컨벤션

형식: `<타입>: <한 줄 요약>`

| 타입 | 설명 |
|------|------|
| `feat` | 새 기능 |
| `fix` | 버그 수정 |
| `refactor` | 리팩토링 |
| `chore` | 빌드/설정 |
| `ci` | CI/CD |
| `docs` | 문서 |

### ✅ 좋은 예
```bash
feat: 할일 완료 시 경험치 트리거 구현
fix: Gemini 응답 캐싱 키 충돌 수정
ci: GitHub Actions OIDC role-to-assume 적용
```
### ❌ 나쁜 예
```bash
git commit -m "수정"
git commit -m "작업중"
```

---

## 🔀 PR 규칙

1. `feature/*` → `main` 방향으로만 PR
2. PR 제목: `[feat] 할일 완료 경험치 트리거`
3. PR 본문 템플릿 작성 (변경 요약·테스트 방법)
4. 팀원 1인 이상 리뷰 approve 후 merge
5. **approve 없이 merge 금지**, main 직접 push 금지

---

## 🔄 CI/CD 흐름

```
[PR 생성]    → build + test + Dockerfile lint (배포 안 함)
[main merge] → Docker build(태그=git SHA) → OIDC로 ECR push
             → config 레포의 image tag 갱신
             → ArgoCD 감지 → dev 자동 sync / prod 승인 후 sync → EKS 배포
```
- **CI:** GitHub Actions, AWS 인증은 **OIDC** (Access Key 미사용)
- **CD:** ArgoCD, config 레포가 SSoT, **dev 자동 / prod 승인 게이트**

---

## 🔑 Gemini AI 보안

- 호출은 **백엔드에서만**, 추천 버튼 누를 때 온디맨드
- 키는 **HTTP 헤더**로 전달 (URL 금지 — 로그·Referer 노출 방지)
- 키 저장: **Secrets Manager (KMS 암호화)** → **ESO** 로 Pod에 주입
- 코드·이미지·프론트 어디에도 키 평문 없음
- 비용 통제: 온디맨드 호출 + Redis 캐싱

---

## 📄 문서

| 문서 | 위치 |
|------|------|
| 기획안 | <!-- 링크 추가 --> |
| 아키텍처 설계서 | <!-- 링크 추가 --> |
| 인프라 (Terraform) | team4-taskfarm-infra |
| 배포 (ArgoCD) | team4-taskfarm-config |
| 트러블슈팅 / 일일 기록 | <!-- 노션 링크 --> |

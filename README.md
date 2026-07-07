# 🌱 taskfarm — App

> 할일을 완료하면 AI가 경험치를 책정하고, 그 경험치로 농장을 키우는 **게이미피케이션 투두앱**
> Spring Boot 멀티모듈 · Docker · **EKS 위에 GitOps(ArgoCD)로 배포**

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.14-green)
![EKS](https://img.shields.io/badge/AWS-EKS%201.35-FF9900)
![CI](https://img.shields.io/badge/CI-GitHub%20Actions%20(OIDC)-2088FF)

---

## 📌 서비스 소개

taskfarm은 **할일 관리 + 농장 키우기**를 결합한 게이미피케이션 투두앱입니다.

1. 할일을 완료한다
2. 추천 버튼을 누르면 **Gemini AI가 그 할일의 경험치를 책정**한다 (유저가 원할 때만 — 온디맨드)
3. 쌓인 경험치·물방울·코인으로 **농장을 운영**(씨앗 심기·물주기·수확·주민 주문·상점)한다

> 단순 할일 체크를 넘어, 완료의 보상을 농장 성장으로 시각화해 꾸준함을 유도합니다.
> **차별점** = Todoist/Habitica/Forest 대비 **AI 경험치 추천 + 실시간 랭킹**.

---

## 🛠 기술 스택

| 영역 | 기술 |
|------|------|
| Backend | Spring Boot 3.5.14, JDK 17, JPA, Spring Security (JWT) |
| 아키텍처 | **멀티모듈** — `common` / `user`(:8080) / `admin`(:8081) |
| Frontend | HTML/CSS/JS (경량 연동) |
| DB | MySQL 8.4 (RDS) — **dev 단일 / prod Multi-AZ + Read Replica** |
| Cache | Redis (ElastiCache) — AI 응답 캐싱 · 큐(List) · 랭킹 |
| AI | Google Gemini API (경험치 책정, 온디맨드) |
| Secret | AWS Secrets Manager + ESO |
| Container | Docker (멀티스테이지), Amazon ECR |
| Orchestration | Amazon EKS 1.35 |
| CI | GitHub Actions (OIDC → ECR) |
| CD | ArgoCD (GitOps) · config 레포가 SSoT |
| Monitoring | kube-prometheus-stack, Grafana |

---

## 🧱 멀티모듈 구조

```
taskfarm-app/
├── common/     # 공통 (BaseEntity, 공통 Response/예외, 설정)
├── user/       # 사용자 앱 (:8080) — 회원·할일·AI경험치·농장·통계·랭킹·친구·업적·우편
├── admin/      # 관리자 앱 (:8081) — 유저/정책/Gemini모니터링·부하시뮬레이터·MFA
├── docker-compose.yml   # 로컬 통합 테스트 (user+admin+mysql+redis)
└── pom.xml     # 부모 POM (packaging=pom)
```

> **왜 나눴나?** 사용자 트래픽과 관리자 트래픽의 관심사·스케일 정책이 다릅니다.
> user는 KEDA로 오토스케일, admin은 고정. 배포·권한도 분리됩니다.

---

## 🗂 3-Repo 구조 (GitOps)

| 레포 | 책임 | 주요 내용 |
|------|------|-----------|
| **team4-taskfarm-app** (현재) | 애플리케이션 + CI | Spring Boot 소스, Dockerfile, GitHub Actions (OIDC → ECR) |
| **team4-taskfarm-config** | 배포 (CD) | K8s 매니페스트(Kustomize), ArgoCD Application, ExternalSecret |
| **team4-taskfarm-infra** | 인프라 | Terraform (modules + envs/dev·prod) |

> 💡 CI(app)가 이미지를 ECR에 올리고 **config 레포의 image tag를 갱신**하면,
> ArgoCD가 config 레포를 추적해 클러스터에 동기화합니다. app 레포는 클러스터를 직접 건드리지 않습니다.

---

## 🚀 로컬 실행 (개발자용)

```bash
# 1. 로컬 통합 실행 (user + admin + mysql + redis)
docker-compose up --build

# user  → http://localhost:8080
# admin → http://localhost:8081

# 2. 개별 모듈 빌드 (테스트 스킵)
./mvnw -pl user -am clean package -DskipTests
./mvnw -pl admin -am clean package -DskipTests
```

> 로컬은 `docker-compose.yml`이 MySQL·Redis까지 함께 띄웁니다.
> Gemini 키 등 민감값은 커밋 금지 — 로컬은 환경변수/`.env`(gitignore)로 주입.

---

## 🐳 컨테이너 빌드 & ECR Push (수동 참고)

> 실제 배포는 **CI가 자동으로** 합니다(아래 CI/CD 참고). 아래는 수동 확인용.

```bash
# 이미지 태그는 git SHA 사용 (latest 금지)
TAG=$(git rev-parse --short HEAD)
ECR=<account>.dkr.ecr.ap-northeast-2.amazonaws.com

# OIDC 환경이 아니면 로그인 필요
aws ecr get-login-password --region ap-northeast-2 \
  | docker login --username AWS --password-stdin $ECR

docker build -t $ECR/taskfarm-user:$TAG  -f user/Dockerfile  .
docker build -t $ECR/taskfarm-admin:$TAG -f admin/Dockerfile .
docker push $ECR/taskfarm-user:$TAG
docker push $ECR/taskfarm-admin:$TAG
```

---

## 🔄 CI/CD 흐름 (자동 배포)

```
[PR → main]   ci.yml   : build + test + Dockerfile lint (배포 안 함, 검증만)
[main merge]  cd.yml   : Docker build(태그=git SHA) → OIDC로 ECR push
                        → config 레포 overlays/{env} image tag 갱신
                        → ArgoCD 감지 → dev 자동 sync / prod 승인 후 sync → EKS 배포
```

- **CI:** GitHub Actions, AWS 인증은 **OIDC** (정적 Access Key 미사용)
- **CD:** ArgoCD, config 레포가 SSoT, **dev 자동 sync / prod 승인 게이트**
- 배포 후: readinessProbe 통과 → 트래픽 전환 → Slack 알림

---

## 🔑 Gemini AI 보안

- 호출은 **백엔드에서만**, 추천 버튼 누를 때 **온디맨드** (자동 호출 없음 → 비용 통제)
- 키는 **HTTP 헤더(Authorization)** 로 전달 (URL 금지 — 로그·Referer 노출 방지)
- 키 저장: **Secrets Manager (KMS 암호화)** → **ESO** 로 백엔드 Pod에만 주입
- 코드·이미지·프론트 어디에도 키 평문 없음
- 비용 통제: **온디맨드 호출 + Redis 캐싱** (동일 요청 캐시 적중 시 API 미호출)

---

## 🌿 브랜치 · 커밋 · PR 규칙

- **환경은 브랜치가 아닙니다.** `main` 단일 + `feature/*`. 환경(dev/prod)은 config·infra의 **오버레이/디렉터리**로. (develop 미사용)
- 브랜치: `feature/*` `fix/*` `chore/*` `ci/*` — 형식 `타입/이름_날짜/기능설명`
  예) `feature/NOEUL_20260604/todo-crud`
- 커밋: `<타입>: <한 줄 요약>` — `feat` `fix` `refactor` `chore` `ci` `docs`
- PR: `feature/* → main`만, 제목 `[feat] ...`, **1인 이상 approve 후 merge**, main 직접 push 금지

---

## ⚠️ 커밋 금지 (사고 방지)

```gitignore
*.tfstate
*.tfvars
.env
application-*.yml   # 민감값 포함 시
```
> 민감값(Gemini 키·DB 비번)은 **Secrets Manager에만.** 앱/매니페스트는 참조만.
> Gitleaks가 커밋 단계에서 시크릿 노출을 차단합니다.

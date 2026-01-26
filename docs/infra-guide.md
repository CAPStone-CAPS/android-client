# 인프라 환경 구축 가이드 (연습용)

이 문서는 팀원이 처음 Docker와 배포를 접해도 따라할 수 있도록 만든 연습용 가이드입니다.

## 구성 요소
- api 서버: FastAPI
- db: Postgres
- nginx: 외부 요청을 api 서버로 전달

## 폴더 구조
- server/: FastAPI 코드와 Dockerfile
- infra/nginx/: Nginx 설정
- docker-compose.yml: 모든 컨테이너 실행 설정
- .env.example: 환경변수 샘플

## 사전 준비 (EC2)
1) EC2 접속
- ssh -i "C:\Users\user\Downloads\garyoung_key_pair.pem" ubuntu@3.26.220.73

2) Docker 설치
- sudo apt-get update
- sudo apt-get install -y docker.io docker-compose-plugin
- sudo usermod -aG docker $USER
- 재접속 후 docker version 으로 확인

3) 포트 열기 (EC2 보안 그룹)
- 80/tcp 인바운드 허용

## 로컬 실행
1) 도커 실행 확인
- docker version
- docker compose version

2) 컨테이너 실행
- docker compose up -d --build

3) 상태 확인
- docker compose ps
- 브라우저에서 http://localhost/health

## EC2 실행 (추천 순서)
1) EC2에 코드 업로드
- git clone <REPO_URL> app
- cd app

2) 환경변수 파일 준비
- cp .env.example .env
- .env 파일에서 비밀번호와 DATABASE_URL 수정

3) 컨테이너 실행
- docker compose up -d --build

4) 외부 접속 확인
- http://3.26.220.73/health

## 배포 흐름 한 줄 요약
1) 코드 업데이트
2) docker compose up -d --build
3) /health 확인

## 자주 쓰는 로그 확인
- nginx 로그: docker compose logs -f nginx
- api 로그: docker compose logs -f server
- db 로그: docker compose logs -f db

## 체크리스트
- EC2 보안 그룹에 80 포트가 열려 있는지
- .env 파일이 서버에 있는지
- docker compose ps 에서 nginx/server/db가 모두 running 인지

## FAQ (자주 발생하는 오류 5가지)
1) 502 Bad Gateway
- 원인: nginx가 api 서버에 접근 불가
- 확인: docker compose ps / docker compose logs -f server

2) DB 연결 실패
- 원인: DATABASE_URL 오타, db 컨테이너 비정상
- 확인: docker compose logs -f db

3) 포트 80 사용 중 에러
- 원인: 이미 다른 서비스가 80 포트를 사용
- 해결: docker-compose.yml에서 포트 변경 또는 기존 서비스 종료

4) 이미지 빌드 실패
- 원인: Dockerfile 경로 또는 requirements 문제
- 확인: docker build ./server

5) 변경 사항이 반영되지 않음
- 원인: 캐시된 이미지 사용
- 해결: docker compose up -d --build

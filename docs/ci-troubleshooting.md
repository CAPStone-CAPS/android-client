# CI/CD 트러블슈팅 플로우

배포 실패 시 아래 순서대로 확인합니다.

1) GitHub Actions 탭 확인
- 실패한 워크플로우를 열고 실패 스텝을 확인

2) 로그에서 에러 메시지 찾기
- 가장 처음 나타난 에러를 찾고 원인 파악

3) 환경변수 확인
- Actions Secrets 또는 서버 .env 값 오타 여부 확인

4) EC2 접속 확인
- SSH 접속 가능 여부 확인
- 보안 그룹에서 22/tcp 인바운드 허용 여부 확인

5) Docker 재시도
- docker compose up -d --build

## 자주 실패하는 케이스
- 환경변수 누락 (DATABASE_URL 등)
- EC2 SSH 키 권한 문제
- Nginx 설정 파일 경로 오류
- Docker 이미지 태그 불일치
- 서버 디스크 부족 (docker system df)

## 배포 실패 시 체크리스트
- Secrets: EC2_HOST, EC2_USER, EC2_SSH_KEY 등록 여부
- 서버: .env 파일 존재 여부
- 서버: docker compose ps 가 정상인지

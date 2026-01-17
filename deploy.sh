#!/bin/bash

DOCKER_APP_NAME="clairedelune1018/life-manager"

echo "========== Docker 배포 시작 =========="

# 현재 실행 중인 컨테이너 확인 및 중지/삭제
if [ "$(docker ps -a -q -f name=life-manager-container)" ]; then
  echo "Existing container found. Stopping and removing..."
  docker stop life-manager-container
  docker rm life-manager-container
fi

# 기존 이미지 삭제 (용량 확보)
# 2>/dev/null || true : 이미지가 없어서 에러가 나도 무시하고 계속 진행하라는 뜻
echo "Removing old image..."
docker rmi $DOCKER_APP_NAME:latest 2>/dev/null || true

# 최신 이미지 다운로드 (Pull)
echo "Pulling new image..."
docker pull $DOCKER_APP_NAME:latest

# 새 컨테이너 실행
# -d: 백그라운드 실행
# -p 9000:9000 : 호스트 9000포트를 컨테이너 9000 포트와 연결
echo "Running container..."
docker run -d -p 9000:9000 \
  --name life-manager-container \
  -v /home/ubuntu/app/life-manager/src/main/resources/application-prod.properties:/config/application-prod.properties \
  $DOCKER_APP_NAME:latest

# (선택 사항) 사용하지 않는 불필요한 이미지 찌꺼기 정리
docker image prune -f

echo "========== Docker 배포 완료 =========="
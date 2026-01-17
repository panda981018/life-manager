#!/bin/bash

echo "========== 배포 시작 =========="

# 프로젝트 디렉토리로 이동
REPOSITORY=/home/ubuntu/app/life-manager
cd $REPOSITORY

# Git pull
echo "Git pull..."
git pull origin main

# Gradle 빌드
echo "Building..."
./gradlew clean build -x test

# 서비스 재시작
echo "Restarting service..."
sudo systemctl restart life-manager

# 상태 확인
echo "Checking status..."
sudo systemctl status life-manager --no-pager
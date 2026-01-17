FROM eclipse-temurin:17-jdk

ARG JAR_FILE=build/libs/life-manager-1.0.0.jar

COPY ${JAR_FILE} app.jar

# 실행 명령어 (프로필 설정 포함)
ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-Dspring.profiles.active=prod", "-jar", "/app.jar"]
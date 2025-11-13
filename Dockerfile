# 1단계: Build Stage
FROM gradle:8.7-jdk17 AS builder
WORKDIR /app
COPY . .
RUN gradle clean build -x test

# 2단계: Run Stage
FROM eclipse-temurin:17-jre
WORKDIR /app

# Render에서 포트 설정
ENV PORT=8080
EXPOSE 8080

# UTF-8 설정
ENV JAVA_TOOL_OPTIONS="-Xmx512m -Dfile.encoding=UTF-8"

# jar 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 실행 명령
CMD ["sh", "-c", "java $JAVA_TOOL_OPTIONS -jar app.jar"]

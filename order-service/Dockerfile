FROM openjdk:17-jdk-slim
WORKDIR /app

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
COPY src/main/resources/secure-connect-ecom-db.zip /app/resources/secure-connect-ecom-db.zip

# Poin spring to the bundle path in the container
ENV SPRING_CASSANDRA_CLOUD_SECURE_CONNECT_BUNDLE=/app/resources/secure-connect-ecom-db.zip

EXPOSE 8083
ENTRYPOINT ["java","-jar","/app/app.jar"]

FROM openjdk:17-jdk-slim

# Install mysql-client
RUN apt-get update && apt-get install -y default-mysql-client

# Copy jar and script
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
COPY wait-for-mysql.sh wait-for-mysql.sh
RUN chmod +x wait-for-mysql.sh

ENTRYPOINT ["./wait-for-mysql.sh"]

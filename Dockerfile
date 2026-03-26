# Build stage
FROM openjdk:17-slim AS build
WORKDIR /app
COPY . .
# Download the MySQL JDBC driver
RUN apt-get update && apt-get install -y curl && \
    curl -L -o mysql-connector-j-8.3.0.jar https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.3.0/mysql-connector-j-8.3.0.jar
# Compile Java files
RUN javac *.java

# Run stage
FROM openjdk:17-slim
WORKDIR /app
COPY --from=build /app/*.class /app/
COPY --from=build /app/*.html /app/
COPY --from=build /app/*.css /app/
COPY --from=build /app/*.js /app/
COPY --from=build /app/mysql-connector-j-8.3.0.jar /app/

# Port is typically handled by Hugging Face via the PORT env var (default 7860)
EXPOSE 7860

CMD ["java", "-cp", ".:mysql-connector-j-8.3.0.jar", "Main"]

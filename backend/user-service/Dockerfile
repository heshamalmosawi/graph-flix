FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src ./src

RUN chmod +x ./mvnw
RUN ./mvnw clean package -DskipTests

EXPOSE 8443

CMD ["java", "-jar", "target/userservice-1.0.0.jar"]
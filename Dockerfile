# Stage 1: Build
FROM maven:3.8.6-openjdk-18 as build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

#Stage 2: Create runtime
FROM openjdk:18-oracle
WORKDIR /app
COPY --from=build /app/target/teamTimer-0.0.1-SNAPSHOT.jar teamtimer.jar
RUN mkdir "/excels"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "teamtimer.jar"]
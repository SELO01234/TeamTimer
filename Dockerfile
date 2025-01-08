FROM openjdk:18-oracle
COPY target/teamTimer-0.0.1-SNAPSHOT.jar teamtimer.jar
RUN mkdir "/excels"
EXPOSE 8080
ENTRYPOINT ["java","-jar", "teamtimer.jar"]
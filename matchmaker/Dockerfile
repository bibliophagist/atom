FROM openjdk:8-jre-alpine
COPY build/libs/matchmaker-1.0-SNAPSHOT.jar /services/mm.jar
EXPOSE 8091
CMD ["java", "-jar", "/services/mm.jar"]
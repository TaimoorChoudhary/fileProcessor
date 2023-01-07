FROM adoptopenjdk/openjdk11:alpine
ARG JAR_FILE=*.jar
COPY ${JAR_FILE} fileprocessor-1.0-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","/fileprocessor-1.0-SNAPSHOT.jar"]
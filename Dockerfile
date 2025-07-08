FROM azul/zulu-openjdk:23-jdk AS build

WORKDIR /api

#Copy dependencies and api
COPY pom.xml .
COPY src src

# Copy Maven wrapper
COPY mvnw .
COPY .mvn .mvn

# Set execution permission for the Maven wrapper
RUN chmod +x ./mvnw
RUN ./mvnw clean package -DskipTests

FROM azul/zulu-openjdk:23-jdk
VOLUME /tmp

COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
EXPOSE 8080
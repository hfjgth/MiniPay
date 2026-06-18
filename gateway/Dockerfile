FROM azul/zulu-openjdk:21

WORKDIR /app

COPY target/gateway-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=docker"]
FROM azul/zulu-openjdk:17

WORKDIR /app

COPY target/order_service-1.0.0.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=docker"]
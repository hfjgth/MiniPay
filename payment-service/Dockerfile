FROM azul/zulu-openjdk:21

WORKDIR /app

COPY target/payment-service-1.0.0.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=docker"]
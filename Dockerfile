FROM mcr.microsoft.com/playwright/java:v1.48.0-jammy

WORKDIR /app

COPY target/immowelt-0.0.1-SNAPSHOT.jar app.jar

ENV PLAYWRIGHT_BROWSERS_PATH=/ms-playwright

ENTRYPOINT ["java", "-jar", "app.jar"]

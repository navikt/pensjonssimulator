FROM gcr.io/distroless/java21-debian12:nonroot
ENV TZ="Europe/Oslo"
COPY target/pensjonssimulator.jar /app/app.jar
CMD ["-jar", "/app/app.jar"]

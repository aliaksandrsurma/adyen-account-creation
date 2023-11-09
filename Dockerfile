#
# Build stage
#
FROM gradle:8.3-jdk17 AS build
WORKDIR /app

# Copy the source code
COPY . .

# Build the application
RUN gradle clean bootJar

# ---- Run Stage ----
FROM openjdk:17-jdk-slim

EXPOSE 8080
# Set working directory
WORKDIR /app

# Copy the executable jar from the build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Run the application
CMD ["java", "-jar", "app.jar"]
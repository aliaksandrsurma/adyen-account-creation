#
# Caching dependencies
#

FROM gradle:8.4-jdk17 as cache

RUN gradle --version && java -version
RUN mkdir -p /home/gradle/cache_home
ENV GRADLE_USER_HOME /home/gradle/cache_home

WORKDIR /app

COPY build.gradle settings.gradle .

# Eat the expected build failure since no source code has been copied yet
RUN gradle clean build -i --stacktrace --no-daemon > /dev/null 2>&1 || true

#
# Build stage
#
FROM gradle:8.4-jdk17 AS build
# Copy cached dependencies
COPY --from=cache /home/gradle/cache_home /home/gradle/.gradle

WORKDIR /app
# Copy the source code
COPY . .

# Build the application
RUN gradle bootJar -i --stacktrace

# ---- Run Stage ----
FROM openjdk:17-jdk-slim

EXPOSE 8080
# Set working directory
WORKDIR /app

# Copy the executable jar from the build stage
COPY --from=build /app/build/libs/*.jar app.jar

ENV env test
# Run the application
CMD ["java", "-jar", "app.jar"]
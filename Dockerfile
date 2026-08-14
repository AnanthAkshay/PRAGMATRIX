# Use Maven to build the application
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Use Tomcat 10/11 with JDK 17 to run the application
FROM tomcat:10.1-jdk17
# Remove default Tomcat apps to save memory
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy the built WAR file from the build stage to Tomcat's ROOT
COPY --from=build /app/target/pragmatrix2026.war /usr/local/tomcat/webapps/ROOT.war

# Expose the port Render uses
EXPOSE 8080

# Configure JVM memory limits to fit within 512MB Free Tier limits to prevent crashes
ENV JAVA_OPTS="-Xms128m -Xmx256m -XX:+UseG1GC"

CMD ["catalina.sh", "run"]

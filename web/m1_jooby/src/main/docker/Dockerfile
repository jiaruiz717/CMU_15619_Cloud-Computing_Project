# Ubuntu Linux as the base image
FROM maven:3.8.4-jdk-11

# Install the packages by using the default package manager in Ubuntu
RUN apt-get update && apt-get -y install default-jre

# Open port 80
EXPOSE 80

# Copy the source code into the container and ADD them to the desired directory inside the container image
ADD m1_jooby /

# Compile the application in the container
RUN mvn clean package

# run the JAR file of the profile service when the container starts
CMD ["java -jar target/m1_jooby-1.0.0.jar"]

# Use Bash as the container's entry point.
ENTRYPOINT ["/bin/bash", "-c"]
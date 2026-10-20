# Ubuntu Linux as the base image
FROM maven:3.8.3-jdk-8-slim

# Install the packages by using the default package manager in Ubuntu
RUN apt-get update && apt-get -y install default-jre

# Open port 80
EXPOSE 80

# Copy the source code into the container and ADD them to the desired directory inside the container image
ADD m1_vertx /

# Compile the application in the container
RUN mvn clean package

# run the JAR file of the profile service when the container starts
CMD ["mvn exec:java"]

# Use Bash as the container's entry point.
ENTRYPOINT ["/bin/bash", "-c"]
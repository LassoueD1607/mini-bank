FROM amazoncorretto:17.0.0-alpine

#Info about the owner of the image
LABEL "org.opencontainers.image.authors"="lassoued.com"
#Add the application's jar to the image
COPY target/accounts-0.0.1-SNAPSHOT.jar accounts-0.0.1-SNAPSHOT.jar

#Execute the application
ENTRYPOINT ["java", "-jar", "accounts-0.0.1-SNAPSHOT.jar"]

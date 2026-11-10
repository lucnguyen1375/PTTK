FROM maven:3.9.16-eclipse-temurin-21@sha256:99e61abcff91a9b1333463bd8451fb18495d6eba9250ac66a338b518f8278320 AS build
WORKDIR /build
COPY . .
RUN mvn -B -ntp -DskipTests clean package

FROM tomcat:10.1.61-jdk21-temurin@sha256:1f7866abf3650fb7f7befa7eb1b32c1db3e776859748de8439e4b74d9856cda1
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /build/target/pttk-m3.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080

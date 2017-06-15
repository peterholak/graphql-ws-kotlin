FROM openjdk:8-jdk-alpine
COPY install.sh /
RUN apk add --update git
RUN chmod +x install.sh
RUN /install.sh
ENTRYPOINT ["/example-1.0-SNAPSHOT/bin/example"]

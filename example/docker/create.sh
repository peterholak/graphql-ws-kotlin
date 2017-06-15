#!/bin/sh

docker run -d --rm --name graphql-ws-kotlin -p 127.0.0.1:4567:4567 --memory=200m --cpu-period="100000" --cpu-quota="50000" -e JAVA_OPTS='-Xmx100m' graphql-ws-kotlin
docker stop graphql-ws-kotlin

#!/bin/sh

cd /
git clone https://github.com/peterholak/graphql-ws-kotlin
cd graphql-ws-kotlin
./gradlew :example:build
tar -xvf example/build/distributions/example-1.0-SNAPSHOT.tar -C /

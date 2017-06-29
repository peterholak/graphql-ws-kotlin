#!/bin/sh

cd /
git clone https://github.com/peterholak/graphql-ws-kotlin
cd graphql-ws-kotlin/example
./gradlew build
tar -xvf build/distributions/example-1.0-SNAPSHOT.tar -C /

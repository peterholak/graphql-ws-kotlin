# graphql-ws-kotlin

**Unfinished / work in progress, but should be somewhat usable! Feedback welcome.**

This library makes it easy to write GraphQL servers that support subscriptions via WebSockets.

It uses the same protocol as https://github.com/apollographql/subscriptions-transport-ws, which
you can use as a client (see the frontend in `example/src/main/resources/frontend`).

## Getting started

### Run the example

- Run the `example` project (from the IDE, or by running `./gradlew :example:run`),
then open your browser at http://localhost:4567/

- You can see a live demo at http://graphql.holak.net/

### Use the library

For now, there is no released version, so you can only use the current snapshot.
Add the jitpack.io repository to your `build.gradle`.

```
repositories {
    maven { url "https://jitpack.io" }
}
```

Then add the dependency

```
compile 'com.github.peterholak:graphql-java-tools:subscription-support-SNAPSHOT'
```

## What still needs to be done

- more robust exception handling
- thread safety
- tests for more cases
- the setup is still a bit too complicated, untangle the cyclical dependencies

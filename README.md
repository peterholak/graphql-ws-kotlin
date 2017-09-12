# graphql-ws-kotlin

**Unfinished / work in progress, but should be somewhat usable! Feedback welcome.**

Example demo: http://graphql.holak.net/

---

This library makes it easy to write GraphQL servers that support subscriptions via WebSockets.
It uses [graphql-java](https://github.com/graphql-java/graphql-java) to execute GraphQL queries.

The protocol for subscriptions is taken from [apollographql/subscriptions-transport-ws](https://github.com/apollographql/subscriptions-transport-ws), which
you can use as a client (see the frontend in [`example/src/main/resources/frontend`](example/src/main/resources/frontend)).

## Getting started

### Run the example

- Run the [`example`](example) project (from the IDE, or by running `./gradlew run` in the example directory),
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
compile 'com.github.peterholak:graphql-ws-kotlin:-SNAPSHOT'
```

## What still needs to be done

- improved documentation, code examples in README
- more robust exception handling (consistent/predictable behavior no matter where an exception occurs)
- more logging
- performance improvements
    - currently, it runs the subscription query for every subscriber, even if the query is the same
    - support for batching notifications would be nice
    - as far as I can tell, variables are also needlessly coerced into the correct type on every `publish()` (it is sufficient to do this just once)
- thread safety
- tests for more cases (especially write tests where the stubs currently are)
- the setup is still a bit too complicated, untangle the cyclical dependencies (see [`Main.kt`](example/src/main/kotlin/net/holak/graphql/example/Main.kt) in the example project)
- maybe support for writing clients (including Kotlin JS target)
- all the other TODOs in the code (other operations, keep-alive, etc.)
- javadoc for most of the classes
- maybe support for using method arguments in filters, instead of using maps (see the publish for [`filteredPublishedText`](example/src/main/kotlin/net/holak/graphql/example/ExampleServer.kt#L48) in the example project)

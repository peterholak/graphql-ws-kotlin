# graphql-ws-kotlin

**Unfinished / work in progress, but should be somewhat usable! Feedback welcome.**

This library makes it easy to write GraphQL servers that support subscriptions via WebSockets.
It uses the same protocol as https://github.com/apollographql/subscriptions-transport-ws

## Getting started

- Run the `example` project (from the IDE, or by running `./gradlew :example:run`),
then open your browser at http://localhost:4567/

## What still needs to be done

- more robust exception handling
- thread safety
- tests for more cases
- the setup is still a bit too complicated, untangle the cyclical dependencies

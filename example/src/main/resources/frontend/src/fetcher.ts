/* Based on from https://github.com/apollographql/GraphiQL-Subscriptions-Fetcher */

import { SubscriptionClient } from 'subscriptions-transport-ws';
import { parse } from 'graphql';

const hasSubscriptionOperation = (graphQlParams: any) => {
  const queryDoc = parse(graphQlParams.query);

  for (let definition of queryDoc.definitions) {
    if (definition.kind === 'OperationDefinition') {
      const operation = definition.operation;
      if (operation === 'subscription' && (graphQlParams.operationName === undefined || (definition.name && graphQlParams.operationName === definition.name.value))) {
        return true;
      }
    }
  }

  return false;
};

export const graphQLFetcher = (subscriptionsClient: SubscriptionClient, fallbackFetcher: Function, subscribedCallback: (id, result, error) => any) => {
  return (graphQLParams: any) => {
    if (subscriptionsClient && hasSubscriptionOperation(graphQLParams)) {
      let id: string
      id = subscriptionsClient.subscribe({
        query: graphQLParams.query,
        variables: graphQLParams.variables,
        operationName: graphQLParams.operationName
      }, function (error, result) {
        if (error) {
          subscribedCallback(id, undefined, error)
        } else {
          subscribedCallback(id, result, undefined)
        }
      });
      return Promise.resolve("Subscription id is " + id + ". Data received via subscriptions will appear in the area above")
    } else {
      return fallbackFetcher(graphQLParams);
    }
  };
};
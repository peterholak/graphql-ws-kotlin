import * as React from 'react'
import * as ReactDOM from 'react-dom'
import * as GraphiQL from 'graphiql'
import { graphQLFetcher } from './fetcher'
import { SubscriptionClient } from 'subscriptions-transport-ws'

const subscriptionClient = new SubscriptionClient(
    'ws://' + window.location.host + "/subscriptions",
    { reconnect: true }
)

function postFetcher(payload: any) {
    return fetch(window.location.origin + '/graphql', {
            method: 'post',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        }).then((response: Response) => response.json())
}

const fetcher = graphQLFetcher(subscriptionClient, postFetcher)

const app =
    <div style={{height: 900}}>
        <GraphiQL fetcher={fetcher} />
    </div>

ReactDOM.render(
    app,
    document.getElementById('app')
)

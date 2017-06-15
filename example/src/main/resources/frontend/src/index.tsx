import * as React from 'react'
import * as ReactDOM from 'react-dom'
import * as GraphiQL from 'graphiql'
import { graphQLFetcher } from './fetcher'
import { SubscriptionClient } from 'subscriptions-transport-ws'

// Change this when running under webpack-dev-server
const host = window.location.host
// const host = 'localhost:4567'

const subscriptionClient = new SubscriptionClient(
    'ws://' + host + "/subscriptions",
    { reconnect: true }
)

function postFetcher(payload: any) {
    return fetch('http://' + host + '/graphql', {
            method: 'post',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        }).then((response: Response) => response.json())
}

interface SubscriptionData { payload: { id: string, data?: any, errors?: any }, reactKey: number }
interface State { events: SubscriptionData[], status: string }
class App extends React.Component<{}, State> {
    state: State = { events: [], status: 'disconnected' }
    fetcher = graphQLFetcher(subscriptionClient, postFetcher, this.onSubscriptionData.bind(this))
    unrelatedValueElement: HTMLInputElement
    keyCounter = 0

    componentDidMount() {
        subscriptionClient.onConnecting(() => this.setState({ status: 'connecting' }))
        subscriptionClient.onConnected(() => this.setState({ status: 'connected '}))
        subscriptionClient.onDisconnected(() => this.setState({ status: 'disconnected'}))
        subscriptionClient.onReconnecting(() => this.setState({ status: 'reconnecting'}))
        subscriptionClient.onReconnected(() => this.setState({ status: 'reconnected' }))
    }

    render() {
        return <div style={{width: '100%', height: '100%'}}>
            <div style={{height: '25%', display: 'flex'}}>
                <div style={{flexGrow: 0.25}}>
                    <div>
                        <button onClick={this.notifyUnrelated.bind(this)}>Notify unrelated</button>
                        <input defaultValue={"10"} ref={e => this.unrelatedValueElement = e} />
                        </div>
                    <div>
                        <button onClick={this.unsubscribeAll.bind(this)}>Unsubscribe all</button>
                    </div>
                    <div><button onClick={this.clearLog.bind(this)}>Clear log</button></div>
                    <div>WebSocket status: {this.state.status}</div>
                </div>
                <div style={{flexGrow: 0.75, height: '100%', overflow: 'auto'}}>
                    {this.state.events.map(e => <div key={e.reactKey}>{JSON.stringify(e.payload)}</div>)}
                </div>
            </div>
            <div style={{height: '75%'}}>
                <GraphiQL style={{flexGrow: 1}} fetcher={this.fetcher} />
            </div>
        </div>
    }

    onSubscriptionData(id, result, error) {
        this.keyCounter++
        if (result !== undefined) {
            this.setState({ events: [ ...this.state.events, { payload: { id, data: result }, reactKey: this.keyCounter } ] })
        }else{
            this.setState({ events: [ ...this.state.events, { payload: { id, errors: error }, reactKey: this.keyCounter } ] })
        }
    }

    notifyUnrelated() {
        fetch("http://" + host + "/notify-unrelated", {
            method: 'POST',
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ value: this.unrelatedValueElement.value })
        })
    }

    unsubscribeAll() {
        subscriptionClient.unsubscribeAll()
    }

    clearLog() {
        this.setState({ events: [] })
    }
}

ReactDOM.render(
    <App />,
    document.getElementById('app')
)

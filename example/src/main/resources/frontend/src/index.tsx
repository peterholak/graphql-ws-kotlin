import * as React from 'react'
import * as ReactDOM from 'react-dom'
import * as GraphiQL from 'graphiql'
import { graphQLFetcher } from './fetcher'
import { SubscriptionClient } from 'subscriptions-transport-ws'

// Change this in order to use webpack-dev-server
// const host = window.location.host
const host = 'localhost:4567'

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

interface SubscriptionData { id: string, payload: any }
interface State { events: SubscriptionData[], status: string }
class App extends React.Component<{}, State> {
    state: State = { events: [], status: 'disconnected' }
    fetcher = graphQLFetcher(subscriptionClient, postFetcher, this.onSubscriptionData.bind(this))
    unrelatedValueElement: HTMLInputElement

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
                    {this.state.events.map(e => <div>{JSON.stringify(e)}</div>)}
                </div>
            </div>
            <div style={{height: '75%'}}>
                <GraphiQL style={{flexGrow: 1}} fetcher={this.fetcher} />
            </div>
        </div>
    }

    onSubscriptionData(id, result, error) {
        if (result !== undefined) {
            this.setState({ events: [ ...this.state.events, { id, payload: result } ] })
        }else{
            this.setState({ events: [ ...this.state.events, { id, payload: error } ] })
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

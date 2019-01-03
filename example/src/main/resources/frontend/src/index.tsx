import * as React from 'react'
import * as ReactDOM from 'react-dom'
import * as GraphiQL from 'graphiql'
import { graphQLFetcher } from './fetcher'
import { SubscriptionClient } from 'subscriptions-transport-ws'
import * as defaultText from './default-text.txt'

// Change this when running under webpack-dev-server
const host = window.location.host
// const host = 'localhost:4567'

const subscriptionClient = new SubscriptionClient(
    (window.location.protocol === 'https:' ? 'wss://' : 'ws://') + host + "/subscriptions",
    { reconnect: true }
)

function postFetcher(payload: any) {
    return fetch(window.location.protocol + '//' + host + '/graphql', {
            method: 'post',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        }).then((response: Response) => response.json())
}

type ConnectionStatus = 'connected'|'connecting'|'disconnected'|'reconnected'|'reconnecting'
interface SubscriptionData { payload: { id: string, data?: any, errors?: any }, reactKey: number }
interface State { events: SubscriptionData[], status: ConnectionStatus }

class App extends React.Component<{}, State> {

    state: State = { events: [], status: 'disconnected' }
    fetcher = graphQLFetcher(subscriptionClient, postFetcher, this.onSubscriptionData.bind(this))
    publishTextElement: HTMLInputElement
    logBottomElement: HTMLSpanElement
    keyCounter = 0

    componentDidMount() {
        subscriptionClient.onConnecting(() => this.setState({ status: 'connecting' }))
        subscriptionClient.onConnected(() => this.setState({ status: 'connected'}))
        subscriptionClient.onDisconnected(() => this.setState({ status: 'disconnected'}))
        subscriptionClient.onReconnecting(() => this.setState({ status: 'reconnecting'}))
        subscriptionClient.onReconnected(() => this.setState({ status: 'reconnected' }))
    }

    componentDidUpdate(prevProps: {}, prevState: State) {
        if (this.state.events !== prevState.events) {
            this.logBottomElement.scrollIntoView()
        }
    }

    render() {
        return <div style={{width: '100%', height: '100%'}}>
            <div style={{height: '30%', display: 'flex'}}>
                <div style={{flexGrow: 0.25, overflow: 'auto'}}>
                    <h1 style={headingStyle}>graphql-ws-kotlin example</h1>
                    <div style={hintStyle}>Try subscribing to `textPublished` in the graphiql editor below!</div>
                    <div style={hintStyle}>
                        Try opening this page in multiple tabs, or even on different devices!<br />
                        You may also see messages published by other people.
                    </div>
                    <div style={{marginBottom: '10px'}}>
                        <button style={publishButtonStyle} onClick={this.publishText.bind(this)}>Publish text</button>
                        <input defaultValue={"10"} ref={e => this.publishTextElement = e} />
                    </div>
                    <div>
                        <button style={redButtonStyle} onClick={this.unsubscribeAll.bind(this)}>Unsubscribe all</button>
                        <button style={redButtonStyle} onClick={this.clearLog.bind(this)}>Clear log</button>
                    </div>
                    <div style={{...statusStyle, color: statusColor[this.state.status]}}>WebSocket status: {this.state.status}</div>
                </div>
                <div style={{flexGrow: 0.75, height: '100%', overflow: 'auto', background: '#eee'}}>
                    <h1 style={headingStyle}>Notification log</h1>
                    {this.state.events.map(e => <div key={e.reactKey}>{JSON.stringify(e.payload)}</div>)}
                    <span ref={e => this.logBottomElement = e} />
                </div>
            </div>
            <div style={{height: '70%'}}>
                <GraphiQL style={{flexGrow: 1}} fetcher={this.fetcher} defaultQuery={defaultText} />
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

    publishText() {
        fetch(window.location.protocol + "//" + host + "/publish-text", {
            method: 'POST',
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ value: this.publishTextElement.value })
        })
    }

    unsubscribeAll() {
        subscriptionClient.unsubscribeAll()
    }

    clearLog() {
        this.setState({ events: [] })
    }
}

const buttonStyle = {
    padding: '5px 10px',
    background: '#444',
    color: '#fff',
    border: '1px solid #ccc',
    boxShadow: '4px 4px 4px #eee',
    margin: '0 5px'
}

const publishButtonStyle = {
    ...buttonStyle,
    background: '#00BCD4'
}

const redButtonStyle = {
    ...buttonStyle,
    background: '#EF9A9A'
}

const headingStyle = {
    fontFamily: 'sans-serif',
    margin: '5px',
    fontSize: '100%'
}

const statusStyle = {
    margin: '10px 5px',
    fontFamily: 'sans-serif'
}

const statusColor: {[key in ConnectionStatus]: string} = {
    'connected': '#43A047',
    'connecting': '#F9A825',
    'reconnected': '#43A047',
    'reconnecting': '#F9A825',
    'disconnected': '#F44336'
}

const hintStyle = {
    fontFamily: 'sans-serif',
    margin: '5px',
    fontSize: '85%'
}

ReactDOM.render(
    <App />,
    document.getElementById('app')
)

webpackJsonp([0],{

/***/ 181:
/***/ (function(module, exports, __webpack_require__) {

"use strict";

/* Based on from https://github.com/apollographql/GraphiQL-Subscriptions-Fetcher */
Object.defineProperty(exports, "__esModule", { value: true });
var graphql_1 = __webpack_require__(7);
var hasSubscriptionOperation = function (graphQlParams) {
    var queryDoc = graphql_1.parse(graphQlParams.query);
    for (var _i = 0, _a = queryDoc.definitions; _i < _a.length; _i++) {
        var definition = _a[_i];
        if (definition.kind === 'OperationDefinition') {
            var operation = definition.operation;
            if (operation === 'subscription' && (graphQlParams.operationName === undefined || (definition.name && graphQlParams.operationName === definition.name.value))) {
                return true;
            }
        }
    }
    return false;
};
exports.graphQLFetcher = function (subscriptionsClient, fallbackFetcher, subscribedCallback) {
    return function (graphQLParams) {
        if (subscriptionsClient && hasSubscriptionOperation(graphQLParams)) {
            var id_1;
            id_1 = subscriptionsClient.subscribe({
                query: graphQLParams.query,
                variables: graphQLParams.variables,
                operationName: graphQLParams.operationName
            }, function (error, result) {
                if (error) {
                    subscribedCallback(id_1, undefined, error);
                }
                else {
                    subscribedCallback(id_1, result, undefined);
                }
            });
            return Promise.resolve("Subscription id is " + id_1 + ". Data received via subscriptions will appear in the area above");
        }
        else {
            return fallbackFetcher(graphQLParams);
        }
    };
};


/***/ }),

/***/ 184:
/***/ (function(module, exports, __webpack_require__) {

"use strict";

var __extends = (this && this.__extends) || (function () {
    var extendStatics = Object.setPrototypeOf ||
        ({ __proto__: [] } instanceof Array && function (d, b) { d.__proto__ = b; }) ||
        function (d, b) { for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p]; };
    return function (d, b) {
        extendStatics(d, b);
        function __() { this.constructor = d; }
        d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
    };
})();
Object.defineProperty(exports, "__esModule", { value: true });
var React = __webpack_require__(9);
var ReactDOM = __webpack_require__(96);
var GraphiQL = __webpack_require__(182);
var fetcher_1 = __webpack_require__(181);
var subscriptions_transport_ws_1 = __webpack_require__(183);
// Change this when running under webpack-dev-server
var host = window.location.host;
// const host = 'localhost:4567'
var subscriptionClient = new subscriptions_transport_ws_1.SubscriptionClient('ws://' + host + "/subscriptions", { reconnect: true });
function postFetcher(payload) {
    return fetch('http://' + host + '/graphql', {
        method: 'post',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    }).then(function (response) { return response.json(); });
}
var App = (function (_super) {
    __extends(App, _super);
    function App() {
        var _this = _super !== null && _super.apply(this, arguments) || this;
        _this.state = { events: [], status: 'disconnected' };
        _this.fetcher = fetcher_1.graphQLFetcher(subscriptionClient, postFetcher, _this.onSubscriptionData.bind(_this));
        _this.keyCounter = 0;
        return _this;
    }
    App.prototype.componentDidMount = function () {
        var _this = this;
        subscriptionClient.onConnecting(function () { return _this.setState({ status: 'connecting' }); });
        subscriptionClient.onConnected(function () { return _this.setState({ status: 'connected ' }); });
        subscriptionClient.onDisconnected(function () { return _this.setState({ status: 'disconnected' }); });
        subscriptionClient.onReconnecting(function () { return _this.setState({ status: 'reconnecting' }); });
        subscriptionClient.onReconnected(function () { return _this.setState({ status: 'reconnected' }); });
    };
    App.prototype.render = function () {
        var _this = this;
        return React.createElement("div", { style: { width: '100%', height: '100%' } },
            React.createElement("div", { style: { height: '25%', display: 'flex' } },
                React.createElement("div", { style: { flexGrow: 0.25 } },
                    React.createElement("div", null,
                        React.createElement("button", { onClick: this.notifyUnrelated.bind(this) }, "Notify unrelated"),
                        React.createElement("input", { defaultValue: "10", ref: function (e) { return _this.unrelatedValueElement = e; } })),
                    React.createElement("div", null,
                        React.createElement("button", { onClick: this.unsubscribeAll.bind(this) }, "Unsubscribe all")),
                    React.createElement("div", null,
                        React.createElement("button", { onClick: this.clearLog.bind(this) }, "Clear log")),
                    React.createElement("div", null,
                        "WebSocket status: ",
                        this.state.status)),
                React.createElement("div", { style: { flexGrow: 0.75, height: '100%', overflow: 'auto' } }, this.state.events.map(function (e) { return React.createElement("div", { key: e.reactKey }, JSON.stringify(e.payload)); }))),
            React.createElement("div", { style: { height: '75%' } },
                React.createElement(GraphiQL, { style: { flexGrow: 1 }, fetcher: this.fetcher })));
    };
    App.prototype.onSubscriptionData = function (id, result, error) {
        this.keyCounter++;
        if (result !== undefined) {
            this.setState({ events: this.state.events.concat([{ payload: { id: id, data: result }, reactKey: this.keyCounter }]) });
        }
        else {
            this.setState({ events: this.state.events.concat([{ payload: { id: id, errors: error }, reactKey: this.keyCounter }]) });
        }
    };
    App.prototype.notifyUnrelated = function () {
        fetch("http://" + host + "/notify-unrelated", {
            method: 'POST',
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ value: this.unrelatedValueElement.value })
        });
    };
    App.prototype.unsubscribeAll = function () {
        subscriptionClient.unsubscribeAll();
    };
    App.prototype.clearLog = function () {
        this.setState({ events: [] });
    };
    return App;
}(React.Component));
ReactDOM.render(React.createElement(App, null), document.getElementById('app'));


/***/ })

},[184]);
//# sourceMappingURL=app.js.map
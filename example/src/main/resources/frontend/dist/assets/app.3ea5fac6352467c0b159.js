webpackJsonp([0],{

/***/ 181:
/***/ (function(module, exports, __webpack_require__) {

"use strict";

/*
Taken from https://github.com/apollographql/GraphiQL-Subscriptions-Fetcher
under the following license:

MIT License

Copyright (c) 2017 Apollo GraphQL

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

*/
Object.defineProperty(exports, "__esModule", { value: true });
var graphql_1 = __webpack_require__(7);
var hasSubscriptionOperation = function (graphQlParams) {
    var queryDoc = graphql_1.parse(graphQlParams.query);
    for (var _i = 0, _a = queryDoc.definitions; _i < _a.length; _i++) {
        var definition = _a[_i];
        if (definition.kind === 'OperationDefinition') {
            var operation = definition.operation;
            if (operation === 'subscription') {
                return true;
            }
        }
    }
    return false;
};
exports.graphQLFetcher = function (subscriptionsClient, fallbackFetcher) {
    var activeSubscriptionId = null;
    return function (graphQLParams) {
        if (subscriptionsClient && activeSubscriptionId !== null) {
            subscriptionsClient.unsubscribe(activeSubscriptionId);
        }
        if (subscriptionsClient && hasSubscriptionOperation(graphQLParams)) {
            return {
                subscribe: function (observer) {
                    observer.next('Your subscription data will appear here after server publication!');
                    activeSubscriptionId = subscriptionsClient.subscribe({
                        query: graphQLParams.query,
                        variables: graphQLParams.variables,
                    }, function (error, result) {
                        if (error) {
                            observer.error(error);
                        }
                        else {
                            observer.next(result);
                        }
                    });
                },
            };
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

Object.defineProperty(exports, "__esModule", { value: true });
var React = __webpack_require__(9);
var ReactDOM = __webpack_require__(96);
var GraphiQL = __webpack_require__(182);
var fetcher_1 = __webpack_require__(181);
var subscriptions_transport_ws_1 = __webpack_require__(183);
var subscriptionClient = new subscriptions_transport_ws_1.SubscriptionClient('ws://' + window.location.host + "/subscriptions", { reconnect: true });
function postFetcher(payload) {
    return fetch(window.location.origin + '/graphql', {
        method: 'post',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    }).then(function (response) { return response.json(); });
}
var fetcher = fetcher_1.graphQLFetcher(subscriptionClient, postFetcher);
var app = React.createElement("div", { style: { height: 900 } },
    React.createElement(GraphiQL, { fetcher: fetcher }));
ReactDOM.render(app, document.getElementById('app'));


/***/ })

},[184]);
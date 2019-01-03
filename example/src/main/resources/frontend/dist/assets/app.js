webpackJsonp([0],{176:function(e,t,n){"use strict";Object.defineProperty(t,"__esModule",{value:!0});var o=n(6),i=function(e){for(var t=o.parse(e.query),n=0,i=t.definitions;n<i.length;n++){var r=i[n];if("OperationDefinition"===r.kind){if("subscription"===r.operation&&(void 0===e.operationName||r.name&&e.operationName===r.name.value))return!0}}return!1};t.graphQLFetcher=function(e,t,n){return function(o){if(i(o)){var r=e.subscribe({query:o.query,variables:o.variables,operationName:o.operationName},function(e,t){e?n(r,void 0,e):n(r,t,void 0)});return Promise.resolve("Subscription id is "+r+". Data received via subscriptions will appear in the notification log above.")}return t(o)}}},178:function(e,t){e.exports="subscription {\r\n  # Try pressing Ctrl+Space inside this block.\r\n  # You can only subscribe to a single subscription in one go.\r\n}\r\n\r\n# There are some queries and mutations in the schema as well...\r\n# Explore the schema in the Docs sidebar on the right side of the page.\r\n"},180:function(e,t,n){"use strict";function o(e){return fetch(window.location.protocol+"//"+h+"/graphql",{method:"post",headers:{"Content-Type":"application/json"},body:JSON.stringify(e)}).then(function(e){return e.json()})}var i=this&&this.__extends||function(){var e=Object.setPrototypeOf||{__proto__:[]}instanceof Array&&function(e,t){e.__proto__=t}||function(e,t){for(var n in t)t.hasOwnProperty(n)&&(e[n]=t[n])};return function(t,n){function o(){this.constructor=t}e(t,n),t.prototype=null===n?Object.create(n):(o.prototype=n.prototype,new o)}}(),r=this&&this.__assign||Object.assign||function(e){for(var t,n=1,o=arguments.length;n<o;n++){t=arguments[n];for(var i in t)Object.prototype.hasOwnProperty.call(t,i)&&(e[i]=t[i])}return e};Object.defineProperty(t,"__esModule",{value:!0});var s=n(8),a=n(92),c=n(177),l=n(176),u=n(179),p=n(178),h=window.location.host,d=new u.SubscriptionClient(("https:"===window.location.protocol?"wss://":"ws://")+h+"/subscriptions",{reconnect:!0}),f=function(e){function t(){var t=null!==e&&e.apply(this,arguments)||this;return t.state={events:[],status:"disconnected"},t.fetcher=l.graphQLFetcher(d,o,t.onSubscriptionData.bind(t)),t.keyCounter=0,t}return i(t,e),t.prototype.componentDidMount=function(){var e=this;d.onConnecting(function(){return e.setState({status:"connecting"})}),d.onConnected(function(){return e.setState({status:"connected"})}),d.onDisconnected(function(){return e.setState({status:"disconnected"})}),d.onReconnecting(function(){return e.setState({status:"reconnecting"})}),d.onReconnected(function(){return e.setState({status:"reconnected"})})},t.prototype.componentDidUpdate=function(e,t){this.state.events!==t.events&&this.logBottomElement.scrollIntoView()},t.prototype.render=function(){var e=this;return s.createElement("div",{style:{width:"100%",height:"100%"}},s.createElement("div",{style:{height:"30%",display:"flex"}},s.createElement("div",{style:{flexGrow:.25,overflow:"auto"}},s.createElement("h1",{style:v},"graphql-ws-kotlin example"),s.createElement("div",{style:E},"Try subscribing to `textPublished` in the graphiql editor below!"),s.createElement("div",{style:E},"Try opening this page in multiple tabs, or even on different devices!",s.createElement("br",null),"You may also see messages published by other people."),s.createElement("div",{style:{marginBottom:"10px"}},s.createElement("button",{style:b,onClick:this.publishText.bind(this)},"Publish text"),s.createElement("input",{defaultValue:"10",ref:function(t){return e.publishTextElement=t}})),s.createElement("div",null,s.createElement("button",{style:m,onClick:this.unsubscribeAll.bind(this)},"Unsubscribe all"),s.createElement("button",{style:m,onClick:this.clearLog.bind(this)},"Clear log")),s.createElement("div",{style:r({},g,{color:x[this.state.status]})},"WebSocket status: ",this.state.status)),s.createElement("div",{style:{flexGrow:.75,height:"100%",overflow:"auto",background:"#eee"}},s.createElement("h1",{style:v},"Notification log"),this.state.events.map(function(e){return s.createElement("div",{key:e.reactKey},JSON.stringify(e.payload))}),s.createElement("span",{ref:function(t){return e.logBottomElement=t}}))),s.createElement("div",{style:{height:"70%"}},s.createElement(c,{style:{flexGrow:1},fetcher:this.fetcher,defaultQuery:p})))},t.prototype.onSubscriptionData=function(e,t,n){this.keyCounter++,void 0!==t?this.setState({events:this.state.events.concat([{payload:{id:e,data:t},reactKey:this.keyCounter}])}):this.setState({events:this.state.events.concat([{payload:{id:e,errors:n},reactKey:this.keyCounter}])})},t.prototype.publishText=function(){fetch("http://"+h+"/publish-text",{method:"POST",headers:{"Content-Type":"application/json"},body:JSON.stringify({value:this.publishTextElement.value})})},t.prototype.unsubscribeAll=function(){d.unsubscribeAll()},t.prototype.clearLog=function(){this.setState({events:[]})},t}(s.Component),y={padding:"5px 10px",background:"#444",color:"#fff",border:"1px solid #ccc",boxShadow:"4px 4px 4px #eee",margin:"0 5px"},b=r({},y,{background:"#00BCD4"}),m=r({},y,{background:"#EF9A9A"}),v={fontFamily:"sans-serif",margin:"5px",fontSize:"100%"},g={margin:"10px 5px",fontFamily:"sans-serif"},x={connected:"#43A047",connecting:"#F9A825",reconnected:"#43A047",reconnecting:"#F9A825",disconnected:"#F44336"},E={fontFamily:"sans-serif",margin:"5px",fontSize:"85%"};a.render(s.createElement(f,null),document.getElementById("app"))}},[180]);
//# sourceMappingURL=app.js.map
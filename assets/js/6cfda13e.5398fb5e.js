"use strict";(self.webpackChunkfmodel=self.webpackChunkfmodel||[]).push([[471],{3905:(e,t,n)=>{n.d(t,{Zo:()=>u,kt:()=>f});var r=n(7294);function a(e,t,n){return t in e?Object.defineProperty(e,t,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[t]=n,e}function o(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var r=Object.getOwnPropertySymbols(e);t&&(r=r.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,r)}return n}function i(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?o(Object(n),!0).forEach((function(t){a(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):o(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}function s(e,t){if(null==e)return{};var n,r,a=function(e,t){if(null==e)return{};var n,r,a={},o=Object.keys(e);for(r=0;r<o.length;r++)n=o[r],t.indexOf(n)>=0||(a[n]=e[n]);return a}(e,t);if(Object.getOwnPropertySymbols){var o=Object.getOwnPropertySymbols(e);for(r=0;r<o.length;r++)n=o[r],t.indexOf(n)>=0||Object.prototype.propertyIsEnumerable.call(e,n)&&(a[n]=e[n])}return a}var l=r.createContext({}),d=function(e){var t=r.useContext(l),n=t;return e&&(n="function"==typeof e?e(t):i(i({},t),e)),n},u=function(e){var t=d(e.components);return r.createElement(l.Provider,{value:t},e.children)},c="mdxType",p={inlineCode:"code",wrapper:function(e){var t=e.children;return r.createElement(r.Fragment,{},t)}},m=r.forwardRef((function(e,t){var n=e.components,a=e.mdxType,o=e.originalType,l=e.parentName,u=s(e,["components","mdxType","originalType","parentName"]),c=d(n),m=a,f=c["".concat(l,".").concat(m)]||c[m]||p[m]||o;return n?r.createElement(f,i(i({ref:t},u),{},{components:n})):r.createElement(f,i({ref:t},u))}));function f(e,t){var n=arguments,a=t&&t.mdxType;if("string"==typeof e||a){var o=n.length,i=new Array(o);i[0]=m;var s={};for(var l in t)hasOwnProperty.call(t,l)&&(s[l]=t[l]);s.originalType=e,s[c]="string"==typeof e?e:a,i[1]=s;for(var d=2;d<o;d++)i[d]=n[d];return r.createElement.apply(null,i)}return r.createElement.apply(null,n)}m.displayName="MDXCreateElement"},5162:(e,t,n)=>{n.d(t,{Z:()=>i});var r=n(7294),a=n(6010);const o={tabItem:"tabItem_Ymn6"};function i(e){let{children:t,hidden:n,className:i}=e;return r.createElement("div",{role:"tabpanel",className:(0,a.Z)(o.tabItem,i),hidden:n},t)}},4866:(e,t,n)=>{n.d(t,{Z:()=>E});var r=n(7462),a=n(7294),o=n(6010),i=n(2466),s=n(6550),l=n(1980),d=n(7392),u=n(12);function c(e){return function(e){return a.Children.map(e,(e=>{if(!e||(0,a.isValidElement)(e)&&function(e){const{props:t}=e;return!!t&&"object"==typeof t&&"value"in t}(e))return e;throw new Error(`Docusaurus error: Bad <Tabs> child <${"string"==typeof e.type?e.type:e.type.name}>: all children of the <Tabs> component should be <TabItem>, and every <TabItem> should have a unique "value" prop.`)}))?.filter(Boolean)??[]}(e).map((e=>{let{props:{value:t,label:n,attributes:r,default:a}}=e;return{value:t,label:n,attributes:r,default:a}}))}function p(e){const{values:t,children:n}=e;return(0,a.useMemo)((()=>{const e=t??c(n);return function(e){const t=(0,d.l)(e,((e,t)=>e.value===t.value));if(t.length>0)throw new Error(`Docusaurus error: Duplicate values "${t.map((e=>e.value)).join(", ")}" found in <Tabs>. Every value needs to be unique.`)}(e),e}),[t,n])}function m(e){let{value:t,tabValues:n}=e;return n.some((e=>e.value===t))}function f(e){let{queryString:t=!1,groupId:n}=e;const r=(0,s.k6)(),o=function(e){let{queryString:t=!1,groupId:n}=e;if("string"==typeof t)return t;if(!1===t)return null;if(!0===t&&!n)throw new Error('Docusaurus error: The <Tabs> component groupId prop is required if queryString=true, because this value is used as the search param name. You can also provide an explicit value such as queryString="my-search-param".');return n??null}({queryString:t,groupId:n});return[(0,l._X)(o),(0,a.useCallback)((e=>{if(!o)return;const t=new URLSearchParams(r.location.search);t.set(o,e),r.replace({...r.location,search:t.toString()})}),[o,r])]}function v(e){const{defaultValue:t,queryString:n=!1,groupId:r}=e,o=p(e),[i,s]=(0,a.useState)((()=>function(e){let{defaultValue:t,tabValues:n}=e;if(0===n.length)throw new Error("Docusaurus error: the <Tabs> component requires at least one <TabItem> children component");if(t){if(!m({value:t,tabValues:n}))throw new Error(`Docusaurus error: The <Tabs> has a defaultValue "${t}" but none of its children has the corresponding value. Available values are: ${n.map((e=>e.value)).join(", ")}. If you intend to show no default tab, use defaultValue={null} instead.`);return t}const r=n.find((e=>e.default))??n[0];if(!r)throw new Error("Unexpected error: 0 tabValues");return r.value}({defaultValue:t,tabValues:o}))),[l,d]=f({queryString:n,groupId:r}),[c,v]=function(e){let{groupId:t}=e;const n=function(e){return e?`docusaurus.tab.${e}`:null}(t),[r,o]=(0,u.Nk)(n);return[r,(0,a.useCallback)((e=>{n&&o.set(e)}),[n,o])]}({groupId:r}),b=(()=>{const e=l??c;return m({value:e,tabValues:o})?e:null})();(0,a.useLayoutEffect)((()=>{b&&s(b)}),[b]);return{selectedValue:i,selectValue:(0,a.useCallback)((e=>{if(!m({value:e,tabValues:o}))throw new Error(`Can't select invalid tab value=${e}`);s(e),d(e),v(e)}),[d,v,o]),tabValues:o}}var b=n(2389);const g={tabList:"tabList__CuJ",tabItem:"tabItem_LNqP"};function h(e){let{className:t,block:n,selectedValue:s,selectValue:l,tabValues:d}=e;const u=[],{blockElementScrollPositionUntilNextRender:c}=(0,i.o5)(),p=e=>{const t=e.currentTarget,n=u.indexOf(t),r=d[n].value;r!==s&&(c(t),l(r))},m=e=>{let t=null;switch(e.key){case"Enter":p(e);break;case"ArrowRight":{const n=u.indexOf(e.currentTarget)+1;t=u[n]??u[0];break}case"ArrowLeft":{const n=u.indexOf(e.currentTarget)-1;t=u[n]??u[u.length-1];break}}t?.focus()};return a.createElement("ul",{role:"tablist","aria-orientation":"horizontal",className:(0,o.Z)("tabs",{"tabs--block":n},t)},d.map((e=>{let{value:t,label:n,attributes:i}=e;return a.createElement("li",(0,r.Z)({role:"tab",tabIndex:s===t?0:-1,"aria-selected":s===t,key:t,ref:e=>u.push(e),onKeyDown:m,onClick:p},i,{className:(0,o.Z)("tabs__item",g.tabItem,i?.className,{"tabs__item--active":s===t})}),n??t)})))}function k(e){let{lazy:t,children:n,selectedValue:r}=e;const o=(Array.isArray(n)?n:[n]).filter(Boolean);if(t){const e=o.find((e=>e.props.value===r));return e?(0,a.cloneElement)(e,{className:"margin-top--md"}):null}return a.createElement("div",{className:"margin-top--md"},o.map(((e,t)=>(0,a.cloneElement)(e,{key:t,hidden:e.props.value!==r}))))}function y(e){const t=v(e);return a.createElement("div",{className:(0,o.Z)("tabs-container",g.tabList)},a.createElement(h,(0,r.Z)({},e,t)),a.createElement(k,(0,r.Z)({},e,t)))}function E(e){const t=(0,b.Z)();return a.createElement(y,(0,r.Z)({key:String(t)},e))}},4684:(e,t,n)=>{n.r(t),n.d(t,{assets:()=>u,contentTitle:()=>l,default:()=>f,frontMatter:()=>s,metadata:()=>d,toc:()=>c});var r=n(7462),a=(n(7294),n(3905)),o=n(4866),i=n(5162);const s={sidebar_position:5},l="Specification By Example",d={unversionedId:"domain/specification-by-example",id:"domain/specification-by-example",title:"Specification By Example",description:"- It is a collaborative approach to software analysis and testing.",source:"@site/docs/domain/specification-by-example.md",sourceDirName:"domain",slug:"/domain/specification-by-example",permalink:"/fmodel/docs/domain/specification-by-example",draft:!1,tags:[],version:"current",sidebarPosition:5,frontMatter:{sidebar_position:5},sidebar:"tutorialSidebar",previous:{title:"Aggregating the Behaviour",permalink:"/fmodel/docs/domain/aggregating-the-behaviour"},next:{title:"Composing The Application",permalink:"/fmodel/docs/category/composing-the-application"}},u={},c=[{value:"Illustrating requirements using examples",id:"illustrating-requirements-using-examples",level:2},{value:"Refining specifications",id:"refining-specifications",level:2},{value:"Automating tests based on examples",id:"automating-tests-based-on-examples",level:2}],p={toc:c},m="wrapper";function f(e){let{components:t,...s}=e;return(0,a.kt)(m,(0,r.Z)({},p,s,{components:t,mdxType:"MDXLayout"}),(0,a.kt)("h1",{id:"specification-by-example"},"Specification By Example"),(0,a.kt)("ul",null,(0,a.kt)("li",{parentName:"ul"},"It is a collaborative approach to software analysis and testing."),(0,a.kt)("li",{parentName:"ul"},"It is the fastest way to align people from different roles on what exactly we need to build and how to test it.")),(0,a.kt)("h2",{id:"illustrating-requirements-using-examples"},"Illustrating requirements using examples"),(0,a.kt)("p",null,"The requirements are presented as scenarios.\nA scenario is an example of the system\u2019s behavior from the users\u2019 perspective,\nand they are specified using the ",(0,a.kt)("inlineCode",{parentName:"p"},"Given-When-Then")," structure to create a testable specification:"),(0,a.kt)("ul",null,(0,a.kt)("li",{parentName:"ul"},"Given ",(0,a.kt)("inlineCode",{parentName:"li"},"< some precondition(s) >")),(0,a.kt)("li",{parentName:"ul"},"When ",(0,a.kt)("inlineCode",{parentName:"li"},"< an action/trigger occurs >")),(0,a.kt)("li",{parentName:"ul"},"Then ",(0,a.kt)("inlineCode",{parentName:"li"},"< some post condition >"))),(0,a.kt)("p",null,"We face business with specific questions they should be able to answer.\nWe are not facing them with abstractions or generalizations.\nWe are dealing only with data that are formally representing preconditions (events), actions (commands) and post\nconditions (new events):"),(0,a.kt)("ul",null,(0,a.kt)("li",{parentName:"ul"},"Given ",(0,a.kt)("inlineCode",{parentName:"li"},"< some event(s) / current state of our system > ")),(0,a.kt)("li",{parentName:"ul"},"When ",(0,a.kt)("inlineCode",{parentName:"li"},"< a command occurs >")),(0,a.kt)("li",{parentName:"ul"},"Then ",(0,a.kt)("inlineCode",{parentName:"li"},"< some new event(s) / evolves to the new state of our system >"))),(0,a.kt)("p",null,"It also represents an acceptance criterion of the system, and acts as a documentation."),(0,a.kt)("h2",{id:"refining-specifications"},"Refining specifications"),(0,a.kt)("p",null,"We need to go through all the scenarios, successes and errors.\nFor example, for an ",(0,a.kt)("inlineCode",{parentName:"p"},"OrderDecider")," with given ",(0,a.kt)("inlineCode",{parentName:"p"},"OrderCreatedEvent"),"  event as a precondition, when\ncommand ",(0,a.kt)("inlineCode",{parentName:"p"},"MarkOrderAsPreparedCommand")," is triggered, then Order is successfully prepared (",(0,a.kt)("inlineCode",{parentName:"p"},"OrderPreparedEvent"),").\nBut, without ",(0,a.kt)("inlineCode",{parentName:"p"},"OrderCreatedEvent")," given as precondition, handling the same command ",(0,a.kt)("inlineCode",{parentName:"p"},"MarkOrderAsPreparedCommand")," will\nproduce different result/failure (",(0,a.kt)("inlineCode",{parentName:"p"},"OrderNotPreparedEvent"),")."),(0,a.kt)("p",null,"It means that order can be marked as prepared only if it was previously created/placed."),(0,a.kt)("p",null,(0,a.kt)("img",{alt:"spec image",src:n(1516).Z,width:"2387",height:"1675"})),(0,a.kt)("h2",{id:"automating-tests-based-on-examples"},"Automating tests based on examples"),(0,a.kt)("p",null,"Functions/lambda offers the algebra of manipulating the data (commands, events, state) in a compositional manner,\neffectively modeling the behavior.\nThis leads to modularity in design and a clear separation of the entity\u2019s structure and functions/behaviour of the\nentity. ",(0,a.kt)("strong",{parentName:"p"},"It makes it is easy to test!")),(0,a.kt)("p",null,"You can create a small DSL in Kotlin to write and run specifications in ",(0,a.kt)("inlineCode",{parentName:"p"},"Given-When-Then")," structure (testable\nspecification):"),(0,a.kt)(o.Z,{groupId:"component-type",queryString:"component-type",mdxType:"Tabs"},(0,a.kt)(i.Z,{value:"decider",label:"Decider",mdxType:"TabItem"},(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-kotlin"},"fun <C, S, E> IDecider<C, S, E>.givenEvents(events: Iterable<E>, command: () -> C): Flow<E> =\n    decide(command(), events.fold(initialState) { s, e -> evolve(s, e) })\n\nfun <C> whenCommand(command: C): C = command\n\nsuspend infix fun <E> Flow<E>.thenEvents(expected: Iterable<E>) = assertIterableEquals(expected, toList())\n"))),(0,a.kt)(i.Z,{value:"view",label:"View",mdxType:"TabItem"},(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-kotlin"},"fun <S, E> IView<S, E>.givenEvents(events: Iterable<E>) = events.fold(initialState) { s, e -> evolve(s, e) }\n\ninfix fun <S, U : S> S.thenState(expected: U?) = assertEquals(expected, this)\n"))),(0,a.kt)(i.Z,{value:"saga",label:"Saga",mdxType:"TabItem"},(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-kotlin"},"fun <AR, A> ISaga<AR, A>.whenActionResult(actionResults: AR) = react(actionResults)\n\nsuspend infix fun <A> Flow<A>.expectActions(expected: Iterable<A>) = assertIterableEquals(expected, toList())\n\n")))),(0,a.kt)("p",null,"Runnable tests:"),(0,a.kt)(o.Z,{groupId:"component-type",queryString:"component-type",mdxType:"Tabs"},(0,a.kt)(i.Z,{value:"decider",label:"Decider",mdxType:"TabItem"},(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-kotlin"},'@Test\nfun testCreateOrder(): Unit = runBlocking {\n        val createOrderCommand = CreateOrderCommand(orderId, restaurantId, orderLineItems)\n        val orderCreatedEvent = OrderCreatedEvent(orderId, orderLineItems, restaurantId)\n        \n        with(orderDecider) {\n            givenEvents(emptyList()) {                      // PRE CONDITIONS\n                whenCommand(createOrderCommand)             // ACTION\n            } thenEvents listOf(orderCreatedEvent)          // POST CONDITIONS\n        }\n    }\n\n@Test\nfun testMarkOrderAsPrepared(): Unit = runBlocking {\n    val orderCreatedEvent = OrderCreatedEvent(orderId, orderLineItems, restaurantId)\n    val markOrderAsPreparedCommand = MarkOrderAsPreparedCommand(orderId)\n    val orderPreparedEvent = OrderPreparedEvent(orderId)\n\n    with(orderDecider) {\n        givenEvents(listOf(orderCreatedEvent)) {         // PRE CONDITIONS\n            whenCommand(markOrderAsPreparedCommand)      // ACTION\n        } thenEvents listOf(orderPreparedEvent)          // POST CONDITIONS\n    }\n}\n\n@Test\nfun testMarkOrderAsPreparedDoesNotExistError(): Unit = runBlocking {\n    val markOrderAsPreparedCommand = MarkOrderAsPreparedCommand(orderId)\n    val orderNotPreparedEvent = OrderNotPreparedEvent(orderId, Reason("Order does not exist"))\n\n    with(orderDecider) {\n        givenEvents(emptyList()) {                       // PRE CONDITIONS\n            whenCommand(markOrderAsPreparedCommand)      // ACTION\n        } thenEvents listOf(orderNotPreparedEvent)       // POST CONDITIONS\n    }\n}\n'))),(0,a.kt)(i.Z,{value:"view",label:"View",mdxType:"TabItem"},(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-kotlin"},"@Test\nfun testOrderCreated(): Unit = runBlocking {\n        val orderCreatedEvent = OrderCreatedEvent(orderId, orderLineItems, restaurantId)\n        val orderViewState = OrderViewState(orderId, restaurantId, orderCreatedEvent.status, orderLineItems)\n\n        with(orderView) {\n            givenEvents(\n                listOf(orderCreatedEvent)\n            ) thenState orderViewState\n        }\n    }\n\n@Test\nfun testOrderPrepared(): Unit = runBlocking {\n    val orderCreatedEvent = OrderCreatedEvent(orderId, orderLineItems, restaurantId)\n    val orderPreparedEvent = OrderPreparedEvent(orderId)\n    val orderViewState = OrderViewState(orderId, restaurantId, orderPreparedEvent.status, orderLineItems)\n\n    with(orderView) {\n        givenEvents(\n            listOf(orderCreatedEvent, orderPreparedEvent)\n        ) thenState orderViewState\n    }\n}\n\n@Test\nfun testOrderPreparedDoesNotExistOrderError(): Unit = runBlocking {\n    val orderPreparedEvent = OrderPreparedEvent(orderId)\n    with(orderView) {\n        givenEvents(\n            listOf(orderPreparedEvent)\n        ) thenState null\n    }\n}\n"))),(0,a.kt)(i.Z,{value:"saga",label:"Saga",mdxType:"TabItem"},(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-kotlin"},"@Test\nfun testOrderPlacedAtRestaurantEvent(): Unit = runBlocking {\n        val orderPlacedAtRestaurantEvent = OrderPlacedAtRestaurantEvent(restaurantId, orderLineItems, orderId)\n        val createOrderCommand = CreateOrderCommand(orderId, restaurantId, orderLineItems)\n\n        with(orderSaga) {\n            whenActionResult(\n                orderPlacedAtRestaurantEvent\n            ) expectActions listOf(createOrderCommand)\n        }\n    }\n")))))}f.isMDXComponent=!0},1516:(e,t,n)=>{n.d(t,{Z:()=>r});const r=n.p+"assets/images/spec-by-example-53fb5df5cb1d604d6701aea1021a2c13.jpg"}}]);
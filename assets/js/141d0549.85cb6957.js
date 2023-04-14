"use strict";(self.webpackChunkfmodel=self.webpackChunkfmodel||[]).push([[534],{3905:(e,t,n)=>{n.d(t,{Zo:()=>p,kt:()=>h});var r=n(7294);function a(e,t,n){return t in e?Object.defineProperty(e,t,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[t]=n,e}function i(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var r=Object.getOwnPropertySymbols(e);t&&(r=r.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,r)}return n}function o(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?i(Object(n),!0).forEach((function(t){a(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):i(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}function s(e,t){if(null==e)return{};var n,r,a=function(e,t){if(null==e)return{};var n,r,a={},i=Object.keys(e);for(r=0;r<i.length;r++)n=i[r],t.indexOf(n)>=0||(a[n]=e[n]);return a}(e,t);if(Object.getOwnPropertySymbols){var i=Object.getOwnPropertySymbols(e);for(r=0;r<i.length;r++)n=i[r],t.indexOf(n)>=0||Object.prototype.propertyIsEnumerable.call(e,n)&&(a[n]=e[n])}return a}var l=r.createContext({}),d=function(e){var t=r.useContext(l),n=t;return e&&(n="function"==typeof e?e(t):o(o({},t),e)),n},p=function(e){var t=d(e.components);return r.createElement(l.Provider,{value:t},e.children)},m="mdxType",c={inlineCode:"code",wrapper:function(e){var t=e.children;return r.createElement(r.Fragment,{},t)}},u=r.forwardRef((function(e,t){var n=e.components,a=e.mdxType,i=e.originalType,l=e.parentName,p=s(e,["components","mdxType","originalType","parentName"]),m=d(n),u=a,h=m["".concat(l,".").concat(u)]||m[u]||c[u]||i;return n?r.createElement(h,o(o({ref:t},p),{},{components:n})):r.createElement(h,o({ref:t},p))}));function h(e,t){var n=arguments,a=t&&t.mdxType;if("string"==typeof e||a){var i=n.length,o=new Array(i);o[0]=u;var s={};for(var l in t)hasOwnProperty.call(t,l)&&(s[l]=t[l]);s.originalType=e,s[m]="string"==typeof e?e:a,o[1]=s;for(var d=2;d<i;d++)o[d]=n[d];return r.createElement.apply(null,o)}return r.createElement.apply(null,n)}u.displayName="MDXCreateElement"},2019:(e,t,n)=>{n.r(t),n.d(t,{assets:()=>l,contentTitle:()=>o,default:()=>c,frontMatter:()=>i,metadata:()=>s,toc:()=>d});var r=n(7462),a=(n(7294),n(3905));const i={sidebar_position:1},o="Discovering the Domain",s={unversionedId:"domain/discovering-the-domain",id:"domain/discovering-the-domain",title:"Discovering the Domain",description:"There are numerous techniques to discover a domain.",source:"@site/docs/domain/discovering-the-domain.md",sourceDirName:"domain",slug:"/domain/discovering-the-domain",permalink:"/fmodel/docs/domain/discovering-the-domain",draft:!1,tags:[],version:"current",sidebarPosition:1,frontMatter:{sidebar_position:1},sidebar:"tutorialSidebar",previous:{title:"Modeling The Domain",permalink:"/fmodel/docs/category/modeling-the-domain"},next:{title:"Structuring the data",permalink:"/fmodel/docs/domain/structuring-the-data"}},l={},d=[],p={toc:d},m="wrapper";function c(e){let{components:t,...i}=e;return(0,a.kt)(m,(0,r.Z)({},p,i,{components:t,mdxType:"MDXLayout"}),(0,a.kt)("h1",{id:"discovering-the-domain"},"Discovering the Domain"),(0,a.kt)("p",null,"There are numerous techniques to discover a domain.\nEvent Storming is a particularly interesting one.\nIt is a workshop format for quickly exploring business domains, engaging both Domain Experts and Software Developers."),(0,a.kt)("p",null,(0,a.kt)("a",{parentName:"p",href:"https://eventmodeling.org/posts/what-is-event-modeling/"},"Event Modeling")," adopts Event Storming sticky notes. The final piece was the UI/UX aspects to complete what more resembles a movie story board (white board - or digital white board).\nWhile Event Storming focuses in discovering the problem space, ",(0,a.kt)("strong",{parentName:"p"},"Event Modeling creates a blueprint for a solution"),"."),(0,a.kt)("ul",null,(0,a.kt)("li",{parentName:"ul"},"It is a method of describing systems using an example of how information has changed within them over time."),(0,a.kt)("li",{parentName:"ul"},"It is a scenario-based and UX-driven approach to defining requirements.")),(0,a.kt)("p",null,"On a higher level of abstraction, any information system is responsible for handling the intent (",(0,a.kt)("inlineCode",{parentName:"p"},"Command"),") and, based\non the current ",(0,a.kt)("inlineCode",{parentName:"p"},"State,")," produce new facts (",(0,a.kt)("inlineCode",{parentName:"p"},"Events"),"). The system\u2019s ",(0,a.kt)("strong",{parentName:"p"},"new")," ",(0,a.kt)("inlineCode",{parentName:"p"},"State")," is then evolved out of these ",(0,a.kt)("inlineCode",{parentName:"p"},"Events.")),(0,a.kt)("p",null,(0,a.kt)("img",{alt:"event modeling",src:n(399).Z,width:"5391",height:"3776"})),(0,a.kt)("ul",null,(0,a.kt)("li",{parentName:"ul"},"User submits the form on the page by clicking on the button"),(0,a.kt)("li",{parentName:"ul"},"The intent to change the system is explicitly captured/modeled as a Command/",(0,a.kt)("inlineCode",{parentName:"li"},"C"),"."),(0,a.kt)("li",{parentName:"ul"},"Command is handled by the decider component, which State/",(0,a.kt)("inlineCode",{parentName:"li"},"S")," (yellow) is represented in the swim-lane at the bottom."),(0,a.kt)("li",{parentName:"ul"},"Based on the current State and the Command it received, the Decider will make new decisions/Events/",(0,a.kt)("inlineCode",{parentName:"li"},"E")),(0,a.kt)("li",{parentName:"ul"},"New Events will update/evolve the State of the Decider (yellow), and the View (green)"),(0,a.kt)("li",{parentName:"ul"},"The View state is constructed per need to serve specific pages with data. Every page can have its View.")),(0,a.kt)("p",null,"'FModel' is offering implementation of this blueprint in a very general way.\nThe implementation is parametrized with C/",(0,a.kt)("inlineCode",{parentName:"p"},"Command"),", E/",(0,a.kt)("inlineCode",{parentName:"p"},"Event"),", and S/",(0,a.kt)("inlineCode",{parentName:"p"},"State")," parameters. "),(0,a.kt)("p",null,"The responsibility of the business is to specialize in their case by specifying concrete Commands, Events, and State.\nFor example, ",(0,a.kt)("inlineCode",{parentName:"p"},"Commands"),"=CreateOrder, MarkOrderAsPrepared; ",(0,a.kt)("inlineCode",{parentName:"p"},"Events"),"=OrderCreated, OrderPrepared, ",(0,a.kt)("inlineCode",{parentName:"p"},"State"),"=Order(with list\nof Items)."),(0,a.kt)("p",null,(0,a.kt)("img",{alt:"restaurant model",src:n(182).Z,width:"2787",height:"1434"})),(0,a.kt)("admonition",{type:"note"},(0,a.kt)("p",{parentName:"admonition"},(0,a.kt)("em",{parentName:"p"},"Customers use the web application to place food orders at local restaurants. Application coordinates a\nrestaurant/kitchen order preparation."))),(0,a.kt)("p",null,"Let's learn how to ",(0,a.kt)("a",{parentName:"p",href:"/fmodel/docs/domain/structuring-the-data"},"structure the data/information")," and how to\neffectively ",(0,a.kt)("a",{parentName:"p",href:"/fmodel/docs/domain/modeling-the-behaviour"},"model the behaviour")," in Kotlin, by example!"))}c.isMDXComponent=!0},399:(e,t,n)=>{n.d(t,{Z:()=>r});const r=n.p+"assets/images/event-modeling-f50f091a2cb9416fdd017660651d5de8.png"},182:(e,t,n)=>{n.d(t,{Z:()=>r});const r=n.p+"assets/images/restaurant-model-bddc3c24c98956e7e56820d50a92ee77.jpg"}}]);
"use strict";(self.webpackChunkfmodel=self.webpackChunkfmodel||[]).push([[8215],{3905:(e,t,n)=>{n.d(t,{Zo:()=>c,kt:()=>f});var r=n(7294);function a(e,t,n){return t in e?Object.defineProperty(e,t,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[t]=n,e}function o(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var r=Object.getOwnPropertySymbols(e);t&&(r=r.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,r)}return n}function i(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?o(Object(n),!0).forEach((function(t){a(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):o(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}function l(e,t){if(null==e)return{};var n,r,a=function(e,t){if(null==e)return{};var n,r,a={},o=Object.keys(e);for(r=0;r<o.length;r++)n=o[r],t.indexOf(n)>=0||(a[n]=e[n]);return a}(e,t);if(Object.getOwnPropertySymbols){var o=Object.getOwnPropertySymbols(e);for(r=0;r<o.length;r++)n=o[r],t.indexOf(n)>=0||Object.prototype.propertyIsEnumerable.call(e,n)&&(a[n]=e[n])}return a}var p=r.createContext({}),s=function(e){var t=r.useContext(p),n=t;return e&&(n="function"==typeof e?e(t):i(i({},t),e)),n},c=function(e){var t=s(e.components);return r.createElement(p.Provider,{value:t},e.children)},m="mdxType",d={inlineCode:"code",wrapper:function(e){var t=e.children;return r.createElement(r.Fragment,{},t)}},u=r.forwardRef((function(e,t){var n=e.components,a=e.mdxType,o=e.originalType,p=e.parentName,c=l(e,["components","mdxType","originalType","parentName"]),m=s(n),u=a,f=m["".concat(p,".").concat(u)]||m[u]||d[u]||o;return n?r.createElement(f,i(i({ref:t},c),{},{components:n})):r.createElement(f,i({ref:t},c))}));function f(e,t){var n=arguments,a=t&&t.mdxType;if("string"==typeof e||a){var o=n.length,i=new Array(o);i[0]=u;var l={};for(var p in t)hasOwnProperty.call(t,p)&&(l[p]=t[p]);l.originalType=e,l[m]="string"==typeof e?e:a,i[1]=l;for(var s=2;s<o;s++)i[s]=n[s];return r.createElement.apply(null,i)}return r.createElement.apply(null,n)}u.displayName="MDXCreateElement"},7215:(e,t,n)=>{n.r(t),n.d(t,{assets:()=>p,contentTitle:()=>i,default:()=>d,frontMatter:()=>o,metadata:()=>l,toc:()=>s});var r=n(7462),a=(n(7294),n(3905));const o={sidebar_position:3},i="Examples",l={unversionedId:"application/example",id:"application/example",title:"Examples",description:"All example/demo application are publicly available on Github.",source:"@site/docs/application/example.md",sourceDirName:"application",slug:"/application/example",permalink:"/fmodel/docs/application/example",draft:!1,tags:[],version:"current",sidebarPosition:3,frontMatter:{sidebar_position:3},sidebar:"tutorialSidebar",previous:{title:"Application",permalink:"/fmodel/docs/application/"}},p={},s=[{value:"Spring",id:"spring",level:2},{value:"Ktor",id:"ktor",level:2}],c={toc:s},m="wrapper";function d(e){let{components:t,...o}=e;return(0,a.kt)(m,(0,r.Z)({},c,o,{components:t,mdxType:"MDXLayout"}),(0,a.kt)("h1",{id:"examples"},"Examples"),(0,a.kt)("p",null,"All example/demo application are publicly available on Github.\nThey are all modeling the same imaginary ",(0,a.kt)("inlineCode",{parentName:"p"},"restaurant and order management")," information system by using different technology stacks to demonstrate flexibility of the Fmodel libraries."),(0,a.kt)("p",null,"The domain model is explicitly modeling ",(0,a.kt)("inlineCode",{parentName:"p"},"events")," and ",(0,a.kt)("inlineCode",{parentName:"p"},"state"),", and this is enabling you to: "),(0,a.kt)("ul",null,(0,a.kt)("li",{parentName:"ul"},"compose the ",(0,a.kt)("inlineCode",{parentName:"li"},"event-sourced")," system"),(0,a.kt)("li",{parentName:"ul"},"compose the ",(0,a.kt)("inlineCode",{parentName:"li"},"state-stored")," system")),(0,a.kt)("p",null,(0,a.kt)("img",{alt:"event-sourced vs state-stored",src:n(3983).Z,width:"3981",height:"1668"})),(0,a.kt)("p",null,"The source code snippets in this reference guide are taken from these applications."),(0,a.kt)("h2",{id:"spring"},"Spring"),(0,a.kt)("ul",null,(0,a.kt)("li",{parentName:"ul"},(0,a.kt)("inlineCode",{parentName:"li"},"event-sourced"),": ",(0,a.kt)("a",{parentName:"li",href:"https://github.com/fraktalio/fmodel-spring-demo"},"https://github.com/fraktalio/fmodel-spring-demo")," / #EventSourcing, #SpringBoot, #Reactor, #RSocket, #R2DBC, #SQL, #Testcontainers"),(0,a.kt)("li",{parentName:"ul"},(0,a.kt)("inlineCode",{parentName:"li"},"state-stored"),": ",(0,a.kt)("a",{parentName:"li",href:"https://github.com/fraktalio/fmodel-spring-state-stored-demo"},"https://github.com/fraktalio/fmodel-spring-state-stored-demo")," / #SpringBoot, #Reactor, #RSocket, #R2DBC, #SQL, #Testcontainers")),(0,a.kt)("h2",{id:"ktor"},"Ktor"),(0,a.kt)("ul",null,(0,a.kt)("li",{parentName:"ul"},(0,a.kt)("inlineCode",{parentName:"li"},"event-sourced"),": ",(0,a.kt)("a",{parentName:"li",href:"https://github.com/fraktalio/fmodel-ktor-demo"},"https://github.com/fraktalio/fmodel-ktor-demo")," / #EventSourcing, #Ktor, #R2DBC, #SQL"),(0,a.kt)("li",{parentName:"ul"},(0,a.kt)("inlineCode",{parentName:"li"},"state-stored"),": TODO")),(0,a.kt)("p",null,"more to come..."))}d.isMDXComponent=!0},3983:(e,t,n)=>{n.d(t,{Z:()=>r});const r=n.p+"assets/images/es-ss-diagram-11d21cb283f0018a85873dd94f5d0c65.svg"}}]);
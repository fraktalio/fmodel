"use strict";(self.webpackChunkfmodel=self.webpackChunkfmodel||[]).push([[7307],{3905:(e,a,t)=>{t.d(a,{Zo:()=>d,kt:()=>f});var n=t(7294);function r(e,a,t){return a in e?Object.defineProperty(e,a,{value:t,enumerable:!0,configurable:!0,writable:!0}):e[a]=t,e}function l(e,a){var t=Object.keys(e);if(Object.getOwnPropertySymbols){var n=Object.getOwnPropertySymbols(e);a&&(n=n.filter((function(a){return Object.getOwnPropertyDescriptor(e,a).enumerable}))),t.push.apply(t,n)}return t}function o(e){for(var a=1;a<arguments.length;a++){var t=null!=arguments[a]?arguments[a]:{};a%2?l(Object(t),!0).forEach((function(a){r(e,a,t[a])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(t)):l(Object(t)).forEach((function(a){Object.defineProperty(e,a,Object.getOwnPropertyDescriptor(t,a))}))}return e}function i(e,a){if(null==e)return{};var t,n,r=function(e,a){if(null==e)return{};var t,n,r={},l=Object.keys(e);for(n=0;n<l.length;n++)t=l[n],a.indexOf(t)>=0||(r[t]=e[t]);return r}(e,a);if(Object.getOwnPropertySymbols){var l=Object.getOwnPropertySymbols(e);for(n=0;n<l.length;n++)t=l[n],a.indexOf(t)>=0||Object.prototype.propertyIsEnumerable.call(e,t)&&(r[t]=e[t])}return r}var c=n.createContext({}),m=function(e){var a=n.useContext(c),t=a;return e&&(t="function"==typeof e?e(a):o(o({},a),e)),t},d=function(e){var a=m(e.components);return n.createElement(c.Provider,{value:a},e.children)},p="mdxType",s={inlineCode:"code",wrapper:function(e){var a=e.children;return n.createElement(n.Fragment,{},a)}},u=n.forwardRef((function(e,a){var t=e.components,r=e.mdxType,l=e.originalType,c=e.parentName,d=i(e,["components","mdxType","originalType","parentName"]),p=m(t),u=r,f=p["".concat(c,".").concat(u)]||p[u]||s[u]||l;return t?n.createElement(f,o(o({ref:a},d),{},{components:t})):n.createElement(f,o({ref:a},d))}));function f(e,a){var t=arguments,r=a&&a.mdxType;if("string"==typeof e||r){var l=t.length,o=new Array(l);o[0]=u;var i={};for(var c in a)hasOwnProperty.call(a,c)&&(i[c]=a[c]);i.originalType=e,i[p]="string"==typeof e?e:r,o[1]=i;for(var m=2;m<l;m++)o[m]=t[m];return n.createElement.apply(null,o)}return n.createElement.apply(null,t)}u.displayName="MDXCreateElement"},9053:(e,a,t)=>{t.r(a),t.d(a,{contentTitle:()=>c,default:()=>u,frontMatter:()=>i,metadata:()=>m,toc:()=>d});var n=t(7462),r=(t(7294),t(3905)),l=t(4866),o=t(5162);const i={title:"Release Notes"},c="Release Notes",m={type:"mdx",permalink:"/fmodel/release-notes",source:"@site/src/pages/release-notes.md",title:"Release Notes",description:"3.4.0",frontMatter:{title:"Release Notes"}},d=[{value:"3.4.0",id:"340",level:2},{value:"What&#39;s changed",id:"whats-changed",level:3},{value:"Include the dependencies",id:"include-the-dependencies",level:3},{value:"3.3.0",id:"330",level:2},{value:"What&#39;s changed",id:"whats-changed-1",level:3},{value:"A convenient DSL (builder) for the domain components",id:"a-convenient-dsl-builder-for-the-domain-components",level:4},{value:"Minimizing the API",id:"minimizing-the-api",level:4},{value:"Include the dependencies",id:"include-the-dependencies-1",level:3},{value:"3.2.0",id:"320",level:2},{value:"What&#39;s changed",id:"whats-changed-2",level:3},{value:"Optimistic Locking",id:"optimistic-locking",level:4},{value:"Include the dependencies",id:"include-the-dependencies-2",level:3},{value:"3.1.0",id:"310",level:2},{value:"What&#39;s changed",id:"whats-changed-3",level:3},{value:"Experimental Actors (JVM only)",id:"experimental-actors-jvm-only",level:4},{value:"Include the dependencies",id:"include-the-dependencies-3",level:3},{value:"3.0.0",id:"300",level:2},{value:"What&#39;s changed",id:"whats-changed-4",level:3},{value:"Tests example",id:"tests-example",level:4},{value:"Include the dependencies",id:"include-the-dependencies-4",level:3}],p={toc:d},s="wrapper";function u(e){let{components:a,...t}=e;return(0,r.kt)(s,(0,n.Z)({},p,t,{components:a,mdxType:"MDXLayout"}),(0,r.kt)("h1",{id:"release-notes"},"Release Notes"),(0,r.kt)("h2",{id:"340"},"3.4.0"),(0,r.kt)("p",null,"Artifacts are available on Maven Central"),(0,r.kt)("ul",null,(0,r.kt)("li",{parentName:"ul"},(0,r.kt)("a",{parentName:"li",href:"https://search.maven.org/artifact/com.fraktalio.fmodel/domain/3.4.0/jar"},"https://search.maven.org/artifact/com.fraktalio.fmodel/domain/3.4.0/jar")),(0,r.kt)("li",{parentName:"ul"},(0,r.kt)("a",{parentName:"li",href:"https://search.maven.org/artifact/com.fraktalio.fmodel/application-vanilla/3.4.0/jar"},"https://search.maven.org/artifact/com.fraktalio.fmodel/application-vanilla/3.4.0/jar")),(0,r.kt)("li",{parentName:"ul"},(0,r.kt)("a",{parentName:"li",href:"https://search.maven.org/artifact/com.fraktalio.fmodel/application-arrow/3.4.0/jar"},"https://search.maven.org/artifact/com.fraktalio.fmodel/application-arrow/3.4.0/jar"))),(0,r.kt)("h3",{id:"whats-changed"},"What's changed"),(0,r.kt)("p",null,"New native targets are available:\nlinuxX64(), mingwX64(), macosX64(), macosArm64(), tvos(), tvosSimulatorArm64(), watchosArm32(), watchosArm64(),\nwatchosX86(), watchosX64(), watchosSimulatorArm64(), iosX64(), iosArm64(), iosArm32(), iosSimulatorArm64()"),(0,r.kt)("p",null,(0,r.kt)("strong",{parentName:"p"},"Full Changelog"),": ",(0,r.kt)("a",{parentName:"p",href:"https://github.com/fraktalio/fmodel/compare/v3.3.0...v3.4.0"},"https://github.com/fraktalio/fmodel/compare/v3.3.0...v3.4.0")),(0,r.kt)("h3",{id:"include-the-dependencies"},"Include the dependencies"),(0,r.kt)(l.Z,{groupId:"build",queryString:"build-type",mdxType:"Tabs"},(0,r.kt)(o.Z,{value:"gradleKotlin",label:"Gradle (Kotlin)",mdxType:"TabItem"},(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-kotlin"},'dependencies {\n  implementation("com.fraktalio.fmodel:domain:3.4.0")\n  implementation("com.fraktalio.fmodel:application-vanilla:3.4.0")\n  implementation("com.fraktalio.fmodel:application-arrow:3.4.0")\n}\n'))),(0,r.kt)(o.Z,{value:"gradleGroovy",label:"Gradle (Groovy)",mdxType:"TabItem"},(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-groovy"},"dependencies {\n  implementation 'com.fraktalio.fmodel:domain:3.4.0'\n  implementation 'com.fraktalio.fmodel:application-vanilla:3.4.0'\n  implementation 'com.fraktalio.fmodel:application-arrow:3.4.0'\n}\n"))),(0,r.kt)(o.Z,{value:"maven",label:"Maven",mdxType:"TabItem"},(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-xml"},"\n<dependency>\n    <groupId>com.fraktalio.fmodel</groupId>\n    <artifactId>domain</artifactId>\n    <version>3.4.0</version>\n</dependency>\n\n<dependency>\n    <groupId>com.fraktalio.fmodel</groupId>\n    <artifactId>application-vanilla</artifactId>\n    <version>3.4.0</version>\n</dependency>\n\n<dependency>\n    <groupId>com.fraktalio.fmodel</groupId>\n    <artifactId>application-arrow</artifactId>\n    <version>3.4.0</version>\n</dependency>\n")))),(0,r.kt)("h2",{id:"330"},"3.3.0"),(0,r.kt)("p",null,"Artifacts are available on Maven Central"),(0,r.kt)("ul",null,(0,r.kt)("li",{parentName:"ul"},(0,r.kt)("a",{parentName:"li",href:"https://search.maven.org/artifact/com.fraktalio.fmodel/domain/3.3.0/jar"},"https://search.maven.org/artifact/com.fraktalio.fmodel/domain/3.3.0/jar")),(0,r.kt)("li",{parentName:"ul"},(0,r.kt)("a",{parentName:"li",href:"https://search.maven.org/artifact/com.fraktalio.fmodel/application-vanilla/3.3.0/jar"},"https://search.maven.org/artifact/com.fraktalio.fmodel/application-vanilla/3.3.0/jar")),(0,r.kt)("li",{parentName:"ul"},(0,r.kt)("a",{parentName:"li",href:"https://search.maven.org/artifact/com.fraktalio.fmodel/application-arrow/3.3.0/jar"},"https://search.maven.org/artifact/com.fraktalio.fmodel/application-arrow/3.3.0/jar"))),(0,r.kt)("h3",{id:"whats-changed-1"},"What's changed"),(0,r.kt)("h4",{id:"a-convenient-dsl-builder-for-the-domain-components"},"A convenient DSL (builder) for the domain components"),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-kotlin"},'fun evenNumberDecider(): Decider<EvenNumberCommand?, EvenNumberState, EvenNumberEvent?> =\n    decider {\n        initialState {\n            evenNumberState {\n                descriptionString { "Initial state" }\n                valueInt { 0 }\n            }\n        }\n        decide { c, s ->\n            when (c) {\n                is AddEvenNumber -> flowOf(\n                    evenNumberAdded {\n                        description { c.description }\n                        value { s.value + c.value }\n                    }\n                )\n\n                is SubtractEvenNumber -> flowOf(\n                    evenNumberSubtracted {\n                        description { c.description }\n                        value { s.value - c.value }\n                    }\n                )\n\n                null -> emptyFlow()\n            }\n        }\n        evolve { s, e ->\n            when (e) {\n                is EvenNumberAdded ->\n                    evenNumberState {\n                        description { s.description + e.description }\n                        value { e.value }\n                    }\n\n                is EvenNumberSubtracted ->\n                    evenNumberState {\n                        description { s.description - e.description }\n                        value { e.value }\n                    }\n\n                null -> s\n            }\n        }\n    }\n')),(0,r.kt)("h4",{id:"minimizing-the-api"},"Minimizing the API"),(0,r.kt)("ul",null,(0,r.kt)("li",{parentName:"ul"},(0,r.kt)("inlineCode",{parentName:"li"},"_Decider<C, Si, So, Ei, Eo>")," is internal now"),(0,r.kt)("li",{parentName:"ul"},(0,r.kt)("inlineCode",{parentName:"li"},"_View<Si, So, E>")," is internal now")),(0,r.kt)("p",null,"There was no true usage of this API, so we have decided to make it internal, in favor of ",(0,r.kt)("inlineCode",{parentName:"p"},"Decider<C, S, E>"),"\nand ",(0,r.kt)("inlineCode",{parentName:"p"},"View<S, E>"),".\nPreviously, ",(0,r.kt)("inlineCode",{parentName:"p"},"Decider")," was just a type alias of ",(0,r.kt)("inlineCode",{parentName:"p"},"_Decider"),", but these are different types actually, and we want to\npromote that."),(0,r.kt)("p",null,"We hope to minimize the complexity of the API, and make the right thing to do the easy thing to do."),(0,r.kt)("p",null,(0,r.kt)("strong",{parentName:"p"},"Full Changelog"),": ",(0,r.kt)("a",{parentName:"p",href:"https://github.com/fraktalio/fmodel/compare/v3.2.0...v3.3.0"},"https://github.com/fraktalio/fmodel/compare/v3.2.0...v3.3.0")),(0,r.kt)("h3",{id:"include-the-dependencies-1"},"Include the dependencies"),(0,r.kt)(l.Z,{groupId:"build",queryString:"build-type",mdxType:"Tabs"},(0,r.kt)(o.Z,{value:"gradleKotlin",label:"Gradle (Kotlin)",mdxType:"TabItem"},(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-kotlin"},'dependencies {\n  implementation("com.fraktalio.fmodel:domain:3.3.0")\n  implementation("com.fraktalio.fmodel:application-vanilla:3.3.0")\n  implementation("com.fraktalio.fmodel:application-arrow:3.3.0")\n}\n'))),(0,r.kt)(o.Z,{value:"gradleGroovy",label:"Gradle (Groovy)",mdxType:"TabItem"},(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-groovy"},"dependencies {\n  implementation 'com.fraktalio.fmodel:domain:3.3.0'\n  implementation 'com.fraktalio.fmodel:application-vanilla:3.3.0'\n  implementation 'com.fraktalio.fmodel:application-arrow:3.3.0'\n}\n"))),(0,r.kt)(o.Z,{value:"maven",label:"Maven",mdxType:"TabItem"},(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-xml"},"\n<dependency>\n    <groupId>com.fraktalio.fmodel</groupId>\n    <artifactId>domain</artifactId>\n    <version>3.3.0</version>\n</dependency>\n\n<dependency>\n    <groupId>com.fraktalio.fmodel</groupId>\n    <artifactId>application-vanilla</artifactId>\n    <version>3.3.0</version>\n</dependency>\n\n<dependency>\n    <groupId>com.fraktalio.fmodel</groupId>\n    <artifactId>application-arrow</artifactId>\n    <version>3.3.0</version>\n</dependency>\n")))),(0,r.kt)("h2",{id:"320"},"3.2.0"),(0,r.kt)("p",null,"Artifacts are available on Maven Central"),(0,r.kt)("ul",null,(0,r.kt)("li",{parentName:"ul"},(0,r.kt)("a",{parentName:"li",href:"https://search.maven.org/artifact/com.fraktalio.fmodel/domain/3.2.0/jar"},"https://search.maven.org/artifact/com.fraktalio.fmodel/domain/3.2.0/jar")),(0,r.kt)("li",{parentName:"ul"},(0,r.kt)("a",{parentName:"li",href:"https://search.maven.org/artifact/com.fraktalio.fmodel/application-vanilla/3.2.0/jar"},"https://search.maven.org/artifact/com.fraktalio.fmodel/application-vanilla/3.2.0/jar")),(0,r.kt)("li",{parentName:"ul"},(0,r.kt)("a",{parentName:"li",href:"https://search.maven.org/artifact/com.fraktalio.fmodel/application-arrow/3.2.0/jar"},"https://search.maven.org/artifact/com.fraktalio.fmodel/application-arrow/3.2.0/jar"))),(0,r.kt)("h3",{id:"whats-changed-2"},"What's changed"),(0,r.kt)("h4",{id:"optimistic-locking"},"Optimistic Locking"),(0,r.kt)("p",null,"Optimistic locking, also referred to as optimistic concurrency control, allows multiple concurrent users to attempt to\nupdate the same resource."),(0,r.kt)("p",null,"There are two common ways to implement optimistic locking: version number and timestamp. The version number is generally\nconsidered to be a better option because the server clock can be inaccurate over time, but we do not want to restrict it\nto only one option, so we have the generic parameter V acting as a Version."),(0,r.kt)("p",null,"The optimistic locking mechanism is not leaking into the core Domain layer."),(0,r.kt)("p",null,"Application modules provide more interfaces and extensions, giving you additional options to compose your unique Domain\ncomponents with Optimistic Locking formally in place, without changing the Domain components whatsoever."),(0,r.kt)("p",null,(0,r.kt)("strong",{parentName:"p"},"example (state-stored aggregate / traditional):")),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-kotlin"},"  stateStoredLockingAggregate(\n      decider = myDecider,\n      stateRepository = myLockingRepository\n  ).handleOptimistically(myCommand)\n")),(0,r.kt)("p",null,"where ",(0,r.kt)("inlineCode",{parentName:"p"},"myDecider")," is of type ",(0,r.kt)("inlineCode",{parentName:"p"},"IDecider<C, S, E>"),", ",(0,r.kt)("inlineCode",{parentName:"p"},"myLockingRepository")," is of type ",(0,r.kt)("inlineCode",{parentName:"p"},"StateLockingRepository<C, S, V>"),"\nand ",(0,r.kt)("inlineCode",{parentName:"p"},"myCommand")," is of type ",(0,r.kt)("inlineCode",{parentName:"p"},"C")),(0,r.kt)("p",null,(0,r.kt)("strong",{parentName:"p"},"example (event-sourced aggregate / event-driven):")),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-kotlin"},"\n  eventSourcingLockingAggregate(\n      decider = myDecider,\n      stateRepository = myLockingRepository\n  ).handleOptimistically(myCommand)\n")),(0,r.kt)("p",null,"where ",(0,r.kt)("inlineCode",{parentName:"p"},"myDecider")," is of type ",(0,r.kt)("inlineCode",{parentName:"p"},"IDecider<C, S, E>"),", ",(0,r.kt)("inlineCode",{parentName:"p"},"myLockingRepository")," is of type ",(0,r.kt)("inlineCode",{parentName:"p"},"EventLockingRepository<C, E, V>"),"\nand ",(0,r.kt)("inlineCode",{parentName:"p"},"myCommand")," is of type ",(0,r.kt)("inlineCode",{parentName:"p"},"C")),(0,r.kt)("p",null,(0,r.kt)("strong",{parentName:"p"},"Full Changelog"),": ",(0,r.kt)("a",{parentName:"p",href:"https://github.com/fraktalio/fmodel/compare/v3.1.0...v3.2.0"},"https://github.com/fraktalio/fmodel/compare/v3.1.0...v3.2.0")),(0,r.kt)("h3",{id:"include-the-dependencies-2"},"Include the dependencies"),(0,r.kt)(l.Z,{groupId:"build",queryString:"build-type",mdxType:"Tabs"},(0,r.kt)(o.Z,{value:"gradleKotlin",label:"Gradle (Kotlin)",mdxType:"TabItem"},(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-kotlin"},'dependencies {\n  implementation("com.fraktalio.fmodel:domain:3.2.0")\n  implementation("com.fraktalio.fmodel:application-vanilla:3.2.0")\n  implementation("com.fraktalio.fmodel:application-arrow:3.2.0")\n}\n'))),(0,r.kt)(o.Z,{value:"gradleGroovy",label:"Gradle (Groovy)",mdxType:"TabItem"},(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-groovy"},"dependencies {\n  implementation 'com.fraktalio.fmodel:domain:3.2.0'\n  implementation 'com.fraktalio.fmodel:application-vanilla:3.2.0'\n  implementation 'com.fraktalio.fmodel:application-arrow:3.2.0'\n}\n"))),(0,r.kt)(o.Z,{value:"maven",label:"Maven",mdxType:"TabItem"},(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-xml"},"\n<dependency>\n    <groupId>com.fraktalio.fmodel</groupId>\n    <artifactId>domain</artifactId>\n    <version>3.2.0</version>\n</dependency>\n\n<dependency>\n    <groupId>com.fraktalio.fmodel</groupId>\n    <artifactId>application-vanilla</artifactId>\n    <version>3.2.0</version>\n</dependency>\n\n<dependency>\n    <groupId>com.fraktalio.fmodel</groupId>\n    <artifactId>application-arrow</artifactId>\n    <version>3.2.0</version>\n</dependency>\n")))),(0,r.kt)("h2",{id:"310"},"3.1.0"),(0,r.kt)("p",null,"Artifacts are available on Maven Central"),(0,r.kt)("ul",null,(0,r.kt)("li",{parentName:"ul"},(0,r.kt)("a",{parentName:"li",href:"https://search.maven.org/artifact/com.fraktalio.fmodel/domain/3.1.0/jar"},"https://search.maven.org/artifact/com.fraktalio.fmodel/domain/3.1.0/jar")),(0,r.kt)("li",{parentName:"ul"},(0,r.kt)("a",{parentName:"li",href:"https://search.maven.org/artifact/com.fraktalio.fmodel/application-vanilla/3.1.0/jar"},"https://search.maven.org/artifact/com.fraktalio.fmodel/application-vanilla/3.1.0/jar")),(0,r.kt)("li",{parentName:"ul"},(0,r.kt)("a",{parentName:"li",href:"https://search.maven.org/artifact/com.fraktalio.fmodel/application-arrow/3.1.0/jar"},"https://search.maven.org/artifact/com.fraktalio.fmodel/application-arrow/3.1.0/jar"))),(0,r.kt)("h3",{id:"whats-changed-3"},"What's changed"),(0,r.kt)("h4",{id:"experimental-actors-jvm-only"},"Experimental Actors (JVM only)"),(0,r.kt)("ul",null,(0,r.kt)("li",{parentName:"ul"},"Kotlin Actors (experimental) - concurrently handling messages by ",(0,r.kt)("inlineCode",{parentName:"li"},"idugalic"),"\nin ",(0,r.kt)("a",{parentName:"li",href:"https://github.com/fraktalio/fmodel/pull/70"},"https://github.com/fraktalio/fmodel/pull/70"))),(0,r.kt)("p",null,(0,r.kt)("img",{parentName:"p",src:"https://raw.githubusercontent.com/fraktalio/fmodel/main/.assets/kotlin-actors.png",alt:"kotlin actors"})),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-kotlin"},"@ExperimentalContracts\n@FlowPreview\nfun <C, S, E> EventSourcingAggregate<C, S, E>.handleConcurrently(\n    commands: Flow<C>,\n    numberOfActors: Int = 100,\n    actorsCapacity: Int = Channel.BUFFERED,\n    actorsStart: CoroutineStart = CoroutineStart.LAZY,\n    actorsContext: CoroutineContext = EmptyCoroutineContext,\n    partitionKey: (C) -> Int\n): Flow<E> = channelFlow {\n    val actors: List<SendChannel<C>> = (1..numberOfActors).map {\n        commandActor(channel, actorsCapacity, actorsStart, actorsContext) { handle(it) }\n\n    }\n    commands\n        .onCompletion {\n            actors.forEach {\n                it.close()\n            }\n        }\n        .collect {\n            val partition = partitionKey(it).absoluteValue % numberOfActors.coerceAtLeast(1)\n            actors[partition].send(it)\n        }\n}\n\n")),(0,r.kt)("p",null,(0,r.kt)("strong",{parentName:"p"},"Full Changelog"),": ",(0,r.kt)("a",{parentName:"p",href:"https://github.com/fraktalio/fmodel/compare/v3.0.0...v3.1.0"},"https://github.com/fraktalio/fmodel/compare/v3.0.0...v3.1.0")),(0,r.kt)("h3",{id:"include-the-dependencies-3"},"Include the dependencies"),(0,r.kt)(l.Z,{groupId:"build",queryString:"build-type",mdxType:"Tabs"},(0,r.kt)(o.Z,{value:"gradleKotlin",label:"Gradle (Kotlin)",mdxType:"TabItem"},(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-kotlin"},'dependencies {\n  implementation("com.fraktalio.fmodel:domain:3.1.0")\n  implementation("com.fraktalio.fmodel:application-vanilla:3.1.0")\n  implementation("com.fraktalio.fmodel:application-arrow:3.1.0")\n}\n'))),(0,r.kt)(o.Z,{value:"gradleGroovy",label:"Gradle (Groovy)",mdxType:"TabItem"},(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-groovy"},"dependencies {\n  implementation 'com.fraktalio.fmodel:domain:3.1.0'\n  implementation 'com.fraktalio.fmodel:application-vanilla:3.1.0'\n  implementation 'com.fraktalio.fmodel:application-arrow:3.1.0'\n}\n"))),(0,r.kt)(o.Z,{value:"maven",label:"Maven",mdxType:"TabItem"},(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-xml"},"\n<dependency>\n    <groupId>com.fraktalio.fmodel</groupId>\n    <artifactId>domain</artifactId>\n    <version>3.1.0</version>\n</dependency>\n\n<dependency>\n    <groupId>com.fraktalio.fmodel</groupId>\n    <artifactId>application-vanilla</artifactId>\n    <version>3.1.0</version>\n</dependency>\n\n<dependency>\n    <groupId>com.fraktalio.fmodel</groupId>\n    <artifactId>application-arrow</artifactId>\n    <version>3.1.0</version>\n</dependency>\n")))),(0,r.kt)("h2",{id:"300"},"3.0.0"),(0,r.kt)("p",null,"Artifacts are available on Maven Central"),(0,r.kt)("ul",null,(0,r.kt)("li",{parentName:"ul"},(0,r.kt)("a",{parentName:"li",href:"https://search.maven.org/artifact/com.fraktalio.fmodel/domain/3.0.0/jar"},"https://search.maven.org/artifact/com.fraktalio.fmodel/domain/3.0.0/jar")),(0,r.kt)("li",{parentName:"ul"},(0,r.kt)("a",{parentName:"li",href:"https://search.maven.org/artifact/com.fraktalio.fmodel/application-vanilla/3.0.0/jar"},"https://search.maven.org/artifact/com.fraktalio.fmodel/application-vanilla/3.0.0/jar")),(0,r.kt)("li",{parentName:"ul"},(0,r.kt)("a",{parentName:"li",href:"https://search.maven.org/artifact/com.fraktalio.fmodel/application-arrow/3.0.0/jar"},"https://search.maven.org/artifact/com.fraktalio.fmodel/application-arrow/3.0.0/jar"))),(0,r.kt)("h3",{id:"whats-changed-4"},"What's changed"),(0,r.kt)("ul",null,(0,r.kt)("li",{parentName:"ul"},"A ",(0,r.kt)("a",{parentName:"li",href:"https://kotlinlang.org/docs/multiplatform.html"},"multiplatform support (jvm, js, native)")," included"),(0,r.kt)("li",{parentName:"ul"},"Switched from Spek to Kotest test framework"),(0,r.kt)("li",{parentName:"ul"},"Switched from Maven to Gradle")),(0,r.kt)("h4",{id:"tests-example"},"Tests example"),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-kotlin"},'class DeciderTest : FunSpec({\n    val evenDecider = evenNumberDecider()\n    val oddDecider = oddNumberDecider()\n\n    test("Event-sourced Decider - add even number") {\n        with(evenDecider) {\n            givenEvents(emptyList()) {\n                whenCommand(AddEvenNumber(Description("2"), NumberValue(2)))\n            } thenEvents listOf(EvenNumberAdded(Description("2"), NumberValue(2)))\n        }\n    }\n\n    test("Event-sourced Decider - given previous state, add even number") {\n        with(evenDecider) {\n            givenEvents(listOf(EvenNumberAdded(Description("2"), NumberValue(2)))) {\n                whenCommand(AddEvenNumber(Description("4"), NumberValue(4)))\n            } thenEvents listOf(EvenNumberAdded(Description("4"), NumberValue(4)))\n        }\n    }\n})\n')),(0,r.kt)("p",null,(0,r.kt)("strong",{parentName:"p"},"Full Changelog"),": ",(0,r.kt)("a",{parentName:"p",href:"https://github.com/fraktalio/fmodel/compare/v3.0.0...v3.1.0"},"https://github.com/fraktalio/fmodel/compare/v3.0.0...v3.1.0")),(0,r.kt)("h3",{id:"include-the-dependencies-4"},"Include the dependencies"),(0,r.kt)(l.Z,{groupId:"build",queryString:"build-type",mdxType:"Tabs"},(0,r.kt)(o.Z,{value:"gradleKotlin",label:"Gradle (Kotlin)",mdxType:"TabItem"},(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-kotlin"},'dependencies {\n  implementation("com.fraktalio.fmodel:domain:3.0.0")\n  implementation("com.fraktalio.fmodel:application-vanilla:3.0.0")\n  implementation("com.fraktalio.fmodel:application-arrow:3.0.0")\n}\n'))),(0,r.kt)(o.Z,{value:"gradleGroovy",label:"Gradle (Groovy)",mdxType:"TabItem"},(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-groovy"},"dependencies {\n  implementation 'com.fraktalio.fmodel:domain:3.0.0'\n  implementation 'com.fraktalio.fmodel:application-vanilla:3.0.0'\n  implementation 'com.fraktalio.fmodel:application-arrow:3.0.0'\n}\n"))),(0,r.kt)(o.Z,{value:"maven",label:"Maven",mdxType:"TabItem"},(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-xml"},"\n<dependency>\n    <groupId>com.fraktalio.fmodel</groupId>\n    <artifactId>domain</artifactId>\n    <version>3.0.0</version>\n</dependency>\n\n<dependency>\n    <groupId>com.fraktalio.fmodel</groupId>\n    <artifactId>application-vanilla</artifactId>\n    <version>3.0.0</version>\n</dependency>\n\n<dependency>\n    <groupId>com.fraktalio.fmodel</groupId>\n    <artifactId>application-arrow</artifactId>\n    <version>3.0.0</version>\n</dependency>\n")))))}u.isMDXComponent=!0},5162:(e,a,t)=>{t.d(a,{Z:()=>o});var n=t(7294),r=t(6010);const l={tabItem:"tabItem_Ymn6"};function o(e){let{children:a,hidden:t,className:o}=e;return n.createElement("div",{role:"tabpanel",className:(0,r.Z)(l.tabItem,o),hidden:t},a)}},4866:(e,a,t)=>{t.d(a,{Z:()=>N});var n=t(7462),r=t(7294),l=t(6010),o=t(2466),i=t(6550),c=t(1980),m=t(7392),d=t(12);function p(e){return function(e){return r.Children.map(e,(e=>{if(!e||(0,r.isValidElement)(e)&&function(e){const{props:a}=e;return!!a&&"object"==typeof a&&"value"in a}(e))return e;throw new Error(`Docusaurus error: Bad <Tabs> child <${"string"==typeof e.type?e.type:e.type.name}>: all children of the <Tabs> component should be <TabItem>, and every <TabItem> should have a unique "value" prop.`)}))?.filter(Boolean)??[]}(e).map((e=>{let{props:{value:a,label:t,attributes:n,default:r}}=e;return{value:a,label:t,attributes:n,default:r}}))}function s(e){const{values:a,children:t}=e;return(0,r.useMemo)((()=>{const e=a??p(t);return function(e){const a=(0,m.l)(e,((e,a)=>e.value===a.value));if(a.length>0)throw new Error(`Docusaurus error: Duplicate values "${a.map((e=>e.value)).join(", ")}" found in <Tabs>. Every value needs to be unique.`)}(e),e}),[a,t])}function u(e){let{value:a,tabValues:t}=e;return t.some((e=>e.value===a))}function f(e){let{queryString:a=!1,groupId:t}=e;const n=(0,i.k6)(),l=function(e){let{queryString:a=!1,groupId:t}=e;if("string"==typeof a)return a;if(!1===a)return null;if(!0===a&&!t)throw new Error('Docusaurus error: The <Tabs> component groupId prop is required if queryString=true, because this value is used as the search param name. You can also provide an explicit value such as queryString="my-search-param".');return t??null}({queryString:a,groupId:t});return[(0,c._X)(l),(0,r.useCallback)((e=>{if(!l)return;const a=new URLSearchParams(n.location.search);a.set(l,e),n.replace({...n.location,search:a.toString()})}),[l,n])]}function v(e){const{defaultValue:a,queryString:t=!1,groupId:n}=e,l=s(e),[o,i]=(0,r.useState)((()=>function(e){let{defaultValue:a,tabValues:t}=e;if(0===t.length)throw new Error("Docusaurus error: the <Tabs> component requires at least one <TabItem> children component");if(a){if(!u({value:a,tabValues:t}))throw new Error(`Docusaurus error: The <Tabs> has a defaultValue "${a}" but none of its children has the corresponding value. Available values are: ${t.map((e=>e.value)).join(", ")}. If you intend to show no default tab, use defaultValue={null} instead.`);return a}const n=t.find((e=>e.default))??t[0];if(!n)throw new Error("Unexpected error: 0 tabValues");return n.value}({defaultValue:a,tabValues:l}))),[c,m]=f({queryString:t,groupId:n}),[p,v]=function(e){let{groupId:a}=e;const t=function(e){return e?`docusaurus.tab.${e}`:null}(a),[n,l]=(0,d.Nk)(t);return[n,(0,r.useCallback)((e=>{t&&l.set(e)}),[t,l])]}({groupId:n}),k=(()=>{const e=c??p;return u({value:e,tabValues:l})?e:null})();(0,r.useLayoutEffect)((()=>{k&&i(k)}),[k]);return{selectedValue:o,selectValue:(0,r.useCallback)((e=>{if(!u({value:e,tabValues:l}))throw new Error(`Can't select invalid tab value=${e}`);i(e),m(e),v(e)}),[m,v,l]),tabValues:l}}var k=t(2389);const h={tabList:"tabList__CuJ",tabItem:"tabItem_LNqP"};function g(e){let{className:a,block:t,selectedValue:i,selectValue:c,tabValues:m}=e;const d=[],{blockElementScrollPositionUntilNextRender:p}=(0,o.o5)(),s=e=>{const a=e.currentTarget,t=d.indexOf(a),n=m[t].value;n!==i&&(p(a),c(n))},u=e=>{let a=null;switch(e.key){case"Enter":s(e);break;case"ArrowRight":{const t=d.indexOf(e.currentTarget)+1;a=d[t]??d[0];break}case"ArrowLeft":{const t=d.indexOf(e.currentTarget)-1;a=d[t]??d[d.length-1];break}}a?.focus()};return r.createElement("ul",{role:"tablist","aria-orientation":"horizontal",className:(0,l.Z)("tabs",{"tabs--block":t},a)},m.map((e=>{let{value:a,label:t,attributes:o}=e;return r.createElement("li",(0,n.Z)({role:"tab",tabIndex:i===a?0:-1,"aria-selected":i===a,key:a,ref:e=>d.push(e),onKeyDown:u,onClick:s},o,{className:(0,l.Z)("tabs__item",h.tabItem,o?.className,{"tabs__item--active":i===a})}),t??a)})))}function b(e){let{lazy:a,children:t,selectedValue:n}=e;const l=(Array.isArray(t)?t:[t]).filter(Boolean);if(a){const e=l.find((e=>e.props.value===n));return e?(0,r.cloneElement)(e,{className:"margin-top--md"}):null}return r.createElement("div",{className:"margin-top--md"},l.map(((e,a)=>(0,r.cloneElement)(e,{key:a,hidden:e.props.value!==n}))))}function y(e){const a=v(e);return r.createElement("div",{className:(0,l.Z)("tabs-container",h.tabList)},r.createElement(g,(0,n.Z)({},e,a)),r.createElement(b,(0,n.Z)({},e,a)))}function N(e){const a=(0,k.Z)();return r.createElement(y,(0,n.Z)({key:String(a)},e))}}}]);
import React from 'react';
import clsx from 'clsx';
import Link from '@docusaurus/Link';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import Layout from '@theme/Layout';
import HomepageFeatures from '@site/src/components/HomepageFeatures';
import CodeBlock from '@theme/CodeBlock';

import styles from './index.module.css';

function HomepageHeader() {
    const {siteConfig} = useDocusaurusContext();
    return (
        <header className={clsx('hero hero--primary', styles.heroBanner)}>
            <div className="container">
                <h1 className="hero__title">{siteConfig.title}</h1>
                <p className="hero__subtitle">{siteConfig.tagline}</p>
                <p>Functional, Algebraic and Reactive Domain Modeling</p>
                <p><b>Kotlin / Multiplatform</b></p>
                <div className={styles.buttons}>
                    <Link
                        className="button button--secondary button--lg"
                        to="/docs/intro">
                        Get Started
                    </Link>
                </div>
            </div>
        </header>
    );
}

export default function Home(): JSX.Element {
    const {siteConfig} = useDocusaurusContext();
    return (

        <Layout
            title={`${siteConfig.title}`}
            description="Functional, algebraic and reactive domain modeling">
            <HomepageHeader/>
            <main>
                <HomepageFeatures/>
                <div className="container">
                        <CodeBlock
                            language="kotlin"
                            // title="A simplified model of an Order process:"
                            showLineNumbers>
                            {`
typealias OrderDecider = Decider<OrderCommand?, Order?, OrderEvent?>

fun orderDecider() = OrderDecider(
    // Initial state/s of the Order
    initialState = null,
    // Decide new events/e based on the command/c and current state/s
    decide = { c, s ->
        when (c) {
            is CreateOrderCommand ->
                if (s == null) flowOf(OrderCreatedEvent(c.identifier, c.lineItems, c.restaurantIdentifier))
                else flowOf(OrderRejectedEvent(c.identifier, Reason("Order already exists")))

            is MarkOrderAsPreparedCommand ->
                if (s == null) flowOf(OrderNotPreparedEvent(c.identifier, Reason("Order does not exist")))
                else if (OrderStatus.CREATED != s.status) flowOf(OrderNotPreparedEvent(c.identifier, Reason("Order not in CREATED status")))
                else flowOf(OrderPreparedEvent(c.identifier))
            // Ignore the null command by emitting the empty flow of events
            null -> emptyFlow()
        }
    },
    // Evolve the new state/s based on the event/e and current state/s
    evolve = { s, e ->
        when (e) {
            is OrderCreatedEvent -> Order(e.identifier, e.restaurantId, e.status, e.lineItems)
            is OrderPreparedEvent -> s?.copy(status = e.status)
            is OrderRejectedEvent -> s?.copy(status = e.status)
            is OrderErrorEvent -> s
            // Ignore the null events, by not changing the state
            null -> s
        }
    }
)
// Check the demos for more examples:
// https://github.com/fraktalio/fmodel-spring-demo
// https://github.com/fraktalio/fmodel-ktor-demo
                            `}
                        </CodeBlock>
                    </div>

            </main>
        </Layout>
    );
}

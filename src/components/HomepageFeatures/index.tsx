import React from 'react';
import clsx from 'clsx';
import styles from './styles.module.css';

type FeatureItem = {
    title: string;
    link: string;
    Svg: React.ComponentType<React.ComponentProps<'svg'>>;
    description: JSX.Element;
};

const FeatureList: FeatureItem[] = [
    {
        title: 'Command',
        link: 'docs/domain/structuring-the-data',
        Svg: require('@site/static/img/command.svg').default,
        description: (
            <>
                An intent to change the state of the system.
            </>
        ),
    },
    {
        title: 'Event',
        link: 'docs/domain/structuring-the-data',
        Svg: require('@site/static/img/event.svg').default,
        description: (
            <>
                The state change itself, a fact.
                It represents a decision that has already happened.
            </>
        ),
    },
    {
        title: 'State',
        link: 'docs/domain/structuring-the-data',
        Svg: require('@site/static/img/state.svg').default,
        description: (
            <>
                The current state of the system.
                It is evolved out of past events.
            </>
        ),
    },
    {
        title: 'Decide',
        link: 'docs/domain/modeling-the-behaviour',
        Svg: require('@site/static/img/decide.svg').default,
        description: (
            <>
                A pure function that takes <code>command</code> and current <code>state</code> as parameters,
                and returns the flow of new <code>events</code>.
            </>
        ),
    },
    {
        title: 'Evolve',
        link: 'docs/domain/modeling-the-behaviour',
        Svg: require('@site/static/img/evolve.svg').default,
        description: (
            <>
                A pure function that takes <code>event</code> and current <code>state</code> as parameters,
                and returns the new <code>state</code> of the system.
            </>
        ),
    },
    {
        title: 'React',
        link: 'docs/domain/modeling-the-behaviour',
        Svg: require('@site/static/img/orchestrate.svg').default,
        description: (
            <>
                A pure function that takes <code>event</code> as parameter,
                and returns the flow of <code>commands</code>, deciding what to execute next.
            </>
        ),
    },
    {
        title: 'EventSourced systems',
        link: 'docs/application/architecture?system-type=event-stored',
        Svg: require('@site/static/img/es.svg').default,
        description: (
            <>
                Event-Sourced systems are storing the <code>events</code> in immutable storage by only appending.
            </>
        ),
    },
    {
        title: 'StateStored systems',
        link: 'docs/application/architecture?system-type=state-stored',
        Svg: require('@site/static/img/ss.svg').default,
        description: (
            <>
                State-stored systems are traditional systems that are only storing the current <code>state</code> by overwriting the previous <code>state</code> in the storage.
            </>
        ),
    },
    {
        title: 'EventSourced + StateStored',
        link: 'docs/application/architecture',
        Svg: require('@site/static/img/es-ss.svg').default,
        description: (
            <>
                Both types of systems can be designed by using only these three functions (<code>decide</code>, <code>evolve</code>, <code>react</code>) and three generic parameters (<code>command</code>, <code>event</code>, <code>state</code>).
            </>
        ),
    },
];

function Feature({title, link, Svg, description}: FeatureItem) {
    return (
        <div className={clsx('col col--4')}>
            <div className="text--center">
                <a href={link}><Svg className={styles.featureSvg} role="img"/></a>
            </div>
            <div className="text--center padding-horiz--md">
                <h3>{title}</h3>
                <p>{description}</p>
            </div>
        </div>
    );
}

export default function HomepageFeatures(): JSX.Element {
    return (
        <section className={styles.features}>
            <div className="container">
                <div className="row">
                    {FeatureList.map((props, idx) => (
                        <Feature key={idx} {...props} />
                    ))}
                </div>
            </div>
        </section>
    );
}

import React from 'react';
import clsx from 'clsx';
import Layout from '@theme/Layout';
import Link from '@docusaurus/Link';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import useBaseUrl from '@docusaurus/useBaseUrl';
import styles from './styles.module.css';

const features = [
  {
    title: <>Easy to Use</>,
    description: (
      <>
        ScalaPB translates Protocol Buffers to Scala case classes.
        The generated API is easy to use!
      </>
    ),
  },
  {
    title: <>Supports proto2 and proto3</>,
    description: (
      <>
        ScalaPB is built as a protoc plugin and has perfect compatibility
        with the protobuf language specification.
      </>
    ),
  },
  {
    title: <>Nested updates</>,
    description: (
      <>
        Updating immutable nested structure is made easy by an optional
        lenses support. <a href="docs/generated-code#updating-messages">Learn more.</a>
      </>
    ),
  },
  {
    title: <>Interoperate with Java</>,
    description: (
      <>
        Scala Protocol Buffers can be converted to Java and vice versa. Scala and
        Java protobufs can co-exist in the same project to make it easier to gradually
        migrate, or interact with legacy Java APIs.
      </>
    ),
  },
  {
    title: <>Scala.js support</>,
    description: (
      <>
        ScalaPB fully supports Scala.js so you can write Scala programs that use your
        domain-specific Protocol Buffers in the browser! <a href="docs/scala.js">Learn more.</a>
      </>
    ),
  },
  {
    title: <>gRPC</>,
    description: (
      <>
        Build gRPC servers and clients with ScalaPB. ScalaPB ships with its
        own wrapper around the official gRPC Java implementation. There are gRPC
        libraries for ZIO, Cats Effect and Akka. <a href="docs/grpc"></a>
      </>
    ),
  },
];

function Feature({imageUrl, title, description}) {
  const imgUrl = useBaseUrl(imageUrl);
  return (
    <div className={clsx('col col--4', styles.feature)}>
      {imgUrl && (
        <div className="text--center">
          <img className={styles.featureImage} src={imgUrl} alt={title} />
        </div>
      )}
      <h3>{title}</h3>
      <p>{description}</p>
    </div>
  );
}

function Home() {
  const context = useDocusaurusContext();
  const {siteConfig = {}} = context;
  const img = useBaseUrl('img/ScalaPB.png');
  return (
    <Layout
      title="ScalaPB: Protocol Buffer Compiler for Scala"
      description="ScalaPB compiles protocol buffers into Scala case classes.">
      <header className={clsx('hero hero--primary', styles.heroBanner)}>
        <div className="container">
          <img src={img} width="80%"/>
          <p className="hero__subtitle">{siteConfig.tagline}</p>
          <div className={styles.buttons}>
            <Link
              className={clsx(
                // 'button button--outline button--secondary button--lg',
                styles.indexCtasGetStartedButton,
              )}
              to={useBaseUrl('docs/')}>
              Get Started
            </Link>
          </div>
        </div>
      </header>
      <main>
        {features && features.length > 0 && (
          <section className={styles.features}>
            <div className="container">
              <div className="row">
                {features.map((props, idx) => (
                  <Feature key={idx} {...props} />
                ))}
              </div>
            </div>
          </section>
        )}
      </main>
    </Layout>
  );
}

export default Home;

module.exports = {
  title: 'ScalaPB',
  tagline: 'Protocol Buffer Compiler for Scala',
  url: 'https://scalapb.github.io/',
  baseUrl: '/',
  onBrokenLinks: 'throw',
  favicon: 'img/favicon.ico',
  organizationName: 'scalapb', // Usually your GitHub org/user name.
  projectName: 'scalapb.github.io',
  themeConfig: {
    sidebarCollapsible: false,
    navbar: {
      logo: {
        alt: 'ScalaPB',
        src: 'img/ScalaPB.png',
      },
      items: [
        {
          to: 'docs/',
          activeBasePath: 'docs',
          label: 'Docs',
          position: 'left',
        },
        {
          href: 'https://github.com/scalapb/ScalaPB',
          label: 'GitHub',
          position: 'right',
        },
      ],
    },
    footer: {
      style: 'dark',
      links: [
        {
          title: 'Docs',
          items: [
            {
              label: 'Introduction',
              to: 'docs/',
            },
            {
              label: 'Installation',
              to: 'docs/installation',
            },
            {
              label: 'ScalaDoc',
              href: 'https://scalapb.github.io/api/scalapb'
            },
          ],
        },
        {
          title: 'Community',
          items: [
            {
              label: 'Stack Overflow',
              href: 'https://stackoverflow.com/questions/tagged/scalapb',
            },
            {
              label: 'Gitter',
              href: 'https://gitter.im/ScalaPB/community',
            },
            {
              label: 'Google Groups',
              href: 'https://groups.google.com/g/scalapb',
            },
          ],
        },
        {
          title: 'More',
          items: [
            {
              label: 'GitHub',
              href: 'https://github.com/scalapb/ScalaPB',
            },
          ],
        },
      ],
      copyright: `Copyright © 2014-${new Date().getFullYear()}, <a href="https://www.linkedin.com/in/nadav-samet/">Nadav Samet</a>`,
    },
    prism: {
      additionalLanguages: ['scala', 'protobuf'],
      theme: require('prism-react-renderer/themes/nightOwlLight'),
      darkTheme: require('prism-react-renderer/themes/dracula')
    },
    googleAnalytics: {
      trackingID: 'UA-346180-20'
    }
  },
  presets: [
    [
      '@docusaurus/preset-classic',
      {
        docs: {
          // It is recommended to set document id as docs home page (`docs/` path).
          homePageId: 'intro',
          sidebarPath: require.resolve('./sidebars.js'),
          // mdoc generates to website/docs. The edit url makes it point at
          // the
          path: '../docs/target/mdoc',
          // editUrl:
          //   'https://github.com/scalapb/ScalaPB/edit/master/docs/',
        },
        blog: {
          showReadingTime: true,
          // Please change this to your repo.
          // editUrl:
          //   'https://github.com/facebook/docusaurus/edit/master/website/blog/',
        },
        theme: {
          customCss: require.resolve('./src/css/custom.css'),
        },
      },
    ],
  ],
};

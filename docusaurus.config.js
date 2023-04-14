// @ts-check
// Note: type annotations allow type checking and IDEs autocompletion

const lightCodeTheme = require('prism-react-renderer/themes/github');
const darkCodeTheme = require('prism-react-renderer/themes/dracula');

/** @type {import('@docusaurus/types').Config} */
const config = {
    title: 'Fmodel',
    tagline: 'Functional, Algebraic and Reactive Domain Modeling ',
    favicon: 'img/favicon-32x32.png',

    // Set the production url of your site here
    url: 'https://fraktalio.com',
    // Set the /<baseUrl>/ pathname under which your site is served
    // For GitHub pages deployment, it is often '/<projectName>/'
    baseUrl: '/fmodel/',

    // GitHub pages deployment config.
    // If you aren't using GitHub pages, you don't need these.
    organizationName: 'fraktalio', // Usually your GitHub org/user name.
    projectName: 'fmodel', // Usually your repo name.

    onBrokenLinks: 'throw',
    onBrokenMarkdownLinks: 'warn',
    trailingSlash: false,

    // Even if you don't use internalization, you can use this field to set useful
    // metadata like html lang. For example, if your site is Chinese, you may want
    // to replace "en" with "zh-Hans".
    i18n: {
        defaultLocale: 'en',
        locales: ['en'],
    },

    presets: [
        [
            'classic',
            /** @type {import('@docusaurus/preset-classic').Options} */
            ({
                docs: {
                    sidebarPath: require.resolve('./sidebars.js'),
                    // Please change this to your repo.
                    // Remove this to remove the "edit this page" links.
                    // editUrl:
                    //   'https://github.com/facebook/docusaurus/tree/main/packages/create-docusaurus/templates/shared/',
                },
                blog: {
                    showReadingTime: true,
                    // Please change this to your repo.
                    // Remove this to remove the "edit this page" links.
                    // editUrl:
                    //   'https://github.com/facebook/docusaurus/tree/main/packages/create-docusaurus/templates/shared/',
                },
                theme: {
                    customCss: require.resolve('./src/css/custom.css'),
                },
                gtag: {
                    trackingID: 'G-QGWDF1Z9RM',
                    anonymizeIP: true,
                },
            }),
        ],
    ],

    themeConfig:
    /** @type {import('@docusaurus/preset-classic').ThemeConfig} */
        ({
            // Replace with your project's social card
            image: 'img/fmodel-social2.png',
            metadata: [{name: 'keywords', content: 'domain-modeling, event-sourcing, event-modeling, kotlin'}],
            navbar: {
                title: 'Fmodel',
                logo: {
                    alt: 'Fraktalio Logo',
                    src: 'img/logo.png',
                },
                items: [
                    {
                        type: 'docSidebar',
                        sidebarId: 'tutorialSidebar',
                        position: 'left',
                        label: 'Learn',
                    },
                    //{to: '/blog', label: 'Blog', position: 'left'},
                    {to: '/release-notes', label: 'Release Notes', position: 'left'},
                    {
                        href: 'https://fraktalio.com',
                        label: 'fraktalio.com',
                        position: 'right',
                    },
                    {
                        href: 'https://github.com/fraktalio/fmodel',
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
                                label: 'Learn',
                                to: '/docs/intro',
                            },
                            {
                                label: 'Get Started',
                                to: '/docs/intro#getting-started',
                            }
                        ],
                    },
                    {
                        title: 'Community',
                        items: [
                            {
                                label: 'Mastodon',
                                href: 'https://fosstodon.org/@fraktalio',
                            },
                            {
                                label: 'Twitter',
                                href: 'https://twitter.com/fraktalio',
                            },
                        ],
                    },
                    {
                        title: 'More',
                        items: [
                            // {
                            //   label: 'Blog',
                            //   to: '/blog',
                            // },
                            {
                                label: 'fraktalio.com',
                                href: 'https://fraktalio.com',
                            },
                            {
                                label: 'GitHub',
                                href: 'https://github.com/fraktalio/fmodel',
                            }
                        ],
                    },
                ],
                copyright: `Copyright © ${new Date().getFullYear()} Fraktalio D.O.O.`,
            },
            prism: {
                additionalLanguages: ['kotlin'],
                theme: lightCodeTheme,
                darkTheme: darkCodeTheme,
            },
        }),
};

module.exports = config;

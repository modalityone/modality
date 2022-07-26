# How to contribute to Modality

Thank you so much for your wish to contribute to Modality! There are many areas where help is needed, and we have listed some suggestions below, but we are also open to your own suggestions.


## Adding JavaDoc

Modality is now starting to transition from the prototyping phase into MVP development, and the newly stable code is ready to benefit from comprehensive JavaDoc.

- **Please [contact us][modality-contactus] if you would like to get involved with this.**


## Writing unit tests

Despite already being a significantly-sized codebase, there are currently no unit tests, which is something we need to change as soon as possible.

- **If you are happy to help write some unit tests, please [contact us][modality-contactus].**


## Developing integration tests

Large parts of Modality are already operational, principally in the Back-Office, which is organised according to activities (screens). The completed activities are ready to be subjected to automated integration testing, together with all newly created activities.

- **If you are happy to get involved with the integration testing, please [contact us][modality-contactus].**


## Writing business layer code

The highest layer of the Modality architecture consists of business-specific modules implementing logic for events, hotels, restaurants etc. Code written here is usually directly represented on-screen.

- **If you want to contribute business layer code, please [contact us][modality-contactus].**


## Writing ecommerce layer code

The next layer down is the ecommerce layer. This provides a generic domain model for ecommerce, which models sales, accounts etc. It is the location for payment gateway integration and ecommerce-specific UIs.

- **If you want to contribute ecommerce layer code, please [contact us][modality-contactus].**


## Writing CRM layer code

The CRM layer provides the essential CRM features, including customer accounts, integrated mailing system etc.

- **If you want to contribute CRM layer code, please [contact us][modality-contactus].**


## Writing Base layer code

The Base layer is a fully operational implementation of the [WebFX](https://webfx.dev) stack layer beneath, based on the Postgres database. This layer is a pure technical solution that isn’t bound to any specific domain.

- **If you want to contribute base layer code, please [contact us][modality-contactus].**


## Reporting bugs

- **You can report bugs found in [Modality][modality-repo].** Issues are open for Modality, and we will do our best to fix them.


## Fixing bugs

If you find a bug, you can fork [Modality][modality-repo] and try to fix it.

- **We are accepting pull requests should you wish to submit a bug fix from your fork.**


## Branding

Modality would benefit from a clean and distinctive branding, initially within the app itself, and later extending to associated external artefacts such as the website etc.

- **If you have any Modality branding ideas or drafts, including logo, color palette and font suggestions, you can [send them to us][modality-contactus]. We will be delighted to consider your propositions.**


## Reviewing our documentation

Where our [documentation](https://docs.modality-project.org) is not clear or detailed enough, or where you would like additional documentation for other aspects of Modality that are not planned in the [roadmap](ROADMAP.md), please let us know.

- **You can open an issue for this in our [Modality Docs](https://github.com/webfx-project/modality-docs) repository.**


***


Thank you so much for joining the Modality community!

[modality-repo]: https://github.com/mongoose-project/modality
[modality-contactus]: mailto:maintainer@modality-project.org

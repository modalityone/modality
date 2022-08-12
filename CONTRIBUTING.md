# How to contribute to Modality

Thank you so much for your wish to contribute to Modality! There are many areas where help is needed, and we have listed some suggestions below, but we are also open to your own suggestions.


## Adding Javadoc

Modality is now starting to transition from the prototyping phase into MVP development, and the newly stable code is ready to benefit from comprehensive Javadoc.

- **If you would like to add useful Javadoc to the codebase, please fork Modality for this purpose, then raise a pull request for us to merge your changes into main.**


## Writing unit tests

We are now in a position to start writing new and retrospective tests for Modality, and have incorporated the [JUnit](https://junit.org/junit5/) and [Mockito](https://site.mockito.org/) test frameworks into the codebase.

- **If you would like to write new (or modify existing) JUnit tests, you could fork Modality for this purpose, then raise a pull request for us to merge your changes into main.**


## Developing integration tests

Large parts of Modality are already operational, principally in the Back-Office, which is organised according to activities (screens). The completed activities are ready to be subjected to automated integration testing, together with all newly created activities. 

- **We will soon be incorporating an integration test framework into Modality, after which we will be happy to invite you to help us expand the integration test coverage.**


## Writing business layer code

The highest [layer](https://docs.modality-project.org/#_layers) of the Modality architecture consists of business-specific modules implementing logic for events, hotels, restaurants etc. Code written here is usually directly represented on-screen. 

- **If you would like to contribute business layer code, feel free to assign yourself to one of our GitHub [issues](https://github.com/modalityproject/modality/issues?q=is%3Aissue+is%3Aopen+label%3A%22business+layer+code%22).**


## Writing ecommerce layer code

The next [layer](https://docs.modality-project.org/#_layers) down is the ecommerce layer. This provides a generic domain model for ecommerce, which models sales, accounts etc. It is the location for payment gateway integration and ecommerce-specific UIs.

- **If you would like to contribute ecommerce layer code, feel free to assign yourself to one of our GitHub [issues](https://github.com/modalityproject/modality/issues?q=is%3Aissue+is%3Aopen+label%3A%22ecommerce+layer+code%22+).**


## Writing CRM layer code

The CRM [layer](https://docs.modality-project.org/#_layers) provides the essential CRM features, including customer accounts, integrated mailing system etc.

- **If you would like to contribute CRM layer code, take a look at our GitHub [issues](https://github.com/modalityproject/modality/issues?q=is%3Aissue+is%3Aopen+label%3A%22crm+layer+code%22).**


## Writing Base layer code

The Base [layer](https://docs.modality-project.org/#_layers) is a fully operational implementation of the [WebFX](https://webfx.dev) stack layer beneath, based on the Postgres database. This layer is a pure technical solution that isn’t bound to any specific domain.

- **If you would like to contribute base layer code, take a look at our GitHub [issues](https://github.com/modalityproject/modality/issues?q=is%3Aissue+is%3Aopen+label%3A%22base+layer+code%22+).**


## Reporting bugs

- We invite you to report any bugs found in Modality on our GitHub [issues](https://github.com/modalityproject/modality/issues) page.


## Fixing bugs

If you find a bug, or would like to work on a previously-reported bug on our [issues](https://github.com/modalityproject/modality/labels/bug) page, you could fork the Modality and try to fix it.

- **We are accepting pull requests should you wish to submit a bug fix from your fork.**


<!--
## Branding

Modality would benefit from a clean and distinctive branding, initially within the app itself, and later extending to associated external artefacts such as the website etc.

- **If you have any Modality branding ideas or drafts, including logo, color palette and font suggestions, you can [send them to us][modality-contactus]. We will be delighted to consider your propositions.**


## Reviewing our documentation

Where our [documentation](https://docs.modality-project.org) is not clear or detailed enough, or where you would like additional documentation for other aspects of Modality that are not planned in the [roadmap](ROADMAP.md), please let us know.

- **You can open an issue for this in our [Modality Docs](https://github.com/webfx-project/modality-docs) repository.**
-->


***


Thank you so much for joining the Modality community!

[modality-repo]: https://github.com/mongoose-project/modality
[modality-contactus]: mailto:maintainer@modality-project.org

# Acme Air Sample and Benchmark

This application shows an implementation of a fictitious airline called "Acme Air".  The application was built with the some key business requirements: the ability to scale to billions of web API calls per day, the need to develop and deploy the application in public clouds (as opposed to dedicated pre-allocated infrastructure), and the need to support multiple channels for user interaction (with mobile enablement first and browser/Web 2.0 second).

There are three implementations of the application each with an application tier and a data tier.  They are:
- **NodeJS to Mongodb**
- **Java / WebSphere Liberty Profile to WebSphere eXtreme Scale**
- **Java / WebSphere Liberty Profile to Mongodb**

## Repository Contents

Source:

- **acmeair-common**: The Java entities used throughout the application
- **acmeair-loader**:  A tool to load the Java implementation data store
- **acmeair-services**:  The Java data services interface definitions
- **acmeair-service-wxs**:  A WebSphere eXtreme Scale data service implementation
- **acmeair-service-morphia**:  A mongodb data service implementation
- **acmeair-webapp**:  The Web 2.0 application and associated Java REST services
- **acmeair-webapp-nodejs**: A implementation of the Acme Air application in NodeJS with a MongoDB data store backend
- **acmeair-driver**: The workload driver script and supporting classes and resources

## How to get started

* See the [wiki](https://github.com/acmeair/acmeair/wiki)
* Websphere Liberty to Mongodb Instructions (https://github.com/acmeair/acmeair/blob/mongodb/MONGO_README.md)


## Ask Questions

Questions about the Acme Air Open Source Project can be directed to our Google Groups.

* Acme Air Users: [https://groups.google.com/forum/?fromgroups#!forum/acmeair-users](https://groups.google.com/forum/?fromgroups#!forum/acmeair-users)

## Submit a bug report

We use github issues to report and handle bug reports.

## OSS Contributions

We accept contributions via pull requests.

We will be posting important information about CLA agreements needed for us to accept pull requests soon.

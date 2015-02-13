# Acme Air Sample and Benchmark

This application shows an implementation of a fictitious airline called "Acme Air".  The application was built with the some key business requirements: the ability to scale to billions of web API calls per day, the need to develop and deploy the application in public clouds (as opposed to dedicated pre-allocated infrastructure), and the need to support multiple channels for user interaction (with mobile enablement first and browser/Web 2.0 second).

There are two implementations of the application tier. Each application implementation, supports multiple data tiers.  They are:
- **Node.js**
  - MongoDB
  - Cloudant
- **Java**
  - WebSphere Liberty Profile to WebSphere eXtreme Scale
  - WebSphere Liberty Profile to Mongodb

## Repository Contents

Source:

- **acmeair-common**: The Java entities used throughout the application
- **acmeair-loader**:  A tool to load the Java implementation data store
- **acmeair-services**:  The Java data services interface definitions
- **acmeair-service-wxs**:  A WebSphere eXtreme Scale data service implementation
- **acmeair-service-morphia**:  A mongodb data service implementation
- **acmeair-webapp**:  The Web 2.0 application and associated Java REST services
- **acmeair-driver**: The workload driver script and supporting classes and resources

## How to get started

* See the [wiki](https://github.com/acmeair/acmeair/wiki)
* Websphere Liberty Profile to [MongoDB Instructions](https://github.com/acmeair/acmeair/blob/master/MONGO_README.md)
* Acme Air for Node.js [Instructions](https://github.com/acmeair/acmeair-nodejs/blob/master/README.md)

## Ask Questions

Questions about the Acme Air Open Source Project can be directed to our Google Groups.

* Acme Air Users: [https://groups.google.com/forum/?fromgroups#!forum/acmeair-users](https://groups.google.com/forum/?fromgroups#!forum/acmeair-users)

## Submit a bug report

We use github issues to report and handle bug reports.

## OSS Contributions

We accept contributions via pull requests.

We will be posting important information about CLA agreements needed for us to accept pull requests soon.

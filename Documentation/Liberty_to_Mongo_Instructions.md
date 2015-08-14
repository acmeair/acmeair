# WebSphere Liberty to MongoDB Setup Instructions 

This document will walk through the steps required to configure the Acme Air sample application and WebSphere Liberty to use MongoDB. These instructions build upon the information from the [WebSphere Liberty instructions document](Liberty_Instructions.md). 


## Build the Acme Air codebase to use the morphia service 

To communicate with MongoDB, Acme Air uses the [Morphia](http://mongodb.github.io/morphia/) MongoDB driver. Acme Air needs to be built using with this service specified. 


```text
./gradlew -Pservice=morphia clean build
```

Further details for building the Acme Air codebase can be found [here](Build_Instructions.md).


## Download the MongoDB Java Driver

Acme Air has been tested with the 2.12.2 version of the mongo-java-driver
* Download link:  http://mongodb.github.io/mongo-java-driver/ 

Copy the mongo-java-driver jar file to a mongodb directory under Liberty's shared resources directory.

Windows:
```text 
copy mongo-java-driver-2.12.2.jar %WLP_SERVERDIR%\usr\shared\resources\mongodb\
```

Linux:
```text  
cp mongo-java-driver-2.12.2.jar $WLP_SERVERDIR/usr/shared/resources/mongodb/
```

## Modify the WebSphere Liberty server configuration file.  

* Edit %WLP_SERVERDIR%\usr\servers\server1\server.xml to change the featureManager section to:

```xml
   <featureManager>
         <feature>webProfile-7.0</feature>
         <feature>mongodb-2.0</feature>
    </featureManager>
```


Add the MongoDB driver location, and add the classloader reference

```xml
    <library id="mongo-lib">
        <file name="${shared.resource.dir}/mongodb/mongo-java-driver-2.12.2.jar" />
    </library>

    <application id="acmeair-webapp" name="acmeair-webapp" type="war" location="acmeair-webapp-1.1.0-SNAPSHOT.war">
        <classloader commonLibraryRef="mongo-lib" />
    </application>
```

Add the configuration information so Liberty knows how to connect to MongoDB. 
```xml
    <mongo id="mongo" libraryRef="mongo-lib" hostNames="localhost" ports="27017"/>
    <mongoDB jndiName="mongo/acmeairMongodb" mongoRef="mongo" databaseName="acmeair"/>
```


And add a jndiEntry to indicate the service type to use
```xml
    <jndiEntry jndiName="com/acmeair/repository/type" value="morphia"/>
```

## Configure and start MongoDB

See the MongoDB [Manual](http://docs.mongodb.org/manual/) for more information on using MongoDB. 

## Start Liberty and Load the database.

Follow the remaining steps in the [WebSphere Liberty instructions document](Liberty_Instructions.md).

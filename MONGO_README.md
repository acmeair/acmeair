# Additional Instructions for Running Acmeair on WebSphere Liberty to Mongodb

## Connection to Mongodb

Default Mongodb location is localhost:27017/acmeair. You can revise the content [here](https://github.com/acmeair/acmeair/blob/mongodb/acmeair-services-morphia/src/main/resources/acmeair-mongo.properties) before build the application.


## Enable Mongodb for application runtime

Acmeair by default will connect to WebSphere eXtreme Scale. To enable Mongodb, you need to add the following content to Liberty's server.xml:

    <jndiEntry jndiName="com/acmeair/repository/type" value="mongodirect"/>



## Enable Mongodb for loader

Loader by default will connect to WebSphere eXtreme Scale. To enable Mongodb, you need to add the following JVM property when running loader:

    -Dcom.acmeair.repository.type=mongodirect
## WebSphere Liberty Setup Instructions 

If you have not already done so, read through the [instructions for building the codebase](Build_Instructions.md) first. 


## Download the following free for developers or trial versions of WebSphere software

WebSphere Liberty 8.5.5.6
* Download link:  https://developer.ibm.com/wasdev/downloads/liberty-profile-using-non-eclipse-environments
* Click on the "Download Zip" for the "WAS Liberty with Java EE 7 Web Profile" image 
* Review and agree to the license, click the "Accept and download" button and save the resulting "wlp-webProfile7-8.5.5.6.zip" file.

In addition, if you wish to use the WebSphere eXtreme Scale service you will need to install the  "WebSphere eXtreme Scale for Developers Liberty Profile" addon. 
* Download link:  https://www.ibmdw.net/wasdev/downloads/
* Click on the "Download V8.6" image under "WebSphere eXtreme Scale for Developers Liberty Profile"
* Click on the "I confirm" button, review and agree to the license, click the "Download Now" link and save the resulting "wxs-wlp_8.6.0.8.jar" file.

* Install the WebSphere Liberty Profile Developers Runtime file into a directory of your choice
```text
unzip wlp-webProfile7-8.5.5.6.zip
```

* Install the "WebSphere eXtreme Scale for Developers Liberty Profile" file into the same directory as the developers runtime
```text
java -jar wxs-wlp_8.6.0.8.jar
```

* For the rest of these instructions we will assume this to be the WLP_SERVERDIR
Windows:
```text
set WLP_SERVERDIR=C:\work\java\wlp
```
 
Linux:
```text
export WLP_SERVERDIR=~/work/java/wlp
```


## Create the WebSphere Liberty server and then deploy the application
Windows:
```text
cd %WLP_SERVERDIR%
bin\server create server1
```

Linux:
```text
cd $WLP_SERVERDIR
bin/server create server1
```

* Copy the web application you previously built
Windows:
```text
copy %ACMEAIR_SRCDIR%\acmeair-webapp\build\libs\acmeair-webapp-1.0-SNAPSHOT.war %WLP_SERVERDIR%\usr\servers\server1\dropins\.
```

Linux:
```text
cp $ACMEAIR_SRCDIR/acmeair-webapp/build/libs/acmeair-webapp-1.1.0-SNAPSHOT.war $WLP_SERVERDIR/usr/servers/server1/dropins/
```

* Start the WebSphere Liberty server
```text
cd %WLP_SERVERDIR%
bin\server start server1
```


# Using WebSphere eXtreme Scale for the Data Service 

## Modify the WebSphere Liberty server configuration file.  

* Edit %WLP_SERVERDIR%\usr\servers\server1\server.xml to change the featureManager section to:

```xml
   <featureManager>
         <feature>webProfile-7.0</feature>
         <feature>eXtremeScale.client-1.1</feature>
    </featureManager>
```

add the eXtreme Scale bindings
```xml
    <xsBindings>
       <xsGrid jndiName="wxs/acmeair" gridName="AcmeGrid"/>
    </xsBindings>
    <xsClientDomain default="dev">    
      <endpointConfig> dev ; localhost:2809 </endpointConfig>    
    </xsClientDomain>
```

And add a jndiEntry to indicate the service type to use
```xml
    <jndiEntry jndiName="com/acmeair/repository/type" value="wxs"/>
```

## Create a WebSphere eXtreme Scale configuration
* To create a new configuration we will create a copy of the "gettingstarted" configuration and customize it to a configuration and directory called "acmeair"
```text
cd %WXS_SERVERDIR%
xcopy gettingstarted\*.* acmeair\. /s/e/i/v/q
```
* Under %WXS_SERVERDIR%\acmeair, customize the env.bat to include pointers to the classes you have built
* You will find a line with SAMPLE_SERVER_CLASSPATH, modify it as below (ensure these directories and jars exist based on your environment variables)
```text
SET SAMPLE_SERVER_CLASSPATH=%SAMPLE_HOME%\server\bin;%SAMPLE_COMMON_CLASSPATH%;%ACMEAIR_SRCDIR%\acmeair-common\target\classes;%ACMEAIR_SRCDIR%\acmeair-services-wxs\target\classes;%HOMEPATH%\.m2\repository\commons-logging\commons-logging\\1.1.1\commons-logging-1.1.1.jar

```
* Next we copy the Acme Air specific eXtreme Scale configuration files from our source directory
```text
cd %WXS_SERVERDIR%\acmeair
copy /y %ACMEAIR_SRCDIR%\acmeair-services-wxs\src\main\resources\deployment.xml server\config\.
copy /y %ACMEAIR_SRCDIR%\acmeair-services-wxs\src\main\resources\objectgrid.xml server\config\.
```

## Now start the Acme Air WebSphere eXtreme Scale configuration catalog server and container server
* In one window start the catalog server
```text
cd %WXS_SERVERDIR%\acmeair
.\runcat.bat
```
* In another window start a single container server
 * Ensure that you have set JAVA_HOME and have Java in the path as before
```text
cd %WXS_SERVERDIR%\acmeair
.\runcontainer.bat c0
```


## Start the WebSphere Liberty server

* Start the WebSphere Liberty server
Windows:
```text
cd %WLP_SERVERDIR%
bin\server start server1
```

Linux:
```text
cd $WLP_SERVERDIR
bin/server start server1
```

## Look at the application
* Load the following url:
```text
http://localhost:9080/acmeair-webapp-1.0-SNAPSHOT
```


## Now we will load sample data using the web loader

Click on the "configure the Acme Air environment." link at the bottom of the page, or alternatively go to 
```text
http://localhost:9080/acmeair-webapp-1.1.0-SNAPSHOT/loader.html
```

You can change the value for how many customers you wish to have loaded.  The default of 200 customer to load will be displayed. 
* After clicking on the "Load the Database" button you should see output that indicates flights and customers (200) were loaded. 


You will now be able to log in, click on the "Acme Air Home" link at either the top or bottom of the page to return to the welcome page. 

* Login (use the provided credentials), search for flights (suggest today between Paris and New York), book the flights, use the checkin link to cancel the bookings one at a time, view your account profile



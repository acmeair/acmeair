# WebSphere Liberty to WebSphere eXtreme Scale Setup Instructions 

This document will walk through the steps required to configure the Acme Air sample application and WebSphere Liberty to use WebSphere eXtreme Scale. These instructions build upon the information from the [WebSphere Liberty instructions document](Liberty_Instructions.md). 



## Download the following free for developers or trial versions of WebSphere software

In order to use the WebSphere eXtreme Scale service you will need to install the  "WebSphere eXtreme Scale for Developers Liberty Profile" addon.
* Download link:  https://www.ibmdw.net/wasdev/downloads/
* Click on the "Download V8.6" image under "WebSphere eXtreme Scale for Developers Liberty Profile"
* Click on the "I confirm" button, review and agree to the license, click the "Download Now" link and save the resulting "wxs-wlp_8.6.0.8.jar" file.


* Install the "WebSphere eXtreme Scale for Developers Liberty Profile" file into the same directory as the Liberty runtime
```text
java -jar wxs-wlp_8.6.0.8.jar
```


 
WebSphere eXtreme Scale 8.6.0.8
* Download link:  http://www.ibm.com/developerworks/downloads/ws/wsdg
* Download the trial zip in a file called "extremescaletrial860.zip"
* Unzip this zip into a directory
* For the rest of these instructions we will assume this to be the WXS_SERVERDIR
Windows:
```text
cd \work\java
unzip extremescaletrial860.zip
cd \work\java\ObjectGrid
set WXS_SERVERDIR=C:\work\java\ObjectGrid
```
Linux:
```text
cd ~/work/java
unzip extremescaletrial860.zip
cd ObjectGrid
export WXS_SERVERDIR=~/work/java/ObjectGrid
```



## Build the Acme Air codebase to use the wxs service 

To communicate with WebSphere eXtreme Scale, Acme Air needs to be built using the wxs service specified. 


```text
./gradlew -Pservice=wxs clean build
```

Further details for building the Acme Air codebase can be found [here](Build_Instructions.md).


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


## Start Liberty and Load the database.

Follow the remaining steps in the [WebSphere Liberty instructions document](Liberty_Instructions.md).

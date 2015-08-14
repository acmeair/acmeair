# WebSphere Liberty Setup Instructions 

If you have not already done so, read through the [instructions for building the codebase](Build_Instructions.md) first. 


## Download the following free for developers or trial versions of WebSphere software

WebSphere Liberty 8.5.5.6
* Download link:  https://developer.ibm.com/wasdev/downloads/liberty-profile-using-non-eclipse-environments
* Click on the "Download Zip" for the "WAS Liberty with Java EE 7 Web Profile" image 
* Review and agree to the license, click the "Accept and download" button and save the resulting "wlp-webProfile7-8.5.5.6.zip" file.


* Install the WebSphere Liberty Profile Developers Runtime file into a directory of your choice
```text
unzip wlp-webProfile7-8.5.5.6.zip
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

## Choose a Data Service
The Acme Air sample application is able to utilize several types of data services. 
Choose one of the following -
* [WebSphere eXtreme Scale](Liberty_to_WXS_Instructions.md)
* [MongoDB](Liberty_to_Mongo_Instructions.md)



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



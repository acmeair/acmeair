## Instructions for building the Acme Air sample application 

## Add Java to your path and set your JAVA_HOME environment variable appropriately
Windows:
```text
set JAVA_HOME=C:\work\java\ibm-java-sdk-7.1-win-i386
set PATH=%JAVA_HOME%\bin;%PATH%
```

Linux:
```text
export JAVA_HOME=~/work/java/ibm-java-sdk-7.1-i386
export PATH=$JAVA_HOME/bin:$PATH
```

## Install the following development tools

* Git for access to the source code (http://msysgit.github.io/ for windows)
* Maven for building the project (http://maven.apache.org/download.cgi)
* Ensure git and mvn are in your path


## Download the following free for developers or trial versions of WebSphere software

 
WebSphere eXtreme Scale 8.6.0.x
The objectgrid.jar is needed in order to build the acmeair-services-wxs module. The following steps describe how to obtain a trial version of WebSphere eXtreme Scale, which contains the objectgrid.jar file. 
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

## Install the dependency that isn't available in Maven public repositories:
Windows:
```text
cd %WXS_SERVERDIR%\lib
```
Linux
```text
cd $WXS_SERVERDIR/lib
```

```text
mvn install:install-file -Dfile=objectgrid.jar -DgroupId=com.ibm.websphere.objectgrid -DartifactId=objectgrid -Dversion=8.6.0.2 -Dpackaging=jar
```


## Get the Acme Air codebase

* Go into a directory that you want to have the code in and use git to clone it
```text
cd work
git clone https://github.com/acmeair/acmeair.git
```

* For the rest of these instructions we will assume this to be the ACMEAIR_SRCDIR
Windows:
```text
set ACMEAIR_SRCDIR=\work\acmeair
cd %ACMEAIR_SRCDIR%
```

Linux:
```text
export ACMEAIR_SRCDIR=~/work/acmeair
cd $ACMEAIR_SRCDIR
```


## Choose the service you want & Build the Acme Air codebase

The Acme Air sample application can be configure to use different data services, including WebSphere eXtreme Scale and MongoDB.
During the build process, you may choose to  package either one of the services, or all of the services in to the application web archive (.war file).

To pick a single service use the "-Pservice=<serviceName>" option on the gradle command.   

For example, to only include the wxs service (located in the acmeair-services-wxs module) to use WebSphere eXtreme Scale, the build command would be as follows:

```text
./gradlew -Pservice=wxs clean build
```

To package ALL of the services in the .war file, the "-Pservice=" parameter can be omitted. 

```text
./gradle clean build
```



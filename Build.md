# How to build

## Build Prerequisites
- Java 17 
- Apache Maven 3.8.5

## Installing Java 17 on windows
- Download Zulu Java zip from: https://www.azul.com/downloads/?package=jdk
- Unpack the zip
- Edit path in powershell by doing:
```
$Env:PATH >> Env_Path.txt 
$Env:PATH = "C:\Users\myaccount\tools\zulu17.34.19-ca-jdk17.0.3-win_x64\bin;$Env:PATH"
```

The first command here just backs up current path. Second command needs to be pointed a the directory where you unpacked zulu jdk.

## Running the build on Windows with Powershell
Note, you may need to allow you current user to execute powershell scripts by running:
```
Set-ExecutionPolicy -Scope CurrentUser -ExecutionPolicy Unrestricted
```

The script will build all artifacts, including gwt component. Importantly for dev and test work, it will
build the vertx and openjfx fat jars because the required profiles are specified in the script. To check that these have
been refreshed correctly look at the following files and check their dates:
```
mongoose-base\mongoose-base-server-application-vertx\target\mongoose-base-server-application-vertx-1.0.0-SNAPSHOT-fat
mongoose-all/mongoose-all-backoffice-application-gwt/target/mongoose-all-backoffice-application-gwt-1.0.0-SNAPSHOT.war
```
The first file contains the server and back end in a single jar. The second contains the back office or admin UI in a single war.

## Creating and running Docker containers for testing


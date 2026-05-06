@echo off
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.4+7"
set "PATH=C:\Program Files\Eclipse Adoptium\jdk-21.0.4+7\bin;%PATH%"
cd /d D:\data\mmwiki-java
java -jar target\mmwiki-0.0.1-SNAPSHOT.jar > run-jar.log 2>&1

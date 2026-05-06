@echo off
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.4+7"
set "PATH=C:\Program Files\Eclipse Adoptium\jdk-21.0.4+7\bin;D:\Program Files\Maven\apache-maven-3.9.11\bin;%PATH%"
cd /d D:\data\mmwiki-java
mvn.cmd -s build-support\maven-settings.xml -gs build-support\maven-settings.xml spring-boot:run

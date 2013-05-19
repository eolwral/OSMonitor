Welcome to the OSMonitor wiki!

## Introduction ##
OS Monitor is a tool for collecting system information on Android OS.

It offers the following information.

- Processes - monitor all processes.
- Connections - display every network connection.
- Misc - monitor processors, network interfaces and file system.
- Messages - search dmesg or logcat in real-time. 

## How it works ##
OS Monitor runs a native execute binary to collecting data that will be transferred to Android UI App via IPC, the native binary calls "OSMCore" is wrote by C++. 

## What's New ? ##
The latest version has been rewrote, the whole architecture is totally changed, 1.x ~ 2.x always use JNI to receive data form C library, it causes some problems that like maintenance, 3.x try to solve issues and improve performance, it bases on new mechanism which introduces on the following section.

### IPC Mechanism ###

[[IPC.png]]

#### Architecture ####
- Communication between Android App and native process via Unix socket
- All requests or responses are serialized by Google Protocol Buffer
- Data could be reused if the request is equal in a short time 
- Maximum 8 connections at same time

#### Security ####
It bases on Unix Socket with a fixed name, if anyone knows the fixed name, he can easy hijack it with own app, a security token are used to prevent hijack, any connection need to offer a token for validating, otherwise, the connection will be disconnected.

#### Other ####
- All data are serialized and reusable.
- Expand and Upgrade is easy now.

### Question ###
If you have any question, please use <a href="https://github.com/eolwral/OSMonitor/issues">GitHub</a> to issue a ticket.
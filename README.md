# Prova Finale Ingegneria del Software 2020

## Gruppo AM10

- ### 10596841 Di Gennaro Marco ([@marcoDige](https://github.com/marcoDige)) marco1.digennaro@mail.polimi.it
- ### 10567849 De Bartolomeis Piersilvio ([@pierobartolo](https://github.com/pierobartolo)) piersilvio.debartolomeis@mail.polimi.it
- ### 10579929 Di Maio Alessandro ([@aledimaio](https://github.com/aledimaio)) alessandro1.dimaio@mail.polimi.it

| Functionality | State |
|:-----------------------|:------------------------------------:|
| Basic rules | [![GREEN](https://placehold.it/15/44bb44/44bb44)](#) |
| Complete rules | [![GREEN](https://placehold.it/15/44bb44/44bb44)](#) |
| Socket | [![GREEN](https://placehold.it/15/44bb44/44bb44)](#) |
| GUI | [![GREEN](https://placehold.it/15/44bb44/44bb44)](#) |
| CLI | [![GREEN](https://placehold.it/15/44bb44/44bb44)](#) |
| Multiple games | [![GREEN](https://placehold.it/15/44bb44/44bb44)](#) |
| Persistence | [![RED](https://placehold.it/15/f03c15/f03c15)](#) |
| Advanced Gods | [![GREEN](https://placehold.it/15/44bb44/44bb44)](#) |
| Undo | [![RED](https://placehold.it/15/f03c15/f03c15)](#) |

<!--
[![RED](https://placehold.it/15/f03c15/f03c15)](#)
[![YELLOW](https://placehold.it/15/ffdd00/ffdd00)](#)
[![GREEN](https://placehold.it/15/44bb44/44bb44)](#)
-->

## Instructions for build and execution:

The jar is the same both for server and client, built it under your preferred OS and launch it following the instructions below.
The project has been developed using JAVA 13 and javaFX 14. 
The following terminals have been tested for the cli: WSL (Ubuntu 18.04 and Debian), Windows Terminal and iTerm.

### Build instructions:

The jar is built using "Maven Shade Plugin", in IntelliJ IDE clone the repository and launch "install" under "lifecycle" section in Maven toolbar.
The generated jar will be placed in the "shade" folder in your project's root.

If you want use Maven in the terminal execute:
```
mvn install
```
In the project's root folder (same folder of the pom.xml).
The generated jar will be placed in the "shade" folder in your project's root.

### Execution instructions:

#### Server

In the terminal execute:

```
java -jar AM10.jar -server
```
The server will be execute listening on the default port ```1234```.

Execute in the terminal:

```
java -jar AM10.jar -server -PORT
```

Where instead of ```PORT``` put the port number where you want the server to listen. Example: ```java -jar AM10.jar -server -8080```

#### Client

To launch the program in the command line interface (cli) first make sure you have the terminal in fullscreen,
then execute in the terminal (opened in the same folder where the jar is):

```
java -jar AM10.jar -client -cli
```

To launch the program with the graphical user interface (gui) simple double-click on the jar (on Ubuntu 20.04 first make sure to mark as executable the jar file).
You can also execute in the terminal:

```
java -jar AM10.jar -client -gui
```
or simply:
```
java -jar AM10.jar
```
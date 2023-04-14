# Java Socket client-server 'War' card game

#### This is an implementation of the card game _War_ using Java Sockets. The rules to the card game can be found [here.](https://bicyclecards.com/how-to-play/war/)  

## Quick Start
Created using Java socket programming, this is a client-server application that can be played between two clients on the same network. To run, clone the repo, cd into the cloned directory and compile all Java files using:  
```javac *.java```  
Once compiled you need start the server first by running:  
```java Server```  
Once started, the server will ask you which port on the network you would like to run on. The default port is 1337. You can either play this game from two clients on the same machine, or from different machines on the same network. To start the client from the same machine you can start two instances of `Client` in two terminal/cmd instances by running:  
```java Client```   
If starting from another machine you will have to compile the code using the same steps as above. The IP address of the server will need to be known if playing from different computers.

## Implementation Details

### SocketConnection.java
This file encapsulates all the logic used to set up the `Socket` objects. This class contains methods to send and receive messages from the `Socket`. This file could potentially be used in other projects since it just sets up the sockets and communication methods.

### Server.java
This file contains the game logic, sets up the connections to, from and between the client sockets and manages game state.

### Client.java
This file connects to the server via a socket. User input is handled in this file and is used to send game event triggers to the server.
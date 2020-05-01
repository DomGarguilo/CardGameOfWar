# Java socket client-server card game

This is Java socket version of the card game war. The rules to the original card game can be found [here.](https://bicyclecards.com/how-to-play/war/)  

Created using Java socket programming, this is a client-server application that can be played between two clients on the same network. To run, cd into this directory and compile all java files using  
```javac *.java```  
Once compiled you need start the server first by using  
```java Server```  
Once started, the server will ask you which port on the network you would like to run on. The default port is 1337. You can either play this game from two clients on the same machine, or from different machines on the same network. To start the client from the same machine you can start two instances of client in two terminal/cmd windows using  
```java Client```   
If starting from another maching you will have to compile the code using the same steps as above. The IP address of the server will need to be known if playing from different computers.

# Java socket client-server card game

This is a command line version of the card game war. The rules to the original card game can be found [here.](https://bicyclecards.com/how-to-play/war/)  

Created using Java socket programming, this is a client-server application that can be played between two clients on the same network. To run, cd into this directory and compile all java files using  
```javac *.java```  
Once compiled you need start the server first by using  
```java Server```  
Once started, the server will ask you which port on the network you would like to run on. The default port is 1337 and that is what the client runs on. To change which port the client runs on you will need to edit the code in [*Client.java*](Client.java). You can either play this game from two clients on the same machine, or two machines on the same network. To start the client from the same machine you can start two instances of client in two terminal/cmd windows using  
```java Server```   
If starting from another maching you will have to compile the code using the same steps as above. Additionally, the game 'war' often entails many rounds. For those interested, there is a boolean flag "auto" in [*Client.java*](Client.java) that when set to true, will automatically flip your cards after the game is started, otherwise the user will be prompted each time a card needs to be flipped in the game.

# Java socket client-server card game

This is a command line version of the card game war. The rules to the original card game can be found [here.](https://bicyclecards.com/how-to-play/war/)

Created using Java socket programming, this is a client-server application that can be played between two clients on the same network. To run, cd into this directory and compile all java files using
```javac *.java```
Once compiled you can start the server using
```java Server```
You can either play this game from two clients on the same machine, or two machines on the same network. Once started, the server will ask you which port on the network you would like to run on. The default port is 1337 and that is what the client runs on. To change which port the client runs on you will need to edit the code in [*Client.java*](CardGameOfWar/Client.java).

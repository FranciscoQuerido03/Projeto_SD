#Compile instructions: <br>

javac -d . *.java
java -cp . .\HelloServer.java

#Files x-planation:

##Client.java -> Client program which requests for web content
###Functions:
-> Communicates with GateWay via RMI

##GateWay.java -> Component which stands in the middle of the client and the system responsable for acquiring the content.
###Functions:
-> Communicates with client via RMI
-> Communicates with index barrels via RMI
-> Adds urls to the queue(deque) when necessary

##Message.java -> Java class which has the data type necessary for Client <-> GateWay Comms.

##Request.java -> Interface for Client <-> Gateway Comms.

#Compile instructions:

javac -d . *.java<br>
java -cp . .\name.java<br>

#Files x-planation:<br>

##Client.java -> Client program which requests for web content<br>
###Functions:<br>
-> Communicates with GateWay via RMI<br>

##GateWay.java -> Component which stands in the middle of the client and the system responsable for acquiring the content.<br>
###Functions:<br>
-> Communicates with client via RMI<br>
-> Communicates with index barrels via RMI<br>
-> Adds urls to the queue(deque) when necessary<br>

##Message.java -> Java class which has the data type necessary for Client <-> GateWay Comms.<br>

##Request.java -> Interface for Client <-> Gateway Comms.<br>

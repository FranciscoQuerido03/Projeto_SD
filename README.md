Compile/Run instructions <br>

1) Navegar até à diretoria que contenha os ficheiros e executar o seguinte comando: <br>
javac -cp "path\jsoup-1.17.2.jar" -d . *.java , onde <br>

path = caminho até ao jar file do jsoup<br>

Exemplo:<br>
javac -cp "C:\Users\luism\Downloads\jsoup-1.17.2.jar" -d . *.java <br>

2) Executar a Queue: <br>
java -cp . .\Queue.java <br>

3) Executar GateWay e Downloaders: <br>

GateWay: java -cp . .\GateWay.java <br>
Downloaders: java -cp ".;<path>\jsoup-1.17.2.jar;." .\Downloader.java  (1 por terminal) <br>

4) Executar Barrels e Clientes: <br>

<id> = Número inteiro 

Barrels: java -cp . .\IndexBarrels.java <id> <br>
Clientes: java -cp . .\Client.java <br>

NOTA: Para o bom funcionamento da aplicação é importante a execução dos componentes seguir a ordem especificada.

package sd_projeto;


import java.util.Arrays;
import java.util.Scanner;
import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;

public class Client extends UnicastRemoteObject implements Client_I {

	public Client() throws RemoteException {
		super();
	}


	public void print_on_client(Urls_list list) throws java.rmi.RemoteException {
		System.out.println(list.toString());
	}

	public void print_err_2_client(Message erro) throws java.rmi.RemoteException{
		System.out.println(erro);
	}

	public static void main(String args[]) {

		/*
		System.getProperties().put("java.security.policy", "policy.all");
		System.setSecurityManager(new RMISecurityManager());
		*/

		try {
			Client c = new Client();

			Scanner scanner = new Scanner(System.in);
			Request Conection = (Request) Naming.lookup("rmi://localhost:1098/request");
			while(true){
				String str = scanner.nextLine();

				if(str.equals("\\close")){
					scanner.close();
					UnicastRemoteObject.unexportObject(c, true);
					break;
				}

				String[] parts = str.split(" ");
				String data = String.join(" ", Arrays.copyOfRange(parts, 1, parts.length));

				Message conteudo = new Message(data);



				switch (parts[0]) {
					case "index":
						System.out.println("Indexing " + conteudo + "...");
						Conection.send_request_queue(c, conteudo);
						break;
					case "search":
						System.out.println("Searching " + conteudo + "...");
						Conection.send_request_barrels(c, conteudo);
						break;
					default:
						System.out.println("Invalid command");
						break;
				}
				//System.out.println("Request sent");
			}

		} catch (RemoteException re) {
			System.out.println("Exception in GateWay.main: " + re);
		} catch (MalformedURLException e) {
			System.out.println("MalformedURLException in GateWay.main: " + e);
		} catch (NotBoundException e) {
			System.out.println("NotBoundException in GateWay.main: " + e);
		}

	}

}

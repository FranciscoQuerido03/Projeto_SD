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
		System.out.println(erro.toString());
	}

	public static void main(String args[]) {

		/*
		System.getProperties().put("java.security.policy", "policy.all");
		System.setSecurityManager(new RMISecurityManager());
		*/

		try {
			Client c = new Client();
			boolean keepItgoin = true;

			Scanner scanner = new Scanner(System.in);
			Request Conection = (Request) Naming.lookup("rmi://localhost:1098/request");
			while(keepItgoin){
				
				System.out.println("Syntax:");
				System.out.println("[1] search <search querry>");
				System.out.println("[2] index <url>");
				System.out.println("[3] stats");
				System.out.println("[4] \\close\n");

				String str = scanner.nextLine();

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
					case "stats":
						Message response = Conection.adm_painel();
						System.out.println("\n" + response.toString());
						break;
					case "\\close":
						System.out.println("Terminus");
						scanner.close();
						UnicastRemoteObject.unexportObject(c, true);
						keepItgoin = false;
						break;
					default:
						System.out.println("Invalid command");
						break;
				}
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

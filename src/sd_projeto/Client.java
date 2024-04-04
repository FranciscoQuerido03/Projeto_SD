package sd_projeto;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;

public class Client extends UnicastRemoteObject implements Client_I {

	private static String NAMING;

	public Client() throws RemoteException {
		super();
		File_Infos f = new File_Infos();
		f.get_data("Client");

		NAMING = f.lookup[0];
	}


	public void print_on_client(ArrayList<URL_Content> list) throws java.rmi.RemoteException {

		for (URL_Content urlContent : list) {
			System.out.println(urlContent.toString()); 
		}
			
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
			Request Conection = (Request) Naming.lookup(NAMING);
			while(keepItgoin){
				
				System.out.println("\nSelecione um opção:");
				System.out.println("[1] search <search querry>");
				System.out.println("[2] index <url>");
				System.out.println("[3] stats");
				System.out.println("[4] \\close\n");

				String str = scanner.nextLine();

				String[] parts = str.split(" ");
				String data = String.join(" ", Arrays.copyOfRange(parts, 1, parts.length));

				Message conteudo = new Message(data);

				switch (parts[0]) {
					case "1":
						System.out.println("\nSearching " + conteudo + "...\n");
						searchBarrels(c, Conection, conteudo);
						break;
					case "2":
						System.out.println("\nIndexing " + conteudo + "...\n");
						Conection.send_request_queue(c, conteudo);
						//Conection.send_request_barrels(c, conteudo);
						break;
					case "3":
						Message response = Conection.adm_painel();
						System.out.println("\n" + response.toString());
						break;
					case "4":
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

	private static void searchBarrels(Client c, Request conection, Message conteudo) {
		try {
			boolean end = false;
			int i = 0;
			Scanner scanner = new Scanner(System.in);
			while (!end) {
				conection.send_request_barrels(c, conteudo, i);
				System.out.println("[1] Previous 10\t[2] Next 10\t[3] End\n");
				String str = scanner.nextLine();
				switch (str) {
					case "1":
						i -= 1;
						break;
					case "2":
						i += 1;
						break;
					case "3":
						end = true;
						break;
					default:
						System.out.println("\nInvalid command\nTry again\n");
						break;
				}
			}

		} catch (RemoteException e) {
			System.out.println("RemoteException in GateWay.searchBarrels: " + e);
		}
	}

}

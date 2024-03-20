package sd_projeto;


import java.sql.SQLOutput;
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
				
				Message conteudo = new Message(str);

				Conection.send_request(c, conteudo);
				System.out.println("Request sent");
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

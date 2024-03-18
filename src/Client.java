package sd_projeto;

import java.util.Scanner;
import java.rmi.*;

public class Client {

	public static void main(String args[]) {

		/*
		System.getProperties().put("java.security.policy", "policy.all");
		System.setSecurityManager(new RMISecurityManager());
		*/

		try {
			Scanner scanner = new Scanner(System.in);
			Request Conection = (Request) Naming.lookup("rmi://localhost:1098/request");

            String str = scanner.nextLine();
			
			Message conteudo = new Message(str);

			Conection.send_request(conteudo);

		} catch (Exception e) {
			System.out.println("Exception in main: " + e);
		}

	}

}

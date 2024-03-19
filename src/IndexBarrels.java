package sd_projeto;

import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.*;
import java.util.ArrayList;
//import java.net.*;
import java.util.HashMap;
import java.util.List;

public class IndexBarrels extends UnicastRemoteObject implements Barrel_I {

	HashMap<String, List<String>> hashMap = new HashMap<>();
	public static Request Conection;
	
	public IndexBarrels() throws RemoteException {
		super();

		List<String> list1 = new ArrayList<>();
        list1.add("google");
        list1.add("wiki");
        list1.add("f-bomb");
        hashMap.put("Ola", list1);

        List<String> list2 = new ArrayList<>();
        list2.add("banks");
        list2.add("tedx");
        hashMap.put("Todos", list2);
	}

	public void request(String m) throws java.rmi.RemoteException {
		String[] words = m.split(" ");

		for (String word : words) {
            System.out.println(word);
			List<String> Vals = hashMap.get(word);
			Urls_list me = new Urls_list(Vals);
			Conection.answer(me);
		}
	}

	// =======================================================

	public static void main(String args[]) {

		try {
			Conection = (Request) Naming.lookup("rmi://localhost:1099/request_barrel");
			IndexBarrels h = new IndexBarrels();
			Conection.subscribe((Barrel_I) h);
			System.out.println("Barrel ready.");

		} catch (RemoteException re) {
			System.out.println("Exception in GateWay.main: " + re);
		} catch (MalformedURLException e) {
			System.out.println("MalformedURLException in GateWay.main: " + e);
		} catch (NotBoundException e) {
			System.out.println("NotBoundException in GateWay.main: " + e);
		}

	}

}
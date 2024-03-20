package sd_projeto;

import java.io.IOException;
import java.net.*;
import java.net.UnknownHostException;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.*;
import java.util.ArrayList;
//import java.net.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class IndexBarrels extends UnicastRemoteObject implements Barrel_I {

	static HashMap<String, List<String>> hashMap = new HashMap<>();
	public static Request Conection;

	private static String MULTICAST_ADDRESS = "224.3.2.1";
	private static int PORT = 4321;
	
	public IndexBarrels() throws RemoteException {
		super();

		List<String> list1 = new ArrayList<>();
        list1.add("Ola");
        list1.add("teste");
        list1.add("wtf");
        hashMap.put("google.com", list1);

        List<String> list2 = new ArrayList<>();
        list2.add("banks");
        list2.add("tedx");
        hashMap.put("tedx.com", list2);
	}

	public void request(String m) throws java.rmi.RemoteException {
		String[] words = m.split(" ");

		for (String word : words) {
			Urls_list urls = searchUrls(word);

			Conection.answer(urls);

		}
	}

	public Urls_list searchUrls(String palavra) {
		List<String> chavesEncontradas = new ArrayList<>();
		for (String chave : hashMap.keySet()) {
			List<String> lista = hashMap.get(chave);
			if (lista.contains(palavra)) {
				chavesEncontradas.add(chave);
			}
		}
        return new Urls_list(chavesEncontradas);
	}

	// =======================================================

	public static void main(String[] args) {
		MulticastSocket socket = null;

		try {
			Conection = (Request) Naming.lookup("rmi://localhost:1099/request_barrel");
			IndexBarrels h = new IndexBarrels();
			Conection.subscribe((Barrel_I) h);

			socket = new MulticastSocket(PORT); // create socket and bind it
			InetAddress mcastaddr = InetAddress.getByName(MULTICAST_ADDRESS);
			socket.joinGroup(new InetSocketAddress(mcastaddr, 0), NetworkInterface.getByIndex(0));

			System.out.println("Barrel ready.");

			while (true) {
				byte[] buffer = new byte[256];
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				socket.receive(packet);

				DealPacket(packet);
			}



		} catch (RemoteException re) {
			System.out.println("Exception in GateWay.main: " + re);
		} catch (MalformedURLException e) {
			System.out.println("MalformedURLException in GateWay.main: " + e);
		} catch (NotBoundException e) {
			System.out.println("NotBoundException in GateWay.main: " + e);
		} catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

	private static void DealPacket(DatagramPacket packet) {
		String message = new String(packet.getData(), 0, packet.getLength());
		String[] words = message.split(" ");

		String url = words[0];
        List<String> list = new ArrayList<>(Arrays.asList(words).subList(1, words.length));

		hashMap.put(url, list);
	}

}
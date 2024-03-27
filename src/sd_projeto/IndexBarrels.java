package sd_projeto;

import java.io.File;
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
	static HashMap<String, Integer> urls = new HashMap<>();
	static HashMap<String, Integer> links = new HashMap<>();

	public static Request Conection;
	private static String NAMING_URL= "";
	private static String MULTICAST_ADDRESS = "";
	private static int PORT = 0;
	
	public IndexBarrels() throws RemoteException {
		super();
	}

	public void request(String m) throws java.rmi.RemoteException {
		String[] words = m.split(" ");

//		for (String word : words) {
//			//Urls_list urls = searchUrls(word);
//
//			Conection.answer(urls);
//
//		}
	}

//	public Urls_list searchUrls(String palavra) {
////		List<String> chavesEncontradas = new ArrayList<>();
////		for (String chave : hashMap.keySet()) {
////			List<String> lista = hashMap.get(chave);
////			if (lista.contains(palavra)) {
////				chavesEncontradas.add(chave);
////			}
////		}
////        return new Urls_list(chavesEncontradas);
//	}

	// =======================================================

	public static void main(String[] args) {
		readFile();
		MulticastSocket socket = null;

		try {
			Conection = (Request) Naming.lookup(NAMING_URL);
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
				System.out.println("Received message: " + new String(packet.getData(), 0, packet.getLength()));

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

	private static void readFile() {
		File file = new File("src\\Index_config.txt");
		try {
			java.util.Scanner scanner = new java.util.Scanner(file);
			NAMING_URL = scanner.nextLine();
			MULTICAST_ADDRESS = scanner.nextLine();
			PORT = Integer.parseInt(scanner.nextLine());

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
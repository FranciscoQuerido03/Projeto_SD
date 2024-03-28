package sd_projeto;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.rmi.*;
import java.rmi.server.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class IndexBarrels extends UnicastRemoteObject implements Barrel_I {

	static HashMap<String, Integer> urls = new HashMap<>();
	static HashMap<String, int[]> words_HM = new HashMap<>();
	//static HashMap<String, Integer> links = new HashMap<>();

	public static Request Conection;
	private static String NAMING_URL= "rmi://localhost:1099/request_barrel";
	private static String MULTICAST_ADDRESS = "224.3.2.1";
	private static int PORT = 4321;
	
	public static int numBarrels;
	public int id;

	public IndexBarrels(int num) throws RemoteException {
		super();
		id = num;
	}

	public void Update_mem(boolean b, Barrel_I h) throws java.rmi.RemoteException {
		if(b){
			System.out.println("Yes");
			Conection.subscribe((Barrel_I) h);
		} else{
			System.out.println("No");
			Conection.subscribe((Barrel_I) h);
		}

		System.out.println("Barrel " + id + " up in service!");

	}

	public void request(String m) throws java.rmi.RemoteException {
		String[] words = m.split(" ");
		Urls_list not_found_words = new Urls_list(new ArrayList<>());
		Urls_list lista_final = new Urls_list(new ArrayList<>());
		System.out.println(id);

		for (String word : words) {
			System.out.println(word);
			int[] nums = words_HM.get(word);

			//System.out.println(lista_final);
			if(nums != null){
				for(int num : nums){
					for (Map.Entry<String, Integer> entry : urls.entrySet()) {
						if (entry.getValue() == num) {
							lista_final.addUrl(entry.getKey());
							break; 
						}
					}
				}
			}else
				not_found_words.addUrl(word);
		}

		if (lista_final.hasValues()) {
			if(not_found_words.hasValues())
				Conection.err_no_matches(new Message("No URLs found for: " + not_found_words.wordtoString()));
			Conection.answer(lista_final);
		} else {
			Conection.err_no_matches(new Message("No URLs found for the entire input: " + not_found_words.wordtoString()));
		}
	}

	/*
	 * 	<Debug Functions>
	 */

	public void printUrls() throws java.rmi.RemoteException {
        System.out.println("Urls:");
        for (Map.Entry<String, Integer> entry : urls.entrySet()) {
            System.out.println(entry.getKey() + " -> " + entry.getValue());
        }
        System.out.println();
    }

    public void printWordsHM() throws java.rmi.RemoteException {
        System.out.println("Words_HM:");
        for (Map.Entry<String, int[]> entry : words_HM.entrySet()) {
            System.out.print(entry.getKey() + " -> ");
            for (int urlNum : entry.getValue()) {
                System.out.print(urlNum + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

	/*
	 * 	<\Debug Functions>
	 */

	// =======================================================

	public static void main(String[] args) {
		//readFile();

		try {
			Conection = (Request) Naming.lookup(NAMING_URL);
			
			try{
				numBarrels = Integer.parseInt(args[0]);
			} catch (NumberFormatException | ArrayIndexOutOfBoundsException e){
				System.err.println("Falta o num de barrels cuh");
				System.exit(1);
			}

			for(int i = 0; i< numBarrels; i++){
				Thread barrel = new Thread(new Barrel_Function(i+1));
				barrel.start();
			}

			//System.out.println("Barrel ready.");


		} catch (RemoteException re) {
			System.out.println("Exception in GateWay.main: " + re);
		} catch (MalformedURLException e) {
			System.out.println("MalformedURLException in GateWay.main: " + e);
		} catch (NotBoundException e) {
			System.out.println("NotBoundException in GateWay.main: " + e);
		}

    }

	static class Barrel_Function implements Runnable {
		private final int barrel_id;
		private static int count_urls = 0;
		private static MulticastSocket socket = null;
		private IndexBarrels h;

		public Barrel_Function(int barrel_id){
			this.barrel_id = barrel_id;
		}

		@Override
		public void run(){

			System.out.println("Barrel " + barrel_id + " created!");

			try{
				h = new IndexBarrels(barrel_id);
				Conection.V_I((Barrel_I) h);						//Verifica se é necessário sincronizar com barrels existentes

				Runtime.getRuntime().addShutdownHook(new Thread(() -> {
					try {
						Conection.barrel_disconnect((Barrel_I) h);
						UnicastRemoteObject.unexportObject(h, true);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}));

				socket = new MulticastSocket(PORT); // create socket and bind it
				String modifiedAddress = MULTICAST_ADDRESS.substring(0, MULTICAST_ADDRESS.lastIndexOf(".") + 1) + barrel_id;
				InetAddress mcastaddr = InetAddress.getByName(modifiedAddress);
				socket.joinGroup(new InetSocketAddress(mcastaddr, 0), NetworkInterface.getByIndex(0));

				while (true) {
					byte[] buffer = new byte[256];
					DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
					socket.receive(packet);
					//System.out.println(barrel_id + " Received message: " + new String(packet.getData(), 0, packet.getLength()));

					DealPacket(packet);
				}

			} catch (IOException e) {
				try {
					Conection.barrel_disconnect((Barrel_I) h);
					UnicastRemoteObject.unexportObject(h, true);
				} catch (RemoteException e2) {
					e2.printStackTrace();
				}
				throw new RuntimeException(e);
			}
		}

		private static void DealPacket(DatagramPacket packet) {
			String message = new String(packet.getData(), 0, packet.getLength());
			String[] words = message.split(" ");
	
			String url = words[0];
			String[] list;
			if (words.length > 1 && words[1] != null) {
				list = words[1].split(" ");
			} else {
				list = new String[0];
			}

			int aux_url_num = 0;

			if(urls.get(url) == null){
				aux_url_num = count_urls;
				urls.put(url, count_urls++);

			}else{
				aux_url_num = urls.get(url);
			}

			for(String w : list){
				w = w.toLowerCase();
				int[] existingArray = words_HM.get(w);
				if(existingArray == null){
					existingArray = new int[1];
					existingArray[0] = aux_url_num;
					System.out.println(w);
					words_HM.put(w, existingArray);
				}else{
					int newArrayLength = existingArray.length + 1;
					int[] newArray = Arrays.copyOf(existingArray, newArrayLength);
					newArray[newArrayLength - 1] = aux_url_num;
					words_HM.put(w, newArray);
				}
			}
		}

	}

	private static void readFile() {
		File file = new File("C:\\Users\\luism\\Desktop\\Engenharia Informática\\3 ano 2 semestre\\S fucking D\\Projeto\\Projeto_SD\\SRC\\Index_config.txt");
		try {
			java.util.Scanner scanner = new java.util.Scanner(file);
			NAMING_URL = scanner.nextLine();
			MULTICAST_ADDRESS = scanner.nextLine();
			PORT = Integer.parseInt(scanner.nextLine());

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
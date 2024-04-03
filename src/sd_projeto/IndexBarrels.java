package sd_projeto;

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
	private static String NAMING_URL;
	private static String MULTICAST_ADDRESS;
	private static int PORT;
	

	public static int numBarrels;
	public static int id;

	public IndexBarrels(int num) throws RemoteException {
		super();
		id = num;
	}

	public void request(String m) throws java.rmi.RemoteException {
		String[] words = m.split(" ");
		Urls_list not_found_words = new Urls_list(new ArrayList<>());
		Urls_list lista_final = new Urls_list(new ArrayList<>());
		System.out.println("Request received: " + m);

		for (String word : words) {
			//System.out.println(word);
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
			File_Infos f = new File_Infos();
			f.get_data("Barrel");

			NAMING_URL = f.lookup[0];
			MULTICAST_ADDRESS = f.Address;
			PORT = f.Port;
			
			Conection = (Request) Naming.lookup(NAMING_URL);
			
			try{
				numBarrels = Integer.parseInt(args[0]);
				id = Integer.parseInt(args[1]);
			} catch (NumberFormatException | ArrayIndexOutOfBoundsException e){
				System.err.println("Falta o num de barrels cuh");
				System.exit(1);
			}

			for(int i = 0; i< numBarrels; i++){
				Thread barrel = new Thread(new Barrel_Function(id+i));
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

	static class Barrel_Send_HMs implements Runnable {

		private final int barrel_id;

		public Barrel_Send_HMs(int barrel_id){
			super();
			this.barrel_id = barrel_id;
		}

		@Override
		public void run(){
			try{
				Mc_HM_Content();
			} catch(RemoteException e){
				System.out.println("Erro");
			}

		}

		public void Mc_HM_Content() throws java.rmi.RemoteException {
			System.out.println("Synchorizing");
			
			send_mc_urls();
			send_mc_words();
	
		}
	
		private void send_mc_urls() {
			String content = "";
			MulticastSocket socket = null;
			byte[] buffer;
			DatagramPacket packet;
	
			try{
	
				synchronized(urls) {
			
					socket = new MulticastSocket(PORT);
					socket.setReuseAddress(true);
					InetAddress mcastaddr = InetAddress.getByName(MULTICAST_ADDRESS);
					socket.joinGroup(new InetSocketAddress(mcastaddr, 0), NetworkInterface.getByIndex(0));
		
		
					for (Map.Entry<String, Integer> entry : urls.entrySet()) {
						//System.out.println(entry.getKey() + " " + entry.getValue());
						Udp_Mc_Packet mc_packet = new Udp_Mc_Packet("Sync_url", entry.getKey() + " " + entry.getValue());
						buffer = mc_packet.toString().getBytes();
						packet = new DatagramPacket(buffer, buffer.length, mcastaddr, PORT);
						socket.send(packet);
					}
					
					Udp_Mc_Packet mc_packet_end = new Udp_Mc_Packet("Sync_url_end", content);
					buffer = mc_packet_end.toString().getBytes();
					DatagramPacket packet_end = new DatagramPacket(buffer, buffer.length, mcastaddr, PORT);
					socket.send(packet_end);

					socket.close();
				}
	
			} catch (IOException e) {
				e.printStackTrace();
			}
	
		}
	
		private void send_mc_words() {
			String content = "";
			MulticastSocket socket = null;
			byte[] buffer;
			DatagramPacket packet;
	
			try{
	
				synchronized(words_HM) {	
		
					socket = new MulticastSocket(PORT);
					socket.setReuseAddress(true);
					InetAddress mcastaddr = InetAddress.getByName(MULTICAST_ADDRESS);
					socket.joinGroup(new InetSocketAddress(mcastaddr, 0), NetworkInterface.getByIndex(0));
		
		
					for (Map.Entry<String, int[]> entry : words_HM.entrySet()) {
						//System.out.println(entry.getKey() + " " + entry.getValue());
						StringBuilder messageBuilder = new StringBuilder();
						messageBuilder.append(entry.getKey()).append(" ");
		
						int[] values = entry.getValue();
						for (int value : values) {
							messageBuilder.append(value).append(" ");
						}
		
						// Remove the last space
						messageBuilder.deleteCharAt(messageBuilder.length() - 1);
		
						Udp_Mc_Packet mc_packet = new Udp_Mc_Packet("Sync_word", messageBuilder.toString());
						buffer = mc_packet.toString().getBytes();
						packet = new DatagramPacket(buffer, buffer.length, mcastaddr, PORT);
						socket.send(packet);
					}
					
					Udp_Mc_Packet mc_packet_end = new Udp_Mc_Packet("Sync_word_end", content);
					buffer = mc_packet_end.toString().getBytes();
					DatagramPacket packet_end = new DatagramPacket(buffer, buffer.length, mcastaddr, PORT);
					socket.send(packet_end);
		
					socket.close();
				}
	
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	static class Barrel_Multicast_Sender implements Runnable {
		private final int barrel_id;

		public Barrel_Multicast_Sender(int barrel_id){
			super();
			this.barrel_id = barrel_id;
		}

		@Override
		public void run(){
			System.out.println("Sender Initialized!");
			try{
				MulticastSocket socket = new MulticastSocket(PORT);
				socket.setReuseAddress(true);
				InetAddress mcastaddr = InetAddress.getByName(MULTICAST_ADDRESS);
				socket.joinGroup(new InetSocketAddress(mcastaddr, 0), NetworkInterface.getByIndex(0));

				while(true){
					byte[] buffer = new byte[256*2];
					DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
					socket.receive(packet);
					String message = new String(packet.getData(), 0, packet.getLength()).trim();

					if(message.equals("Sync")){
						Barrel_Send_HMs barrel_send_hm = new Barrel_Send_HMs(this.barrel_id);
						Thread thread_send_hm = new Thread(barrel_send_hm);
						thread_send_hm.start();
					}
				}

			} catch(IOException e){
				System.out.println("Erro");
			}
		}
	}

	static class Barrel_Multicast_Receiver implements Runnable {
		private final int barrel_id;

		public Barrel_Multicast_Receiver(int barrel_id){
			super();
			this.barrel_id = barrel_id;
		}

		@Override
		public void run(){
			System.out.println("Receiver Initialized!");

			receive_mc();

		}

		private void receive_mc() {
			boolean check = true;
			try {
				MulticastSocket socket = new MulticastSocket(PORT); // create socket and bind it
				socket.setReuseAddress(true);
				InetAddress mcastaddr = InetAddress.getByName(MULTICAST_ADDRESS);
				socket.joinGroup(new InetSocketAddress(mcastaddr, 0), NetworkInterface.getByIndex(0));
	
				while(check){

					byte[] buffer = new byte[256*2];
					DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
					socket.receive(packet);
					String message = new String(packet.getData(), 0, packet.getLength()).trim();
					String sections[] = message.split(" ");

					if(sections[0].equals("Sync_url"))
						Update_url_HM(message);
					if(sections[0].equals("Sync_word"))
						Update_word_HM(message);
	
					//System.out.println(message);
				}
	
	
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	
		public void Update_word_HM(String m) {
			String[] sections = m.split(" ");
			//System.out.println(m);
	
			if(sections.length >= 3){
				String word = sections[1];
				int[] nums_add = new int[sections.length-2];
	
				for(int i = 2; i<sections.length; i++){
					nums_add[i-2] = Integer.parseInt(sections[i]);
				}

				synchronized(words_HM){
					if (!words_HM.containsKey(word) || !Arrays.equals(words_HM.get(word), nums_add))
						words_HM.put(word, nums_add);
				}
	
			} else {
				System.err.println("Incorrect input format: " + m);
			}
		}
	
		private void Update_url_HM(String m) {
			String[] sections = m.split(" ");
			//System.out.println(m);
			//System.out.println(sections[2]);
			
			if (sections.length >= 3) {
				String url = sections[1];
				int count = Integer.parseInt(sections[2]);

				synchronized(urls){
					if(!urls.containsKey(url))
						urls.put(url, count);
				}
	
			} else {
				System.err.println("Incorrect input format: " + m);
			}
		}
	}

	static class Barrel_Function implements Runnable {
		private final int barrel_id;
		private static int count_urls = 0;
		private static MulticastSocket socket = null;
		private IndexBarrels h;

		public Barrel_Function(int barrel_id){
			super();
			this.barrel_id = barrel_id;
		}

		@Override
		public void run(){

			System.out.println("Barrel " + barrel_id + " created!");

			try{
				h = new IndexBarrels(barrel_id);

				Barrel_Multicast_Sender barrel_sender = new Barrel_Multicast_Sender(barrel_id);
				Thread thread_sender = new Thread(barrel_sender);
				thread_sender.start();

				Barrel_Multicast_Receiver barrel_receiver = new Barrel_Multicast_Receiver(barrel_id);
				Thread thread_receiver = new Thread(barrel_receiver);
				thread_receiver.start();

				socket = new MulticastSocket(PORT); // create socket and bind it
				InetAddress mcastaddr = InetAddress.getByName(MULTICAST_ADDRESS);
				socket.joinGroup(new InetSocketAddress(mcastaddr, 0), NetworkInterface.getByIndex(0));

				Conection.subscribe((Barrel_I) h, barrel_id);

				Runtime.getRuntime().addShutdownHook(new Thread(() -> {
					try {
						Conection.barrel_disconnect((Barrel_I) h);
						UnicastRemoteObject.unexportObject(h, true);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}));

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
			System.out.println(message);
			String[] words = message.split("\n");
			System.out.println(words[0]);
			if(words[0].equals("Data")){
				String url = words[1].split(" ")[1];
				String title = words[2].substring(words[2].indexOf(":") + 2);
				String publicationDate = words[3].substring(words[3].indexOf(":") + 2);
				String tokens = words[4].substring(words[4].indexOf(":") + 2);
				String url_a = words[5].substring(words[5].indexOf(":") + 2);

				System.out.println(url);
				System.out.println(title);
				System.out.println(publicationDate);
				System.out.println(tokens);
				System.out.println(url_a);

				String[] list;
				if (words.length > 2 && words[2] != null) {
					list = words[2].split(" ");
				} else {
					list = new String[0];
				}

				int aux_url_num = 0;

				if(urls.get(url) == null){			//Se o URL ainda nao existe na HM
					aux_url_num = count_urls;		//Guardamos o num equivalente dele
					urls.put(url, count_urls++);	//Adiciona na HM

				}else{								//Se ja existe
					aux_url_num = urls.get(url);	//vamos buscar o int associado
				}

				for(String w : list){		//Para cada palavra que o utilizador introduziu
					w = w.toLowerCase();
					int[] existingArray = words_HM.get(w);
					if(existingArray == null){				// Se a palavra ainda nao existe na HM
						existingArray = new int[1];
						existingArray[0] = aux_url_num;
						//System.out.println(w);
						words_HM.put(w, existingArray);
					}else{									// Se ja existe
						if(!check(existingArray, aux_url_num)){			// Se o URL ainda nao esta associado a palavra em questa
							int newArrayLength = existingArray.length + 1;
							int[] newArray = Arrays.copyOf(existingArray, newArrayLength);
							newArray[newArrayLength - 1] = aux_url_num;
							words_HM.put(w, newArray);
						}
					}
				}
			}
		}

		private static boolean check(int[] nums, int num) {
			for(int i = 0; i<nums.length; i++)
				if(nums[i] == num)
					return true;

			return false;
		}

	}
}
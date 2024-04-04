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

	static HashMap<URL_Content, Integer> urls = new HashMap<>();
	static HashMap<String, int[]> words_HM = new HashMap<>();
	static HashMap<Integer, int[]> links = new HashMap<>();

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

	public void request(String m, int min) throws java.rmi.RemoteException {
		String[] words = m.split(" ");
		Urls_list not_found_words = new Urls_list(new ArrayList<>());
		ArrayList<URL_Content> resultado = new ArrayList<>();
		System.out.println("Request received: " + m);

		int count = 0; // contador para controlar o número de URLs adicionadas

		//printWordsHM();

		for (String word : words) {								// Cada palavra das palavras de pesquisa
			int[] nums = words_HM.get(word);					// Buscar array de ints que correspondem aos urls que a palavra esta associada

			if(nums != null){									// Nums?
				for(int num : nums){							// Para cada num
					for (Map.Entry<URL_Content, Integer> entry : urls.entrySet()) {	
						if (entry.getValue() == num) {			// Se o value corresponder entao este url ta associado a palavra

							for (Map.Entry<Integer, int[]> entry_l : links.entrySet()) {	// Ver quantos url apontam para este url
								if(entry_l.getKey() == num){
									entry.getKey().priority = entry_l.getValue().length;	
									resultado.add(entry.getKey());
									break;
								}
							}

                        	break;
						}
					}
				}
			} else {
				not_found_words.addUrl(word);
			}
		}
		

		if (!resultado.isEmpty()) {
			if(not_found_words.hasValues())
				Conection.err_no_matches(new Message("No URLs found for: " + not_found_words.wordtoString()));
			Conection.answer(resultado);
		} else {
			Conection.err_no_matches(new Message("No URLs found for the entire input: " + not_found_words.wordtoString()));
		}
	}


	/*
	 * 	<Debug Functions>
	 */

	public static void printUrls() {
        System.out.println("\nUrls:\n");
        for (Map.Entry<URL_Content, Integer> entry : urls.entrySet()) {
            System.out.println("Title: " + entry.getKey().title);
			System.out.println("URL: " + entry.getKey().url);
			System.out.println("PUB_DATE: " + entry.getKey().Pub_date);
			System.out.println("Value: " + entry.getValue());
        }
        System.out.println();
    }

    public static void printWordsHM() {
        System.out.println("\nWords_HM:\n");
        for (Map.Entry<String, int[]> entry : words_HM.entrySet()) {
            System.out.print(entry.getKey() + " -> ");
            for (int urlNum : entry.getValue()) {
                System.out.print(urlNum + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

	public static void printLinks() {
		System.out.println("\nLINKS:\n");
        for (Map.Entry<Integer, int[]> entry : links.entrySet()) {
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
			send_mc_links();
	
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
		
		
					for (Map.Entry<URL_Content, Integer> entry : urls.entrySet()) {
						//System.out.println(entry.getKey() + " " + entry.getValue());
						Udp_Mc_Packet mc_packet = new Udp_Mc_Packet("Sync_url", entry.getKey().toString() + entry.getValue());
						//System.out.println(mc_packet.toString());
						buffer = mc_packet.toString().getBytes();
						packet = new DatagramPacket(buffer, buffer.length, mcastaddr, PORT);
						socket.send(packet);
					}

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
		
					socket.close();
				}
	
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void send_mc_links() {
			String content = "";
			MulticastSocket socket = null;
			byte[] buffer;
			DatagramPacket packet;
	
			try{
	
				synchronized(links) {	
		
					socket = new MulticastSocket(PORT);
					socket.setReuseAddress(true);
					InetAddress mcastaddr = InetAddress.getByName(MULTICAST_ADDRESS);
					socket.joinGroup(new InetSocketAddress(mcastaddr, 0), NetworkInterface.getByIndex(0));
		
		
					for (Map.Entry<Integer, int[]> entry : links.entrySet()) {
						//System.out.println(entry.getKey() + " " + entry.getValue());
						StringBuilder messageBuilder = new StringBuilder();
						messageBuilder.append(entry.getKey()).append(" ");
		
						int[] values = entry.getValue();
						for (int value : values) {
							messageBuilder.append(value).append(" ");
						}
		
						// Remove the last space
						messageBuilder.deleteCharAt(messageBuilder.length() - 1);
		
						Udp_Mc_Packet mc_packet = new Udp_Mc_Packet("Sync_link", messageBuilder.toString());
						buffer = mc_packet.toString().getBytes();
						packet = new DatagramPacket(buffer, buffer.length, mcastaddr, PORT);
						socket.send(packet);
					}
		
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

					byte[] buffer = new byte[256*4];
					DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
					socket.receive(packet);
					String message = new String(packet.getData(), 0, packet.getLength()).trim();
					String sections[] = message.split("\n");

					if(sections[0].equals("Sync_url"))
						Update_url_HM(message);
					if(sections[0].equals("Sync_word"))
						Update_word_HM(message);
					if(sections[0].equals("Sync_link"))
						Update_link_HM(message);
					//System.out.println(message);
				}
	
	
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	
		public void Update_word_HM(String m) {
			String[] aux = m.split("\n");
			String[] sections = aux[1].split(" ");
			//System.out.println("word\n" + m);

			if(sections.length >= 3){
				String word = sections[0];
				int[] nums_add = new int[sections.length-1];
	
				for(int i = 1; i<sections.length; i++){
					nums_add[i-1] = Integer.parseInt(sections[i]);
				}

				synchronized(words_HM){
					if (!words_HM.containsKey(word) || !Arrays.equals(words_HM.get(word), nums_add))
						words_HM.put(word, nums_add);
				}
	
			} else {
				System.err.println("Incorrect input format: " + m);
			}
		}

		public void Update_link_HM(String m) {
			String[] aux = m.split("\n");
			String[] sections = aux[1].split(" ");
			//System.out.println("link\n" + m);
	
			if(sections.length >= 2){
				int num = Integer.parseInt(sections[0]);
				int[] nums_add = new int[sections.length-1];
	
				for(int i = 1; i<sections.length; i++){
					nums_add[i-1] = Integer.parseInt(sections[i]);
				}

				synchronized(links){
					if (!links.containsKey(num) || !Arrays.equals(links.get(num), nums_add))
						links.put(num, nums_add);
				}
	
			} else {
				System.err.println("Incorrect input format: " + m);
			}
		}
		
		private void Update_url_HM(String m) {
			String[] sections = m.split("\n");
			URL_Content u;
			//System.out.println("url\n" + m);
			
			if (sections.length >= 3) {
				String title = sections[1].substring(sections[1].indexOf(":") + 2);
				String url = sections[2].substring(sections[2].indexOf(":") + 2);
				String Pub_date = sections[3].substring(sections[3].indexOf(":") + 2);
				u = new URL_Content(title, url, Pub_date);
				int count = Integer.parseInt(sections[4]);
				
				System.out.println(Pub_date);

				synchronized(urls){
					if(!urls.containsKey(u))
						urls.put(u, count);
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
					byte[] buffer = new byte[4096 * 4];
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
			
			//System.out.println(message);
			String[] words = message.split("\n");

			if(words[0].equals("Data")){

				try{

					String url = words[1].split(" ")[1];
					String title = words[2].substring(words[2].indexOf(":") + 2);
					String publicationDate = words[3].substring(words[3].indexOf(":") + 2);

					socket.receive(packet);
					message = new String(packet.getData(), 0, packet.getLength());

					String tokens = message.substring(message.indexOf(":") + 2);
					String list[] = tokens.split(" ");

					socket.receive(packet);
					//System.out.println(packet.getLength());
					message = new String(packet.getData(), 0, packet.getLength());

					String url_a = message.substring(message.indexOf(":") + 2);
					String list_url_a[] = url_a.split(" ");


					URL_Content new_url = new URL_Content(title, url, publicationDate);

					//System.out.println(tokens);
					//System.out.println(url_a);

					int aux_url_num = 0;
					URL_Content aux;

					synchronized(urls){
					
						if((aux = searchByUrl(url)) == null){			//Se o URL ainda nao existe na HM
							aux_url_num = count_urls;			//Guardamos o num equivalente dele
							urls.put(new_url, count_urls++);	//Adiciona na HM

						}else{									//Se ja existe
							aux_url_num = get_num(new_url.url);	//vamos buscar o int associado
							if(aux.title == null) aux.title = title;
							if(aux.Pub_date == null) aux.Pub_date = publicationDate;
							urls.put(aux, aux_url_num);
						}
					}

					synchronized(words_HM){
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

					int aux_url_num2;								// NUM do link da lista de links

					synchronized(links){
						for(String w : list_url_a){
							if(searchByUrl(w) == null){					//Se o URL ainda nao existe na HM
								aux_url_num2 = count_urls;				//Guardamos o num equivalente dele
								new_url = new URL_Content(null, w, null);
								urls.put(new_url, count_urls++);		//Adiciona na HM
		
							}else{										//Se ja existe
								aux_url_num2 = get_num(w);				//vamos buscar o int associado
							}

							int[] existingArray = links.get(aux_url_num2);

							if(existingArray == null){
								existingArray = new int[1];
								existingArray[0] = aux_url_num;
								links.put(aux_url_num2, existingArray);
							} else {
								if(!check(existingArray, aux_url_num)){			
									int newArrayLength = existingArray.length + 1;
									int[] newArray = Arrays.copyOf(existingArray, newArrayLength);
									newArray[newArrayLength - 1] = aux_url_num;
									words_HM.put(w, newArray);
								}
							}
						}
					}

					//System.out.println("\n==========================\n");
					//printUrls();
					//printWordsHM();
					//printLinks();
					//System.out.println("\n==========================\n");

				} catch ( IOException e){
					System.out.println("Erro");
				}

			}
		}

		public static URL_Content searchByUrl(String url) {
			synchronized(urls){
				for (Map.Entry<URL_Content, Integer> entry : urls.entrySet()) {
					URL_Content urlContent = entry.getKey();
					if (urlContent.url.equals(url)) {
						return urlContent;
					}
				}
				return null; // URL not found
			}
		}

		public static int get_num(String url) {
			for (Map.Entry<URL_Content, Integer> entry : urls.entrySet()) {
				URL_Content urlContent = entry.getKey();
				if (urlContent.url.equals(url)) {
					return entry.getValue();
				}
			}
			return -1; // NUM not found
		}

		private static boolean check(int[] nums, int num) {
			for(int i = 0; i<nums.length; i++)
				if(nums[i] == num)
					return true;

			return false;
		}

	}
}
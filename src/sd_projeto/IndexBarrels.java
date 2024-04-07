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

	public void request(String m) throws java.rmi.RemoteException {
		String[] words = m.split(" ");
		ArrayList<URL_Content> resultado = new ArrayList<>();
		System.out.println("Request received: " + m);

		HashMap<Integer, Integer> occurrences = new HashMap<>();

		for(String word : words){
			int[] nums = words_HM.get(word);
			if(nums != null){
				for(int i : nums){
					occurrences.put(i, occurrences.getOrDefault(i, 0) + 1);
				}
			}
		}
		
		//printWordsHM();

		for (Map.Entry<Integer, Integer> entry : occurrences.entrySet()) {
			if(entry.getValue() == words.length){
				for (Map.Entry<URL_Content, Integer> entry_url : urls.entrySet()) {
					if (entry_url.getValue() == entry.getKey()) {			// Se o value corresponder entao este url ta associado a palavra

						for (Map.Entry<Integer, int[]> entry_l : links.entrySet()) {	// Ver quantos url apontam para este url
							if(entry_l.getKey() == entry.getKey()){
								entry_url.getKey().priority = entry_l.getValue().length;
								resultado.add(entry_url.getKey());
								break;
							}
						}

						break;
					}
				}
			}
		}
		/*
		try{
			Thread.sleep(1000);
		} catch (InterruptedException e){
			System.out.println("Erro");
		}
		 */
		if (!resultado.isEmpty()) {
			Conection.answer(resultado);
		} else {
			Conection.err_no_matches(new Message("No URLs found for the input: " + m));
			Conection.answer(resultado);
		}
	}

	@Override
	public void links_pointing_to(String clientRequest) throws RemoteException {

		int valorRequest = -1;
		for (Map.Entry<URL_Content, Integer> entry : urls.entrySet()) {
			URL_Content urlContent = entry.getKey();
			if (urlContent.url.equals(clientRequest)) {
				valorRequest = entry.getValue();
				break;
			}
		}

		if (valorRequest == -1) {
			Conection.err_no_matches(new Message("The URL " + clientRequest + " was not found in the database. Try indexing it first.\n"));
			return;
		}

		int[] listaInts = links.get(valorRequest);
		ArrayList<URL_Content> urlsPointingTo = new ArrayList<>();
		if (listaInts != null) {
			for (int i : listaInts) {
				for (Map.Entry<URL_Content, Integer> entry : urls.entrySet()) {
					if (entry.getValue() == i) {
						urlsPointingTo.add(entry.getKey());
					}
				}
			}
		}
		Conection.answer_pointers(urlsPointingTo);
	}


	/*
	 * 	<Debug Functions>
	 */

	public static void printUrls() {
		System.out.println("\nUrls:\n");
		for (Map.Entry<URL_Content, Integer> entry : urls.entrySet()) {
			System.out.println("Title: " + entry.getKey().title);
			System.out.println("URL: " + entry.getKey().url);
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

		try {
			File_Infos f = new File_Infos();
			f.get_data("Barrel");

			NAMING_URL = f.lookup[0];
			MULTICAST_ADDRESS = f.Address;
			PORT = f.Port;

			Conection = (Request) Naming.lookup(NAMING_URL);

			try{
				id = Integer.parseInt(args[0]);
			} catch (NumberFormatException | ArrayIndexOutOfBoundsException e){
				System.err.println("Falta o id do barrel cuh");
				System.exit(1);
			}

			if(Conection.can_join()){
				Thread barrel = new Thread(new Barrel_Function(id));
				barrel.start();		
			}else{
				System.out.println("No more Barrels allowed");
			}

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
			
			try{
				MulticastSocket socket = new MulticastSocket(PORT);
				socket.setReuseAddress(true);
				InetAddress mcastaddr = InetAddress.getByName(MULTICAST_ADDRESS);
				socket.joinGroup(new InetSocketAddress(mcastaddr, 0), NetworkInterface.getByIndex(0));

				Runtime.getRuntime().addShutdownHook(new Thread(() -> {
					if (socket != null) 
						socket.close();
					
					System.out.println("Synchorizing terminated by force");
				}));

				send_mc_urls(socket, mcastaddr);
				send_mc_words(socket, mcastaddr);
				send_mc_links(socket, mcastaddr);

				socket.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void send_mc_urls(MulticastSocket socket, InetAddress mcastaddr) {
			byte[] buffer;
			DatagramPacket packet;

			try{

				synchronized(urls) {

					for (Map.Entry<URL_Content, Integer> entry : urls.entrySet()) {
						//System.out.println(entry.getKey() + " " + entry.getValue());
						Udp_Mc_Packet mc_packet = new Udp_Mc_Packet("Sync_url", entry.getKey().toString() + entry.getValue());
						//System.out.println(mc_packet.toString());
						buffer = mc_packet.toString().getBytes();
						packet = new DatagramPacket(buffer, buffer.length, mcastaddr, PORT);
						socket.send(packet);
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		private void send_mc_words(MulticastSocket socket, InetAddress mcastaddr) {
			byte[] buffer;
			DatagramPacket packet;

			try{

				synchronized(words_HM) {

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
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void send_mc_links(MulticastSocket socket, InetAddress mcastaddr) {
			byte[] buffer;
			DatagramPacket packet;

			try{

				synchronized(links) {

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

				Runtime.getRuntime().addShutdownHook(new Thread(() -> {
					System.out.println("Sender terminated");
				}));

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
				System.out.println("Erro no Sender");
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

				Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			
					System.out.println("Receiver terminated");
				}));

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
			String cit = new String();
			//System.out.println("url\n" + m);

			if (sections.length >= 3) {
				String title = sections[1].substring(sections[1].indexOf(":") + 2);
				String url = sections[2].substring(sections[2].indexOf(":") + 2);
				if(sections.length >= 4)
					cit = sections[3];
				u = new URL_Content(title, url);
				u.add_citacao(cit);
				int count = Integer.parseInt(sections[4]);

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
						System.out.println("Barrel " + barrel_id + " terminated!");
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}));

				while (true) {
					byte[] buffer = new byte[4096 * 4];
					DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
					socket.receive(packet);
				
					String receivedMessage = new String(packet.getData(), 0, packet.getLength());
					String sections[] = receivedMessage.split(" ");
					//System.out.println(sections[0]);
					if (sections[0].equals("Data_New:")) {
						Thread packetHandlerThread = new Thread(() -> DealPacket(sections[1]));
						packetHandlerThread.start();
					}
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

		private static void DealPacket(String url) {
			//System.out.println(message);
			//String[] words = message.split("\n");
			boolean keep = true;
			URL_Content new_url;
			int num_aux = 0;
			int flag = 0;

			try {

				MulticastSocket newSocket = new MulticastSocket(PORT);
				newSocket.setReuseAddress(true);
				InetAddress mcastaddr = InetAddress.getByName(MULTICAST_ADDRESS);
				newSocket.joinGroup(new InetSocketAddress(mcastaddr, 0), NetworkInterface.getByIndex(0));

				Runtime.getRuntime().addShutdownHook(new Thread(() -> {
					System.out.println("Deal Packet terminated");
				}));

				while(keep){
				
					byte[] buffer = new byte[4096];
					DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
					newSocket.receive(packet);
					String message = new String(packet.getData(), 0, packet.getLength());
					String sections[] = message.split("\n");

					//System.out.println(message);

					if(sections[0].equals("Data: " + url)){

						if(sections[1].split(" ")[0].equals("Title:")){
							new_url = new URL_Content(sections[1].substring("Title: ".length()), url);
							num_aux = insert_url(new_url);
						}

						if(sections[1].split(" ")[0].equals("Text:")){
							String textContent = sections[1].substring("Text: ".length());

							if(flag == 0){												// Buscar as primeiras 10 palavras para a citaçao
								String[] parts = sections[1].split(" ");
								StringBuilder resultBuilder = new StringBuilder();
								for (int i = 1; i <= 10 && i < parts.length; i++) {
									resultBuilder.append(parts[i]);
									if (i < 10 && i < parts.length - 1) {
										resultBuilder.append(" ");
									}
								}
								resultBuilder.deleteCharAt(resultBuilder.length() - 1);
								URL_Content aux = searchByUrl(url);
								aux.add_citacao(resultBuilder.toString());
								flag = 1;
							}

							String[] list = textContent.split("\\s+");
							insert_words(list, num_aux);
						}

						if(sections[1].split(" ")[0].equals("Links:")){
							String textContent = sections[1].substring("Links: ".length());
							String[] list = textContent.split("\\s+");
							insert_links(list, num_aux);
						}

						if(sections[1].equals("END")){
							keep = false;
						}
					}
					
				}
				System.out.println("Exiting");
				newSocket.close();
				
			} catch (IOException e){
				System.out.println("An error occurred: " + e.getMessage());
    			e.printStackTrace(); // This line prints the stack trace of the exception
			}
			
			//System.out.println("\n==========================\n");
			//printUrls();
			//printWordsHM();
			//printLinks();
			//System.out.println("\n==========================\n");
		}

		public static void insert_links(String list_url_a[], int aux_url_num) {
			URL_Content new_url;
			int aux_url_num2;

			synchronized(links){
				for(String w : list_url_a){
					if(searchByUrl(w) == null){					//Se o URL ainda nao existe na HM
						aux_url_num2 = count_urls;				//Guardamos o num equivalente dele
						new_url = new URL_Content(null, w);
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

		}

		public static void insert_words(String list[], int aux_url_num) {
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
		}

		public static int insert_url(URL_Content new_url) {
			URL_Content aux;
			int aux_url_num;

			synchronized(urls){
					
				if((aux = searchByUrl(new_url.url)) == null){			//Se o URL ainda nao existe na HM
					aux_url_num = count_urls;			//Guardamos o num equivalente dele
					urls.put(new_url, count_urls++);	//Adiciona na HM

				}else{									//Se ja existe
					aux_url_num = get_num(new_url.url);	//vamos buscar o int associado
					if(aux.title == null) aux.title = new_url.title;
					urls.put(aux, aux_url_num);
				}
			}

			return aux_url_num;
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
package sd_projeto;

import java.awt.*;
import java.io.IOException;
import java.net.*;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Classe que representa o Gateway do motor de busca.
 * O Gateway coordena as solicitações entre o cliente e os barrels ou a queue.
 */
public class GateWay extends UnicastRemoteObject implements Request {

	private static final String Erro_Indisponibilidade = "Service unavailable due to internal problems...";

	public static String client_request;
	static Barrel_struct barrels[];
	static TopSearches top_searches;
	static int count = 0;
	static int lb = -1; // Último barril; essa técnica mudará eventualmente
	static int NUM_BARRELS;
	static Client_I client;
	private static String MULTICAST_ADDRESS;
	private static int PORT;
	private static QueueInterface queue;
	private static ReentrantLock lock = new ReentrantLock();

	private static HashMap<Client_I, ArrayList<URL_Content>> results10 = new HashMap<>();

	/**
	 * Construtor para criar o Gateway.
	 * @param f As informações do arquivo de configuração.
	 * @throws RemoteException se ocorrer um erro durante a inicialização do objeto remoto.
	 * @throws MalformedURLException se ocorrer um erro na formatação do URL.
	 * @throws NotBoundException se o objeto remoto não estiver vinculado.
	 */
	public GateWay(File_Infos f) throws RemoteException, MalformedURLException, NotBoundException {
		super();
		NUM_BARRELS = f.NUM_BARRELS;
		barrels = new Barrel_struct[NUM_BARRELS];
		Barrel_struct.initialize(barrels, NUM_BARRELS);
		top_searches = new TopSearches();
		queue = (QueueInterface) Naming.lookup(f.lookup[0]);
	}

	/**
	 * Método para desconectar um barrel.
	 * @param barrel O barrel a ser desconectado.
	 * @throws RemoteException se ocorrer um erro durante a comunicação remota.
	 */
	public void barrel_disconnect(Barrel_I barrel) throws RemoteException {
		lock.lock();
		try {
			count = Barrel_struct.remove_barrel(barrels, barrel, count);
			if (count <= 0)
				lb = -1;
		} finally {
			lock.unlock();
		}
	}


	/**
	 * ??????????????????????????????????????????
	 * @param s A mensagem de erro.
	 * @throws RemoteException se ocorrer um erro durante a comunicação remota.
	 */
	public void err_no_matches(Message s) throws RemoteException {
		client.print_err_2_client(s);
	}

	/**
	 * Método para enviar solicitações aos barrels remotos.
	 * @param c O cliente.
	 * @param m A mensagem com a solicitação.
	 * @throws RemoteException se ocorrer um erro durante a comunicação remota.
	 */
	public void send_request_barrels(Client_I c, Message m) throws RemoteException {
		lock.lock();
		try {
			System.out.println("GateWay: " + m.toString() + " " + count);
			client = c;
			client_request = m.toString().trim();
			top_searches.updateSearchCount(client_request);
			if (lb >= 0) {
				if (lb >= count)
					lb = 0;
				System.out.println("lb " + lb);
				long inicio_pedido = System.currentTimeMillis();
				barrels[lb].barrel.request(client_request.toLowerCase());
				long fim_pedido = System.currentTimeMillis();
				barrels[lb].avg_time = (barrels[lb].avg_time + ((fim_pedido - inicio_pedido) / 100)) / 2;
				lb++;
			} else {
				client.print_err_2_client(new Message(Erro_Indisponibilidade));
			}
		} finally {
			lock.unlock();
		}
	}


	/**
	 * Método para enviar solicitações à queue.
	 * @param c O cliente.
	 * @param m A mensagem com a solicitação.
	 * @throws RemoteException se ocorrer um erro durante a comunicação remota.
	 */
	@Override
	public void send_request_queue(Client_I c, Message m) throws RemoteException {
		queue.addFirst(m.text.toString());
	}

	/**
	 * ??????????????????????????????????????????
	 * @param barrel O barril a ser inscrito.
	 * @param id O ID do barril.
	 * @throws RemoteException se ocorrer um erro durante a comunicação remota.
	 */
	public void subscribe(Barrel_I barrel, int id) throws RemoteException{
		System.out.println("Subscri");
		//System.out.println(barrel);
		if(lb < 0)
			lb = 0;
		if(count > 0){
			System.out.println("Sync Needed Cuh");
			try (MulticastSocket socket = new MulticastSocket(4321)) {
				String message = "Sync";
				byte[] buffer = message.getBytes();

				InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
				socket.joinGroup(new InetSocketAddress(group, 0), NetworkInterface.getByIndex(0));

				DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
				socket.send(packet);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if(count < NUM_BARRELS){
			Barrel_struct.add_barrel(barrels, barrel, id, count);
			count++;
		}
	}

	/**
	 * Método para enviar respostas ao cliente.
	 * @param m A lista de URLs.
	 * @throws RemoteException se ocorrer um erro durante a comunicação remota.
	 */

	public void answer(ArrayList<URL_Content> m) throws RemoteException {
		if (!m.isEmpty()) {
			// Organizar a lista por prioridade
			m.sort(Comparator.comparingInt(a -> a.priority));
			// Adicionar a lista organizada ao HashMap
			results10.put(client, m);
		}
		else {
			results10.put(client, new ArrayList<>());
		}
	}


	/**
	 * @param c
	 * @param m
	 * @param indx
	 * @throws RemoteException
	 */
	@Override
	public void request10(Client_I c, Message m, int indx) throws RemoteException {
		if (indx < 0) {
			results10.remove(c);
			return;
		}
		client = c;
		// Verifica se há resultados para o cliente
		if (results10.containsKey(client)) {
			print_on_client_10(indx);
		} else {
			// Se não houver resultados para o cliente, envia uma solicitação
			send_request_barrels(c, m);
			print_on_client_10(indx);
		}
	}

	@Override
	public void print_on_client_10(int indx) throws java.rmi.RemoteException {

		ArrayList<URL_Content> contentToSend = new ArrayList<>();

		if (!results10.containsKey(client)) {
			return;
		}

		ArrayList<URL_Content> results = results10.get(client);

		if (results.isEmpty()) {
			return;
		}

		int startIndex = indx * 10;
		int endIndex = Math.min(startIndex + 10, results.size()); // Garante que não ultrapasse o tamanho da lista

		// Adiciona os 10 conteúdos à lista a ser enviada
		for (int i = startIndex; i < endIndex; i++) {
			contentToSend.add(results.get(i));
		}

		// Envie o ArrayList contentToSend para o cliente
		client.print_on_client(contentToSend);
	}



	/**
	 * Método para fornecer o painel de administração do sistema ao cliente.
	 * @return Uma mensagem com informações sobre o sistema.
	 * @throws RemoteException se ocorrer um erro durante a comunicação remota.
	 */
	public Message adm_painel() throws RemoteException {
		Message m = new Message("");
		m.addText("Online Servers: " + count + "\n");
		m.addText("Top 10 most common searches: \n");
		m.addText(top_searches.getTop10());
		m.addText("Average response time: \n");
		m.addText(Barrel_struct.get_avg_times(barrels, count));
		return m;
	}

	/**
	 * Método principal para iniciar o Gateway.
	 */
	public static void main(String args[]) {
		try {
			File_Infos f = new File_Infos();
			f.get_data("GateWay");

			MULTICAST_ADDRESS = f.Address;
			PORT = f.Port;
			GateWay h = new GateWay(f);
			LocateRegistry.createRegistry(1099).rebind(f.Registo[0], h);
			LocateRegistry.createRegistry(1098).rebind(f.Registo[1], h);
			System.out.println("GateWay ready.");
		} catch (RemoteException | MalformedURLException | NotBoundException re) {
			System.out.println("Exception in GateWay.main: " + re);
		}
	}
}

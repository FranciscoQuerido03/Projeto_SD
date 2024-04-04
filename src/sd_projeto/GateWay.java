package sd_projeto;

import java.io.IOException;
import java.net.*;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.*;
import java.util.ArrayList;
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
	 * @param min Número que define o intervalo de resultados a serem retornados.
	 * @throws RemoteException se ocorrer um erro durante a comunicação remota.
	 */
	public void send_request_barrels(Client_I c, Message m, int min) throws RemoteException {
		lock.lock();
		try {
			System.out.println("GateWay: " + m.toString() + " " + count);
			client = c;
			client_request = m.toString().trim();
			top_searches.updateSearchCount(client_request);
			if (lb >= 0) {
				if (lb >= count)
					lb = 0;
				//System.out.println("lb " + lb);
				//System.out.println(barrels[lb].barrel);
				//barrels[lb].printWordsHM();
				long inicio_pedido = System.currentTimeMillis();
				barrels[lb].barrel.request(client_request.toLowerCase(), min);
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
	public void subscribe(Barrel_I barrel, int id) throws RemoteException {
		lock.lock();
		try {
			if (lb < 0)
				lb = 0;
			if (count > 0) {
				if (count < NUM_BARRELS) {
					Barrel_struct.add_barrel(barrels, barrel, id, count);
					count++;
				}
			}
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Método para enviar respostas ao cliente.
	 * @param m A lista de URLs.
	 * @throws RemoteException se ocorrer um erro durante a comunicação remota.
	 */
	public void answer(ArrayList<URL_Content> m) throws RemoteException{
		//System.out.println(m.toString());
		client.print_on_client(m);
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

package sd_projeto;

import java.io.IOException;
import java.net.*;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.text.StyledEditorKit.BoldAction;

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

	private static final HashMap<Client_I, ArrayList<URL_Content>> results10 = new HashMap<>();
	private static ArrayList<Client_info> clientes;

	public static HashMap<Client_I, ArrayList<URL_Content>> getResults10() {
		return results10;
	}

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
		clientes = new ArrayList<>();
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
			adm_painel();
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Método para conectar um cliente.
	 * @param c O cliente a ser conectado.
	 * @throws RemoteException se ocorrer um erro durante a comunicação remota.
	 */
	public void client_connect(Client_I c) throws RemoteException {
		clientes.add(new Client_info(c, false));
	}

	/**
	 * Método para desconectar um cliente.
	 * @param c O cliente a ser desconectado.
	 * @throws RemoteException se ocorrer um erro durante a comunicação remota.
	 */
	public void client_disconnect(Client_I c) throws RemoteException {
		Client_info ci = get_client(c);
		if(ci != null)
			clientes.remove(ci);
	}

	/**
	 * Método para solicitar o painel de administração.
	 * @param c O cliente.
	 * @param b O bool que indica se o cliente deseja ver o painel de administração.
	 * @throws RemoteException se ocorrer um erro durante a comunicação remota.
	 */
	public void request_adm_painel(Client_I c, Boolean b) throws RemoteException {
		Client_info ci = get_client(c);
		ci.set_see_console();
		if(b)
			c.print_adm_console_on_client(construct_adm_painel());
	}

	/**
	 * Método para obter informações sobre um cliente.
	 * @param c O cliente.
	 * @return As informações do cliente.
	 */
	public Client_info get_client (Client_I c) {
		for(Client_info ci : clientes){
			if(ci.c.equals(c)){
				return ci;
			}
		}
		return null;
	}

	/**
	 * Método para enviar mensagens de erro ao cliente.
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
			adm_painel();
		}
	}

	/**
	 * Método processar as respostas dos barrels.
	 * @param m A lista de URLs.
	 * @throws RemoteException se ocorrer um erro durante a comunicação remota.
	 */
	public void answer(ArrayList<URL_Content> m) throws RemoteException {
		if (!m.isEmpty()) {
			// Organizar a lista por prioridade
			m.sort((a, b) -> Integer.compare(b.priority, a.priority));
			// Adicionar a lista organizada ao HashMap
			synchronized(results10) {
				results10.put(client, m);
			}
		}
		else {
			synchronized(results10) {
				results10.put(client, new ArrayList<>());
			}
		}
	}


	/**
	 * Método para processar as solicitações de pesquisa dos clientes.
	 * @param c O cliente.
	 * @param m A mensagem com a solicitação.
	 * @param indx O índice da página.
	 * @throws RemoteException se ocorrer um erro durante a comunicação remota.
	 */
	@Override
	public void request10(Client_I c, Message m, int indx) throws RemoteException {
        //Cliente desconecta-se
        if (indx < 0) {
            synchronized (results10) {
                results10.remove(c);
            }
            return;
        }

        // Verifica se há resultados para o cliente
        boolean flag;
        synchronized (results10) {
            flag = results10.containsKey(c);
		}

		client = c;

		if (flag) {
            print_on_client_10(c,indx);
        } else {
            // Se não houver resultados para o cliente, envia uma solicitação aos barrels
            send_request_barrels(c, m);
            print_on_client_10(c, indx);
        }
    }

	/**
	 * Método para enviar os resultados da pesquisa agrupados 10 a 10 ao cliente.
	 * @param c    O cliente que receberá os resultados.
	 * @param indx A posição inicial na lista de URLs.
	 * @throws java.rmi.RemoteException
	 */
	@Override
	public void print_on_client_10(Client_I c ,int indx) throws java.rmi.RemoteException {

		ArrayList<URL_Content> contentToSend = new ArrayList<>();

		boolean flag;
		synchronized (results10) {
			flag = results10.containsKey(c);
		}

		if (!flag) {
			return;
		}

		ArrayList<URL_Content> results;

		synchronized (results10) {
			results = results10.get(c);
		}

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
		c.print_on_client(contentToSend);
	}

	/**
	 * Método para processar as solicitações de links a apontar para um URL dos clientes.
	 * @param conteudo O URL.
	 * @throws RemoteException se ocorrer um erro durante a comunicação remota.
	 */

	@Override
	public void links_pointing_to(Client_I c, Message conteudo) throws RemoteException{
		client = c;
		client_request = conteudo.toString().trim();
		send_request_barrels_pointers();
	}

	/**
	 * Método para processar as respostas dos barrels relativas aos links a apontar para um URL.
	 * @param urlsPointingTo A lista de links.
	 */
	@Override
	public void answer_pointers(ArrayList<URL_Content> urlsPointingTo) throws RemoteException {
		client.print_on_client(urlsPointingTo);
	}

	/**
	 * Método para enviar solicitações de links a apontar para um URL aos barrels.
	 * @throws RemoteException se ocorrer um erro durante a comunicação remota.
	 */
	private void send_request_barrels_pointers() throws RemoteException {
		lock.lock();
		try {
			if (lb >= 0) {
				if (lb >= count)
					lb = 0;
				System.out.println("lb " + lb);
				long inicio_pedido = System.currentTimeMillis();
				barrels[lb].barrel.links_pointing_to(client_request);
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
	 * Método para fornecer o painel de administração do sistema ao cliente.
	 * @return Uma mensagem com informações sobre o sistema.
	 * @throws RemoteException se ocorrer um erro durante a comunicação remota.
	 */
	public void adm_painel() throws RemoteException {
		
		for(Client_info ci : clientes){
			if(ci.see_console){
				ci.c.print_adm_console_on_client(construct_adm_painel());
			}
		}
	}

	/**
	 * Método para construir o painel de administração do sistema.
	 * @return Uma mensagem com informações sobre o sistema.
	 */
	public Message construct_adm_painel() {
		Message m = new Message("");
		m.addText("============< ADM CONSOLE >============\n");
		m.addText("Online Servers: " + count + "\n");
		m.addText("Top 10 most common searches: \n");
		m.addText(top_searches.getTop10());
		m.addText("Average response time: \n");
		m.addText(Barrel_struct.get_avg_times(barrels, count));
		m.addText("============< ----------- >============\n");
		return m;
	}

	/**
	 * Método main que inicia o Gateway.
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

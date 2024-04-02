package sd_projeto;

import java.io.IOException;
//import java.net.*;
import java.net.*;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.*;

public class GateWay extends UnicastRemoteObject implements Request {

	private static String Erro_Indisponibilidade = "Service unavailable due to internal problems...";
	
	public static String client_request;
	static Barrel_struct barrels[];
	static TopSearches top_searches;
	static int count = 0;
	static int lb = -1;	//last_barrel ;;; this tech will change eventualy
	static int NUM_BARRELS;
	static Client_I client;
	private static String MULTICAST_ADDRESS;
	private static int PORT;

	static QueueInterface queue;

	public GateWay(File_Infos f) throws RemoteException, MalformedURLException, NotBoundException {
		super();
		NUM_BARRELS = f.NUM_BARRELS;
		barrels = new Barrel_struct[NUM_BARRELS];
		Barrel_struct.initialize(barrels, NUM_BARRELS);
		top_searches = new TopSearches();
		queue = (QueueInterface) Naming.lookup(f.lookup[0]);
	}

	public void barrel_disconnect(Barrel_I barrel) throws RemoteException {
		synchronized(this){
			count = Barrel_struct.remove_barrel(barrels, barrel, count);

			if(count <= 0)
				lb = -1;
		}

		return;
	}

	public void err_no_matches(Message s) throws RemoteException {
		client.print_err_2_client(s);
	}

	public void send_request_barrels(Client_I c, Message m) throws RemoteException {
		synchronized(this){
			System.out.println("GateWay: " + m.toString() + " " + count);
			client = c;
			client_request = m.toString().trim();
			top_searches.updateSearchCount(client_request);
			if(lb >= 0){
				if(lb >= count)
					lb = 0;
				System.out.println("lb " + lb);
				//System.out.println(barrels[lb]);
				//barrels[lb].printWordsHM();
				long inicio_pedido = System.currentTimeMillis();
				System.out.println(barrels[lb].barrel);
				barrels[lb].barrel.request(client_request.toLowerCase());
				long fim_pedido = System.currentTimeMillis();
				barrels[lb].avg_time = (barrels[lb].avg_time + ((fim_pedido - inicio_pedido)/100))/2;
				lb++;
			}else{
				client.print_err_2_client(new Message(Erro_Indisponibilidade));
			}
		}
    }

	@Override
	public void send_request_queue(Client_I c, Message m) throws RemoteException {
		queue.addFirst(m.text.toString());
	}

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

	public void answer(Urls_list m) throws RemoteException{
		System.out.println(m.toString());
		client.print_on_client(m);
	}

	public Message adm_painel() throws RemoteException {
		Message m = new Message("");

		m.addText("Online Servers: " + count + "\n");
		m.addText("Top 10 most common searches: \n");
		m.addText(top_searches.getTop10());
		m.addText("Average response time: \n");
		m.addText(Barrel_struct.get_avg_times(barrels, count));

		return m;
	}

	// =======================================================

	public static void main(String args[]) {
		try {
			File_Infos f = new File_Infos();
			f.get_data("GateWay");

			MULTICAST_ADDRESS = f.Address;
			PORT = f.Port;
			GateWay h = new GateWay(f);
			//System.out.println(f.Registo[0]);
			//System.out.println(f.Registo[1]);
			LocateRegistry.createRegistry(1099).rebind(f.Registo[0], h);
			LocateRegistry.createRegistry(1098).rebind(f.Registo[1], h);

			System.out.println("GateWay ready.");

		} catch (RemoteException | MalformedURLException | NotBoundException re) {
			System.out.println("Exception in GateWay.main: " + re);
		}
    }


}
package sd_projeto;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.*;
//import java.net.*;

public class GateWay extends UnicastRemoteObject implements Request {

	private static String Erro_Indisponibilidade = "Service unavailable due to internal problems...";
	
	public static String client_request;
	static Barrel_I barrels[];
	static int count = 0;
	static int lb = -1;	//last_barrel ;;; this tech will change eventualy
	static Client_I client;

	public GateWay() throws RemoteException {
		super();
		barrels = new Barrel_I[4];
	}

	public void barrel_disconnect(Barrel_I barrel) throws RemoteException {
		synchronized(this){
			//System.out.println(barrel);
			for(int i = 0; i<count; i++){
				//System.out.println(barrels[i]);
				if(barrels[i].equals(barrel)){
					for(int j=0; j+i<count; j++){
						barrels[i+j] = barrels[j+i+1];
					}
					count--;
					if(count <= 0)
						lb = -1;
					return;
				}
			}
		}
		throw new RemoteException("Barrel Disconnection failed... Barrel not found");
	}

	public void err_no_matches(Message s) throws RemoteException {
		client.print_err_2_client(s);
	}

	public void send_request(Client_I c, Message m) throws RemoteException {
		synchronized(this){
			System.out.println("GateWay: " + m.toString() + " " + count);
			client = c;
			client_request = m.toString();

			if(lb >= 0){
				if(lb >= count)
					lb = 0;
				System.out.println("lb " + lb);
				//System.out.println(barrels[lb]);
				barrels[lb].printWordsHM();
				barrels[lb].request(client_request.toLowerCase());
				lb++;
			}else{
				client.print_err_2_client(new Message(Erro_Indisponibilidade));
			}
		}
    }

	public void subscribe(Barrel_I barrel) throws RemoteException{
		//System.out.println("Subscri");
		//System.out.println(barrel);
		if(lb < 0)
			lb = 0;
		if(count > 0){
			System.out.println("Sync Needed Cuh");
			try (MulticastSocket socket = new MulticastSocket(4321)) {
                String message = "Sync";
                byte[] buffer = message.getBytes();

                InetAddress group = InetAddress.getByName("224.3.2.1");
				socket.joinGroup(new InetSocketAddress(group, 0), NetworkInterface.getByIndex(0));

				DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, 4321);
				socket.send(packet);

            } catch (IOException e) {
				e.printStackTrace();
			}
		}
		barrels[count] = barrel;
		count++;
	}

	public void answer(Urls_list m) throws RemoteException{
		System.out.println(m.toString());
		client.print_on_client(m);
	}

	// =======================================================

	public static void main(String args[]) {
		try {
			GateWay h = new GateWay();
			LocateRegistry.createRegistry(1099).rebind("request_barrel", h);
			LocateRegistry.createRegistry(1098).rebind("request", h);
			System.out.println("GateWay ready.");

		} catch (RemoteException re) {
			System.out.println("Exception in GateWay.main: " + re);
		}
	}


}
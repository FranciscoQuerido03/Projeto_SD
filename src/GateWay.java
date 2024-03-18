package sd_projeto;

import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.*;
//import java.net.*;

import sd_projeto.Urls_list;

public class GateWay extends UnicastRemoteObject implements Request {

	public static String client_request;
	static Barrel_I barrels[];
	static int count = 0;

	public GateWay() throws RemoteException {
		super();
		barrels = new Barrel_I[4];
	}

	public void send_request(Message m) throws RemoteException {
        System.out.println("GateWay: " + m.toString());
		client_request = m.toString();
		barrels[count-1].request(m.text);
    }

	public void subscribe(Barrel_I barrel) throws RemoteException{
		//System.out.println("Subscri");
		barrels[count] = barrel;
		count++;
	}

	public void answer(Urls_list m) throws RemoteException{
		System.out.println(m.toString());
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
package sd_projeto;


import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;

public class Testes {

    public void testRequest10() throws MalformedURLException, NotBoundException, RemoteException {
        File_Infos f = new File_Infos();
        f.get_data("GateWay");
        GateWay gateway = new GateWay(f);
        Client_I client = new Client();
        Message message = new Message("test message");

        HashMap<Client_I, ArrayList<URL_Content>> esperado = new HashMap<>();
        HashMap<Client_I, ArrayList<URL_Content>> real = new HashMap<>();

        ArrayList<URL_Content> list1 = new ArrayList<URL_Content>();
        ArrayList<URL_Content> list2 = new ArrayList<URL_Content>();

        URL_Content urlContent1 = new URL_Content("test URL", "test content");
        URL_Content urlContent2 = new URL_Content("test2 URL", "test content2");
        URL_Content urlContent3 = new URL_Content("test3 URL", "test content3");

        urlContent1.priority = 1;
        urlContent2.priority = 2;
        urlContent3.priority = 3;

        list1.add(urlContent1);
        list1.add(urlContent2);
        list1.add(urlContent3);

        list2.add(urlContent3);
        list2.add(urlContent2);
        list2.add(urlContent1);


        esperado.put(client, list2);

        int index = 0;

        try {
            GateWay.client = client;
            gateway.answer(list1);
            real = gateway.getResults10();
        } catch (RemoteException e) {

        }

        if (esperado.equals(real)) {
            System.out.println("TEST PRIORITY OK");
        } else {
            System.out.println("TEST PRIORITY FAIL");
        }

        System.exit(0);
    }

    public static void main(String[] args) {
        Testes testes = new Testes();
        try {
            testes.testRequest10();
        } catch (MalformedURLException | NotBoundException | RemoteException e) {
            e.printStackTrace();
        }
    }

}


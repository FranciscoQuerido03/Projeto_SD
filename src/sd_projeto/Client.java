package sd_projeto;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Implementação de um cliente para interagir com o motor de busca.
 * Este cliente pode realizar várias operações, como pesquisar, indexar e obter estatísticas.
 */
public class Client extends UnicastRemoteObject implements Client_I {

	private static String NAMING;

	/**
	 * Construtor para criar um cliente.
	 * Inicializa o cliente e carrega as informações de registo.
	 * @throws RemoteException se ocorrer um erro durante a criação do objeto remoto.
	 */
	public Client() throws RemoteException {
		super();
		File_Infos f = new File_Infos();
		f.get_data("Client");

		NAMING = f.lookup[0];
	}

	/**
	 * Imprime a lista de URLs no cliente.
	 * @param list A lista de URLs a ser impressa.
	 * @throws RemoteException se ocorrer um erro durante a execução remota.
	 */
	public void print_on_client(ArrayList<URL_Content> list) throws java.rmi.RemoteException {

		for (URL_Content urlContent : list) {
			System.out.println(urlContent.toString()); 
		}
			
	}

	/**
	 * Imprime a mensagem de erro no cliente.
	 * @param erro A mensagem de erro a ser impressa.
	 * @throws RemoteException se ocorrer um erro durante a execução remota.
	 */
	public void print_err_2_client(Message erro) throws RemoteException{
		System.out.println(erro.toString());
	}

	/**
	 * Metodo main para iniciar o cliente.
	 * Aprenta um menu de opções ao utilizador e executa a operação escolhida.
	 */
	public static void main(String args[]) {

		try {
			Client c = new Client();
			boolean keepItgoin = true;

			Scanner scanner = new Scanner(System.in);
			Request Conection = (Request) Naming.lookup(NAMING);
			while(keepItgoin){

				System.out.println("\nSelecione uma opção:");
				System.out.println("[1] search <search query>");
				System.out.println("[2] index <url>");
				System.out.println("[3] stats");
				System.out.println("[4] \\close\n");

				String str = scanner.nextLine();

				String[] parts = str.split(" ");
				String data = String.join(" ", Arrays.copyOfRange(parts, 1, parts.length));

				Message conteudo = new Message(data);

				switch (parts[0]) {
					case "1":
						System.out.println("\nPesquisando por " + conteudo + "...\n");
						searchBarrels(c, Conection, conteudo);
						break;
					case "2":
						if(checkUrl(conteudo)) {
							System.out.println("\nIndexando " + conteudo + "...\n");
							Conection.send_request_queue(c, conteudo);
						}
						break;
					case "3":
						Message response = Conection.adm_painel();
						System.out.println("\n" + response.toString());
						break;
					case "4":
						System.out.println("Terminado");
						scanner.close();
						UnicastRemoteObject.unexportObject(c, true);
						keepItgoin = false;
						break;
					default:
						System.out.println("Comando inválido");
						break;
				}
			}

		} catch (RemoteException re) {
			System.out.println("Exceção em GateWay.main: " + re);
		} catch (MalformedURLException e) {
			System.out.println("MalformedURLException em GateWay.main: " + e);
		} catch (NotBoundException e) {
			System.out.println("NotBoundException em GateWay.main: " + e);
		}

	}

	/**
	 * Verifica se o URL fornecido é válido.
	 * @param conteudo A mensagem que contém o URL.
	 * @return true se o URL for válido, false caso contrário.
	 */
	private static boolean checkUrl(Message conteudo) {
		if (!conteudo.toString().startsWith("http://") && !conteudo.toString().startsWith("https://")) {
			System.out.println("\nURL inválida\nA URL deve começar com 'http://' ou 'https://'\nTente novamente\n");
			return false;
		}
		return true;
	}

	/**
	 * Realiza a procura no barrels remotos.
	 * Apresenta os resultados da pesquisa ao utilizador e permite a navegação entre os resultados 10 a 10.
	 * @param c O cliente atual.
	 * @param conection Conexão com o servidor.
	 * @param conteudo A mensagem a ser pesquisada.
	 */
	private static void searchBarrels(Client c, Request conection, Message conteudo) {
		try {
			boolean end = false;
			int i = 0;
			Scanner scanner = new Scanner(System.in);
			while (!end) {
				conection.send_request_barrels(c, conteudo, i);
				System.out.println("[1] Anteriores 10\t[2] Próximos 10\t[3] Fim\n");
				String str = scanner.nextLine();
				switch (str) {
					case "1":
						i -= 1;
						break;
					case "2":
						i += 1;
						break;
					case "3":
						end = true;
						break;
					default:
						System.out.println("\nComando inválido\nTente novamente\n");
						break;
				}
			}

		} catch (RemoteException e) {
			System.out.println("RemoteException em GateWay.searchBarrels: " + e);
		}
	}

}

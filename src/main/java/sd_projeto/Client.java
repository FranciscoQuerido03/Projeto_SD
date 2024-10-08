package sd_projeto;


import java.util.*;
import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;

/**
 * Implementação de um cliente para interagir com o motor de busca.
 * Este cliente pode realizar várias operações, como pesquisar, indexar e obter estatísticas.
 */
public class Client extends UnicastRemoteObject implements Client_I {

	private static String NAMING;

	private static final List<String> stopWords = Arrays.asList(
			"de", "que", "do", "da", "em", "um", "para", "com", "não", "uma", "os", "no", "se",
			"na", "por", "mais", "as", "dos", "como", "mas", "foi", "ao", "ele", "das", "tem", "seu", "sua", "ou",
			"ser", "quando", "muito", "há", "nos", "já", "está", "eu", "também", "só", "pelo", "pela", "até", "isso",
			"ela", "entre", "era", "depois", "sem", "mesmo", "aos", "ter", "seus", "quem", "nas", "me", "esse", "eles",
			"estão", "você", "tinha", "foram", "essa", "num", "nem", "suas", "meu", "às", "minha", "têm", "numa", "pelos",
			"elas", "havia", "seja", "qual", "será", "nós", "tenho", "lhe", "deles", "essas", "esses", "pelas", "este",
			"fosse", "dele", "tu", "te", "vocês", "vos", "lhes", "meus", "minhas", "teu", "tua", "teus", "tuas", "nosso",
			"nossa", "nossos", "nossas", "dela", "delas", "esta", "estes", "estas", "aquele", "aquela", "aqueles", "aquelas",
			"isto", "aquilo", "estou", "está", "estamos", "estão", "estive", "esteve", "estivemos", "estiveram", "estava",
			"estávamos", "estavam", "estivera", "estivéramos", "esteja", "estejamos", "estejam", "estivesse", "estivéssemos",
			"estivessem", "estiver", "estivermos", "estiverem", "hei", "há", "havemos", "hão", "houve", "houvemos", "houveram",
			"houvera", "houvéramos", "haja", "hajamos", "hajam", "houvesse", "houvéssemos", "houvessem", "houver", "houvermos",
			"houverem", "houverei", "houverá", "houveremos", "houverão", "houveria", "houveríamos", "houveriam", "sou", "somos",
			"são", "era", "éramos", "eram", "fui", "foi", "fomos", "foram", "fora", "fôramos", "seja", "sejamos", "sejam", "fosse",
			"fôssemos", "fossem", "for", "formos", "forem", "serei", "será", "seremos", "serão", "seria", "seríamos", "seriam",
			"tenho", "tem", "temos", "tém", "tinha", "tínhamos", "tinham", "tive", "teve", "tivemos", "tiveram", "tivera",
			"tivéramos", "tenha", "tenhamos", "tenham", "tivesse", "tivéssemos", "tivessem", "tiver", "tivermos", "tiverem",
			"terei", "terá", "teremos", "terão", "teria", "teríamos", "teriam"
	);

	/**
	 * Construtor para criar um cliente.
	 * Inicializa o cliente e carrega as informações de registo.
	 * @throws RemoteException se ocorrer um erro durante a criação do objeto remoto.
	 */
	public Client() throws RemoteException {
		super();
		NAMING = "rmi://localhost:1098/request";
	}

	public Client(File_Infos f) throws RemoteException {
		super();
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
	 * Função que printa a adm_console
	 * @param m conteudo da adm_console a printar
	 * @throws RemoteException se ocorrer um erro durante a execução remota.
	 */
	public void print_adm_console_on_client(Message m) throws java.rmi.RemoteException {
		System.out.println(m.toString());
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
			File_Infos f = new File_Infos();
			f.get_data("Client");

			if (!f.goodRead) {
				System.out.println("Erro na leitura do arquivo de configuração");
				return;
			}

			Client c = new Client(f);
			boolean keepItgoin = true;

			Scanner scanner = new Scanner(System.in);
			Request Conection = (Request) Naming.lookup(NAMING);
			Conection.client_connect(c);

			while(keepItgoin) {

				System.out.println("\nSelecione uma opção:");
				System.out.println("[1] search <search query>");
				System.out.println("[2] index <url>");
				System.out.println("[3] Links pointing to <url>");
				System.out.println("[4] stats");
				System.out.println("[5] exit\n");


				if (scanner.hasNextLine()) {
					String str = scanner.nextLine();


					String[] parts = str.split(" ");
					String data = String.join(" ", Arrays.copyOfRange(parts, 1, parts.length));

					Message conteudo = new Message(data);

					switch (parts[0]) {
						case "1":
							System.out.println("\nSearching " + conteudo + "...\n");
							conteudo = new Message(removeStopWords(conteudo.toString()));
							searchBarrels(c, Conection, conteudo);
							break;
						case "2":
							if (checkUrl(conteudo)) {
								System.out.println("\nIndexing " + conteudo + "...\n");
								Conection.send_request_queue(c, conteudo);
							}
							break;
						case "3":
							if (checkUrl(conteudo)) {
								System.out.println("\nSearching...");
								searchPointers(c, Conection, conteudo);
							}
							break;
						case "4":
							Conection.request_adm_painel(c, true);
							adm_painel_handler();
							Conection.request_adm_painel(c, false);
							break;
						case "5":
							System.out.println("Terminado");
							scanner.close();
							UnicastRemoteObject.unexportObject(c, true);
							keepItgoin = false;
							break;
						default:
							System.out.println("Comando inválido");
							break;
					}
				}else {
					System.exit(1);
				}
			}

		} catch (RemoteException | MalformedURLException | NotBoundException re) {
			System.out.println("Gateway desligada");
			System.exit(1);
		}
	}

	/**
	 * Pesquisa os links que apontam para o URL fornecido.
	 * @param c O cliente atual.
	 * @param conection Conexão com o servidor.
	 * @param conteudo A mensagem que contém o URL.
	 */
	private static void searchPointers(Client c, Request conection, Message conteudo) {
		Scanner scanner = new Scanner(System.in);
		try {
			System.out.println("Links pointing to " + conteudo + ":\n");
			conection.links_pointing_to(c, conteudo);
			System.out.println("\nPressione Enter para continuar\n");
			scanner.nextLine();

		} catch (RemoteException e) {
			System.out.println("RemoteException em GateWay.searchPointers: " + e);
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
	 * Função que fica à espera que o utilizador introduza um comando para sair do modo check_adm_console
	 */
	private static void adm_painel_handler() {
		System.out.println("Press [1] to exit");
		Scanner sc = new Scanner(System.in);

		while(!sc.nextLine().equals("1"))
			continue;

		return;
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
			int indx = 0;
			Scanner scanner = new Scanner(System.in);
			while (!end) {
				conection.request10(c, conteudo, indx);
				System.out.println("Pagina atual: " + indx +"\n[1] Previous 10\t[2] Next 10\t[3] End\n");
				String str = scanner.nextLine();
				switch (str) {
					case "1":
						indx -= 1;
						if (indx < 0) indx = 0;
						break;
					case "2":
						indx += 1;
						break;
					case "3":
						end = true;
						break;
					default:
						System.out.println("\nComando inválido\nTente novamente\n");
						break;
				}
			}
			//Apaga o registo do cliente
			conection.request10(c, conteudo, -1);

		} catch (RemoteException e) {
			System.out.println("RemoteException em GateWay.searchBarrels: " + e);
		}
	}

	/**
	 * Remove as stop words de um texto.
	 * @param text O texto a ser filtrado.
	 * @return O texto sem as stop words.
	 */
	private static String removeStopWords(String text) {
		String[] words = text.split("\\s+");
		List<String> filteredWords = new ArrayList<>();
		for (String word : words) {
			if (!stopWords.contains(word.toLowerCase())) {
				if(word.length() > 2)
					filteredWords.add(word);
			}
		}
		return String.join(" ", filteredWords);
	}

}

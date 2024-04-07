package sd_projeto;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Function;

/**
 * Implementação de uma fila thread-safe remota em Java usando RMI.
 * Esta fila permite adicionar e remover elementos de forma remota.
 */
public class Queue extends UnicastRemoteObject implements QueueInterface {

    private Deque<String> queue;
    private int threadNumber = 0;

    private static BloomFilter<String>  bloomFilter;


    private static String NAMING_DOWNLOADER;
    private static String NAMING_GATEWAY;

    /**
     * Construtor da fila.
     * Inicializa a fila e carrega as informações de registo.
     * @throws RemoteException se ocorrer um erro ao criar o objeto remoto.
     */
    protected Queue(File_Infos f) throws RemoteException {
        queue = new ConcurrentLinkedDeque<>();

        NAMING_DOWNLOADER = f.Registo[0];
        NAMING_GATEWAY = f.Registo[1];
    }

    /**
     * Obtém o primeiro elemento da fila removendo-o da fila e adicionando-o ao bloomFilter.
     * @return O primeiro elemento da fila, ou null se a fila estiver vazia.
     * @throws RemoteException se ocorrer um erro durante a comunicação remota.
     */
    @Override
    public synchronized String getFirst() throws RemoteException {
        String url = queue.pollFirst();
        if (url != null) {
            bloomFilter.add(url);
        }
        return url;
    }

    /**
     * Obtém todos os elementos da fila.
     * @return Um ArrayList com todos os elementos da fila.
     * @throws RemoteException se ocorrer um erro durante a comunicação remota.
     */
    @Override
    public synchronized ArrayList<String> getAll() throws RemoteException {
        return new ArrayList<>(queue);
    }

    /**
     * Adiciona um elemento no início da fila se o elemento não existir no bloomFilter.
     * @param url O URL a ser adicionado.
     * @throws RemoteException se ocorrer um erro durante a comunicação remota.
     */
    @Override
    public synchronized void addFirst(String url) throws RemoteException {
        System.out.println(url);
        if (!bloomFilter.contains(url)) {
            queue.addFirst(url);
        }
    }

    /**
     * Adiciona um elemento no fim da fila se o elemento não existir no bloomFilter.
     * @param url O URL a ser adicionado.
     * @throws RemoteException se ocorrer um erro durante a comunicação remota.
     */
    @Override
    public synchronized void addLast(String url) throws RemoteException {
        if (!bloomFilter.contains(url)) {
            queue.addLast(url);
        }
    }

    /**
     * Método principal para inicializar a fila remotamente e o bloomFilter.
     */
    public static void main(String[] args) {
        try {
            File_Infos f = new File_Infos();
            f.get_data("Queue");

            if (!f.goodRead) {
                System.out.println("Erro na leitura do arquivo de configuração");
                return;
            }

            Queue q = new Queue(f);

            LocateRegistry.createRegistry(1096).rebind(NAMING_DOWNLOADER, q);
            LocateRegistry.createRegistry(1097).rebind(NAMING_GATEWAY, q);
            /*
             Inicializar o BloomFilter
             100 000 000 elementos esperados
             0.01% probabilidade de falsos positivos
             fonte:https://krisives.github.io/bloom-calculator/
            */

            // Criar uma lista de funções hash

            List<Function<String, Integer>> hashFunctions = Arrays.asList(
                    s -> s.hashCode(),
                    s -> s.hashCode() * s.length()
            );

            bloomFilter = new BloomFilter<>(1917011676, hashFunctions);

            System.out.println("Queue ready.");
        } catch (RemoteException re) {
            System.out.println("Exception in GateWay.main: " + re);
        }
    }
}


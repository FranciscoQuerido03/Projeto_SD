package com.example.demo.sd_projeto;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * Interface remota para comunicação com uma fila de URLs.
 */
public interface QueueInterface extends Remote {
    /**
     * Obtém o primeiro elemento da fila.
     * @return O primeiro elemento da fila.
     * @throws RemoteException se ocorrer um erro durante a execução remota.
     */
    public String getFirst() throws RemoteException;

    /**
     * Obtém todos os elementos da fila.
     *
     * @return Uma lista com todos os elementos da fila.
     * @throws RemoteException se ocorrer um erro durante a execução remota.
     */
    public ArrayList<String> getAll() throws RemoteException;

    /**
     * Adiciona um URL ao início da fila.
     *
     * @param url O URL a ser adicionado no início da fila.
     * @throws RemoteException se ocorrer um erro durante a execução remota.
     */
    public void addFirst(String url) throws RemoteException;

    /**
     * Adiciona um URL no fim da fila.
     *
     * @param url o URL a ser adicionado no fim da fila.
     * @throws RemoteException se ocorrer um erro durante a execução remota.
     */
    public void addLast(String url) throws RemoteException;
}


package com.example.demo.sd_projeto;

import java.rmi.*;
import java.util.ArrayList;

/**
 * Interface remota para comunicação com o cliente.
 * Define métodos para imprimir uma lista de URLs, consola de administração e mensagens de erro no cliente.
 */
public interface Client_I extends Remote {
    /**
     * Imprime uma lista de URLs no cliente.
     * @param list A lista de URLs a ser impressa.
     * @throws java.rmi.RemoteException se ocorrer um erro durante a execução remota.
     */
    public void print_on_client(ArrayList<URL_Content> list) throws java.rmi.RemoteException;

    /**
     * Imprime uma mensagem de erro no cliente.
     * @param erro A mensagem de erro a ser impressa.
     * @throws java.rmi.RemoteException se ocorrer um erro durante a execução remota.
     */
    public void print_err_2_client(Message erro) throws java.rmi.RemoteException;

    /**
     * Imprime uma mensagem da consola de administração no cliente.
     *
     * @param m A mensagem a ser impressa da consola de administração.
     * @throws java.rmi.RemoteException se ocorrer um erro durante a execução remota.
     */
    public void print_adm_console_on_client(Message m) throws java.rmi.RemoteException;
}

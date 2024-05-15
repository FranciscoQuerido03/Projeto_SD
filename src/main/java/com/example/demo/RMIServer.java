package com.example.demo;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RMIServer {
    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.createRegistry(2500);

            // Criar uma instância da implementação do servidor web
            WelcomeTeste webServer = new WelcomeTeste();

            // Vincular a instância do servidor web ao registro RMI
            registry.rebind("webServer", webServer);

            System.out.println("RMI server ready");
        } catch (Exception e) {
            System.err.println("RMI server exception:");
            e.printStackTrace();
        }
    }
}

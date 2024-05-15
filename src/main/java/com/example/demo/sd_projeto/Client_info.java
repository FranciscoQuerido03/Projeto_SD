package com.example.demo.sd_projeto;


/**
 * Classe que guarda informação sobre um cliente.
 */
public class Client_info {

    public Client_I c;
    public boolean see_console;

    /**
     * Construtor da classe Client_info.
     * @param c Cliente.
     * @param b Bool que indica se o cliente pode ver a admin console.
     */
    public Client_info(Client_I c, boolean b) {
        this.c = c;
        this.see_console = b;
    }

    /**
     * Alterna a visibilidade da admin console para o cliente.
     */
    public void set_see_console () {
        this.see_console = !this.see_console;
    }
}

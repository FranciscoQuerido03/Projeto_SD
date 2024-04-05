package sd_projeto;

public class Client_info {
    public Client_I c;
    public boolean see_console;

    public Client_info(Client_I c, boolean b) {
        this.c = c;
        this.see_console = b;
    }

    public void set_see_console () {
        this.see_console = !this.see_console;
    }
}

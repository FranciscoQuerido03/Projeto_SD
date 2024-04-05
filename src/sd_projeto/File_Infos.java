package sd_projeto;

import java.io.*;

/**
 * Classe para manipulação de informações de arquivos.
 * Esta classe lê as informações de um arquivo de configuração e disponibiliza as mesmas para uso futuro.
 */
public class File_Infos implements Serializable {
    public String Address;
    public int Port;
    public int NUM_BARRELS;
    public String Registo[];
    public String lookup[];

    private File file;
    private FileReader fileReader;
    private BufferedReader bufferedReader;

    /**
     * Construtor para criar um objeto File_Infos e inicializar a leitura do arquivo de configuração.
     */
    public File_Infos() {
        this.file = new File("..\\Index_config.txt");
        try {
            this.fileReader = new FileReader(file);
            this.bufferedReader = new BufferedReader(fileReader);
        } catch (IOException e) {
            System.out.println("Erro na leitura do arquivo de configuração");
        }
    }

    /**
     * Obtém os dados do arquivo de configuração.
     * @param s A seção específica a ser lida do arquivo.
     */
    public void get_data(String s) {
        String line;
        String[] sections;

        String search = "<" + s + ">";
        String search_f = "<\\" + s + ">";

        try {
            while ((line = this.bufferedReader.readLine()) != null) {
                if (line.equals(search)) {
                    while ((line = this.bufferedReader.readLine()) != null) {
                        sections = line.split(" ");

                        if (sections[0].equals("NUM_BARRELS:") && sections.length > 1)
                            this.NUM_BARRELS = Integer.parseInt(sections[1]);

                        if (sections[0].equals("ADDRESS:") && sections.length > 1)
                            this.Address = sections[1];

                        if (sections[0].equals("PORT:") && sections.length > 1)
                            this.Port = Integer.parseInt(sections[1]);

                        if (sections[0].equals("Registo1:") && sections.length > 1) {
                            this.Registo = new String[2];
                            this.Registo[0] = sections[1];
                        }

                        if (sections[0].equals("LookUp:") && sections.length > 1) {
                            this.lookup = new String[2];
                            this.lookup[0] = sections[1];
                        }

                        if (sections[0].equals("Registo2:") && sections.length > 1)
                            this.Registo[1] = sections[1];

                        if (sections[0].equals(search_f))
                            return;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Erro durante a leitura do arquivo");
        }
    }

}

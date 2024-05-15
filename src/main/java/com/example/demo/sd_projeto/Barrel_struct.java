package com.example.demo.sd_projeto;

/**
 * Classe que contém a estrutura de barrels do Gateway
 */
public class Barrel_struct {
    public Barrel_I barrel;     //Barrel
    public double avg_time;     //Tempo de resposta
    public int barrel_id;       //Id do barrel

    /**
     * Construtor da classe Barrel_struct
     * @param barrel    Barrel
     * @param avg_time  Tempo médio de resposta
     * @param id        Id do barrel
     */
    Barrel_struct(Barrel_I barrel, double avg_time, int id) {
        this.barrel = barrel;
        this.avg_time = avg_time;
        this.barrel_id = id;
    }

    /**
     * Função que inicializa a estrutura que acomoda os barrels (utilizado pelo Gateway)
     * @param lista         Lista com todos os barrels
     * @param barrel_count  Num de barrels máximos
     */
    public static void initialize(Barrel_struct[] lista, int barrel_count) {
        for (int i = 0; i < barrel_count; i++) {
            lista[i] = new Barrel_struct(null, 0.0, 0);
        }
    }
    
    /**
     * Função que adiciona um barrel à lista
     * @param lista     Lista com todos os barrels
     * @param barrel    Barrel a adicionar
     * @param id        Id do barrel
     * @param count     Num de barrels que existem
     */
    public static void add_barrel(Barrel_struct[] lista, Barrel_I barrel, int id, int count) {
        Barrel_struct new_barrel = new Barrel_struct(barrel, 0.0, id);

        lista[count] = new_barrel;
    }

    /**
     * Função que remove um barrel à lista
     * @param lista     Lista com todos os barrels
     * @param barrel    Barrel a remover
     * @param count     Num de barrels que existem
     * @return          Número atual de barrels na lista
     */
    public static int remove_barrel(Barrel_struct[] lista, Barrel_I barrel, int count) {
        for(int i = 0; i<count; i++){
            if(lista[i].barrel.equals(barrel)){
                for(int j=0; j+i<count-1; j++){
                    lista[i+j].barrel = lista[j+i+1].barrel;
                    lista[i+j].avg_time = lista[j+i+1].avg_time;
                    lista[i+j].barrel_id = lista[j+i+1].barrel_id;
                }
                count--;

                return count;
            }
        }
        return count;
    }

    /**
     * Função que retorna uma string formatada para a adm_console com os average_times
     * @param lista     Lista ed barrel
     * @param count     Num atual de barrels ativos
     * @return          String formatada
     */
    public static String get_avg_times(Barrel_struct[] lista, int count) {
        StringBuilder s = new StringBuilder();
        for(int i = 0; i < count; i++) {
            s.append("[Barrel id<" + lista[i].barrel_id + ">] => " + lista[i].avg_time + " s\n");
        }
        return s.toString();
    }

}

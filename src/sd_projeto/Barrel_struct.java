package sd_projeto;

public class Barrel_struct {
    public Barrel_I barrel;
    public double avg_time;
    public int barrel_id;

    Barrel_struct(Barrel_I barrel, double avg_time, int id) {
        this.barrel = barrel;
        this.avg_time = avg_time;
        this.barrel_id = id;
    }

    public static void initialize(Barrel_struct[] lista, int barrel_count) {
        for (int i = 0; i < barrel_count; i++) {
            lista[i] = new Barrel_struct(null, 0.0, 0);
        }
    }

    public static void add_barrel(Barrel_struct[] lista, Barrel_I barrel, int id, int count) {
        Barrel_struct new_barrel = new Barrel_struct(barrel, 0.0, id);

        lista[count] = new_barrel;
    }

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

    public static String get_avg_times(Barrel_struct[] lista, int count) {
        StringBuilder s = new StringBuilder();
        for(int i = 0; i < count; i++) {
            s.append("[Barrel id<" + lista[i].barrel_id + ">] => " + lista[i].avg_time + " s\n");
        }
        return s.toString();
    }

}

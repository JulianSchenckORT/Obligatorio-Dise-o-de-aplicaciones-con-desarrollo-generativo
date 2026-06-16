package ort.da.obligatoriodiseno.utils;

import java.util.ArrayList;
import java.util.List;

public class Command {
  private String id;
    private Object parametro;

    public Command(String id, Object parametro) {
        this.id = id;
        this.parametro = parametro;
    }

    public Command() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Object getParametro() {
        return parametro;
    }

    public void setParametro(Object parametro) {
        this.parametro = parametro;
    }
    
    public static Commands lista(Command... respuestas){
         List<Command> retorno = new ArrayList<Command>();
         for(Command r:respuestas){
             retorno.add(r);
         }
         return new Commands(retorno);
    }
}

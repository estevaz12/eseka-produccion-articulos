package ar.com.leo.produccion.fx.service;

import ar.com.leo.produccion.jdbc.MaquinaDAO;
import ar.com.leo.produccion.model.Maquina;
import javafx.concurrent.Task;

import java.util.List;


public class MaquinaTask extends Task<List<Maquina>> {
    // A Task is a one-shot thing and its initial state should be immutable (or at least encapsulated from external modification).
    private final String roomCode;

    public MaquinaTask(String roomCode) {
        this.roomCode = roomCode;
    }

    @Override
    protected List<Maquina> call() {
        return MaquinaDAO.obtenerMaquinas(this.roomCode);
    }

}


package ar.com.leo.produccion.fx.service;

import ar.com.leo.produccion.jdbc.ArticuloProducidoDAO;
import ar.com.leo.produccion.model.ArticuloProducido;
import javafx.concurrent.Task;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class ArticuloProducidoTask extends Task<List<ArticuloProducido>> {
    // A Task is a one-shot thing and its initial state should be immutable (or at least encapsulated from external modification).
    private final String roomCode;
    private final LocalDateTime fechaInicio;
    private final LocalDateTime fechaFin;
    private final boolean actual;
    private final String articulo;

    public ArticuloProducidoTask(String roomCode, LocalDateTime fechaInicio, LocalDateTime fechaFin, boolean actual, String articulo) {
        this.roomCode = roomCode;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.actual = actual;
        this.articulo = articulo;
    }

    @Override
    protected List<ArticuloProducido> call() throws SQLException {
        return ArticuloProducidoDAO.obtenerProduccion(this.roomCode, this.fechaInicio, this.fechaFin, this.actual, this.articulo);
    }

}


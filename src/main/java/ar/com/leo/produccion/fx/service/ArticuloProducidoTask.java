package ar.com.leo.produccion.fx.service;

import ar.com.leo.produccion.jdbc.ArticuloProducidoDAO;
import ar.com.leo.produccion.jdbc.ColorDAO;
import ar.com.leo.produccion.model.ArticuloColor;
import ar.com.leo.produccion.model.ArticuloProducido;
import javafx.concurrent.Task;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
        final List<ArticuloColor> articulosColores = ColorDAO.obtenerArticulosColores();
        final List<ArticuloProducido> articulosProducidos = ArticuloProducidoDAO.obtenerProduccion(this.roomCode, this.fechaInicio, this.fechaFin, this.actual, this.articulo);

        List<ArticuloProducido> articulosPunto = obtenerResultado(articulosColores, articulosProducidos);

        return articulosPunto;
    }

    private List<ArticuloProducido> obtenerResultado(List<ArticuloColor> articulosColores, List<ArticuloProducido> articulosProducidos) {
        return articulosProducidos.stream().map(articuloProducido -> {
            final ArticuloColor articuloColorEncontrado = articulosColores.stream()
                    .filter(articuloColor -> 
                        articuloProducido.getNumero().equals(articuloColor.getNumero()) 
                        && articuloProducido.getColor().equals(articuloColor.getColor()))
                    .findFirst().orElse(null);

            if (articuloColorEncontrado != null) {
                articuloProducido.setPunto(articuloColorEncontrado.getPunto());

                String styleCode = articuloProducido.getStyleCode();
                String art = styleCode.substring(0, 5);
                String punto = articuloProducido.getPunto();
                String talleColor = styleCode.substring(6, styleCode.length());
                articuloProducido.setStyleCode(art + "." + punto + talleColor);
            }

            return articuloProducido;
        }).collect(Collectors.toList());
    }

}


package ar.com.leo.produccion.fx.service;

import ar.com.leo.produccion.jdbc.ColorDAO;
import ar.com.leo.produccion.jdbc.MaquinaDAO;
import ar.com.leo.produccion.jdbc.ProgramadaArticuloProducidoDAO;
import ar.com.leo.produccion.model.ArticuloColor;
import ar.com.leo.produccion.model.ArticuloProducido;
import ar.com.leo.produccion.model.Maquina;
import javafx.concurrent.Task;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;


public class MaquinaTask extends Task<List<Maquina>> {
    // A Task is a one-shot thing and its initial state should be immutable (or at least encapsulated from external modification).
    private final String roomCode;

    public MaquinaTask(String roomCode) {
        this.roomCode = roomCode;
    }

    @Override
    protected List<Maquina> call() throws SQLException {
            List<Maquina> maquinas = MaquinaDAO.obtenerMaquinas(this.roomCode);
    
            final List<ArticuloColor> articulosColores = ColorDAO.obtenerArticulosColores();
            final List<ArticuloProducido> articulosProducidos = ProgramadaArticuloProducidoDAO.obtenerProduccion();
            // System.out.println("RESULTADO: " + obtenerResultado(articulosColores, articulosProducidos, articulosProgramada));
    
            List<ArticuloProducido> articulosPunto = obtenerResultado(articulosColores, articulosProducidos);
            // System.out.println(articulosPunto.stream()
            //         .map(Object::toString)
            //         .collect(Collectors.joining("\n")));
            HashMap<Integer, String> maquinasConPunto = getMaquinasConPunto(articulosPunto);
    
            setArtConPunto(maquinas, maquinasConPunto);
    
            return maquinas;
    }

    private HashMap<Integer, String> getMaquinasConPunto(List<ArticuloProducido> articulosPunto) {
        HashMap<Integer, String> maquinasConPunto = new HashMap<>();
        for (ArticuloProducido articuloProducido : articulosPunto) {

            if (articuloProducido.getMaquinas() != null) {
                for (int i = 0; i < articuloProducido.getMaquinas().length; i++) {
                    if (articuloProducido.getMaquinas()[i] != null) {
                        Integer machCode = Integer.parseInt(articuloProducido.getMaquinas()[i]);
                    maquinasConPunto.computeIfAbsent(machCode, k -> articuloProducido.getPunto());
                    }
                }
            }
        }
        return maquinasConPunto;
    }

    private void setArtConPunto(List<Maquina> maquinas, HashMap<Integer, String> maquinasConPunto) {

        for (Maquina maquina : maquinas) {
            Integer machCode = maquina.getMachCode();
            
            if (maquinasConPunto.containsKey(machCode)) {
                maquina.setPunto(maquinasConPunto.get(machCode));
            }

        }
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
            }

            return articuloProducido;
        }).collect(Collectors.toList());
    }
}


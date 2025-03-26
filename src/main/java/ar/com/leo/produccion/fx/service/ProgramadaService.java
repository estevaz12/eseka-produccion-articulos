package ar.com.leo.produccion.fx.service;

import ar.com.leo.produccion.jdbc.ArticuloProducidoDAO;
import ar.com.leo.produccion.jdbc.ColorDAO;
import ar.com.leo.produccion.jdbc.ProgramadaArticuloProducidoDAO;
import ar.com.leo.produccion.model.ArticuloColor;
import ar.com.leo.produccion.model.ArticuloProducido;
import ar.com.leo.produccion.model.ArticuloProgramada;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProgramadaService extends Service<List<ArticuloProducido>> {

    private final File archivoPdf;

    public ProgramadaService(File archivoPdf) {
        this.archivoPdf = archivoPdf;
    }

    @Override
    protected Task<List<ArticuloProducido>> createTask() {
        return new Task<>() {
            @Override
            protected List<ArticuloProducido> call() throws Exception {
                final List<ArticuloColor> articulosColores = ColorDAO.obtenerArticulosColores();
                final List<ArticuloProducido> articulosProducidos = ProgramadaArticuloProducidoDAO.obtenerProduccion();
                final List<ArticuloProgramada> articulosProgramada = leerProgramada(archivoPdf);
//                System.out.println("RESULTADO: " + obtenerResultado(articulosColores, articulosProducidos, articulosProgramada));

                return obtenerResultado(articulosColores, articulosProducidos, articulosProgramada);
            }
        };
    }

    private List<ArticuloProducido> obtenerResultado(List<ArticuloColor> articulosColores, List<ArticuloProducido> articulosProducidos, List<ArticuloProgramada> articulosProgramada) {
        return articulosProducidos.stream().map(articuloProducido -> {

            final ArticuloColor articuloColorEncontrado = articulosColores.stream()
                    .filter(articuloColor -> articuloProducido.getNumero().equals(articuloColor.getNumero()) && articuloProducido.getColor().equals(articuloColor.getColor()))
                    .findFirst().orElse(null);

            if (articuloColorEncontrado != null) {
                articuloProducido.setPunto(articuloColorEncontrado.getPunto());
                final ArticuloProgramada articuloProgramadaEncontrado = articulosProgramada.stream()
                        .filter(articuloProgramada -> articuloProgramada.getNumero().equals(articuloProducido.getNumero().replaceAll("^0+", ""))
                                && articuloProgramada.getPunto().equals(articuloProducido.getPunto())
                                && articuloProgramada.getTalle().equals(articuloProducido.getTalle()))
                        .findFirst().orElse(null);
                if (articuloProgramadaEncontrado != null) {
                    articuloProducido.setProducir(articuloProgramadaEncontrado.getProducir());
                }
            }

//                System.out.println("----------------------------------------------------------------------------------------------------------------------------------");
//                System.out.println(articuloProducido);
//                System.out.println(articuloColorEncontrado);
//                System.out.println(articuloProgramadaEncontrado);

            return articuloProducido;
        }).collect(Collectors.toList());
    }

    private List<ArticuloProgramada> leerProgramada(File archivoPdf) {
        try (PDDocument document = Loader.loadPDF(archivoPdf)) {

            final PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            final String page = stripper.getText(document);
            // Split the page content into lines
            final String[] lines = page.split("\n");
            final List<ArticuloProgramada> articulos = new ArrayList<>();
            // Loop through each line and store it in a variable if it contains a comma
            for (String line : lines) {
                if (line.contains(",")) {
                    final String[] words = line.split(" ");
                    final int coma = words[0].indexOf(',');
                    ArticuloProgramada articulo = new ArticuloProgramada();
                    articulo.setNumero(words[0].substring(0, coma));
                    articulo.setPunto(words[0].substring(coma + 1, coma + 3));
                    articulo.setDescripcion(words[1]);
                    articulo.setTalle(Integer.parseInt(words[words.length - 4]));
                    articulo.setProducido(Integer.parseInt(words[words.length - 2]));
                    articulo.setProducir(Integer.parseInt(words[words.length - 3]));
                    articulos.add(articulo);
                }
            }

            return articulos;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}


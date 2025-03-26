package ar.com.leo.produccion.fx.controller;

import ar.com.leo.produccion.fx.service.ProgramadaService;
import ar.com.leo.produccion.model.ArticuloProducido;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.print.*;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.transform.Scale;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.HashSet;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

public class ProgramadaController implements Initializable {

    @FXML
    private TextField ubicacionPdf;
    @FXML
    private TextArea logTextArea;
    @FXML
    private TableView<ArticuloProducido> articulosTableView;
    @FXML
    private TableColumn<ArticuloProducido, String> colArticulo;
    @FXML
    private TableColumn<ArticuloProducido, Integer> colTalle;
    @FXML
    private TableColumn<ArticuloProducido, Integer> colProducir;
    @FXML
    private TableColumn<ArticuloProducido, Double> colProducido;
    @FXML
    private TableColumn<ArticuloProducido, String> colProduciendo;
    @FXML
    private TableColumn<ArticuloProducido, String> colTiempo;
    @FXML
    private TableColumn<ArticuloProducido, String> colHorario;
    @FXML
    private CheckBox produccionCheckBox;
    @FXML
    private TextField buscador;

    private ObservableList<ArticuloProducido> articulosProducidosList;
    private static File archivoPdf;
    private DateTimeFormatter formatter;
    private Set<String> articulosSinPunto;
    private Set<String> articulosFaltantesProgramada;

    public void initialize(URL url, ResourceBundle rb) {
//        BasicConfigurator.configure(); // configure Log4j

        formatter = DateTimeFormatter.ofPattern("dd/MM HH:mm a");

        colArticulo.setCellValueFactory(param -> {
            final SimpleStringProperty articulo = new SimpleStringProperty();
            if (param.getValue().getPunto() != null) {
                articulo.set(param.getValue().getNumero() + "." + param.getValue().getPunto());
            } else {
                articulo.set(param.getValue().getNumero() + ".?");
                articulosSinPunto.add(param.getValue().getNumero() + " (" + param.getValue().getColor() + ")");
            }

            return articulo;
        });

        colArticulo.setCellFactory(column -> new TableCellArticuloWithTooltip<>());

        colTalle.setCellValueFactory(new PropertyValueFactory<>("talle"));

        colProducir.setCellFactory(column -> new TableCellProducirWithTooltip<>());
        colProducir.setCellValueFactory(param -> {
            if (param.getValue().getProducir() == null) {
                articulosFaltantesProgramada.add(param.getValue().getNumero() + "." + param.getValue().getPunto() + " T." + param.getValue().getTalle());
                return null;
            } else {
                final SimpleIntegerProperty aProducir = new SimpleIntegerProperty();
                aProducir.set(param.getValue().getProducir());
                return aProducir.asObject();
            }
        });

        colProducido.setCellValueFactory(new PropertyValueFactory<>("docenas"));

        colProduciendo.setCellValueFactory(new PropertyValueFactory<>("produciendo"));

        colTiempo.setCellValueFactory(param -> {
            final SimpleStringProperty tiempo = new SimpleStringProperty();
            if (param.getValue().getProducir() != null && param.getValue().getProducir() > 0 && param.getValue().getProduciendo().contains("SI") && param.getValue().getIdealCycle() > 0) {
                final long segundos = param.getValue().getIdealCycle() * (param.getValue().getProducir() * 24 - param.getValue().getUnidades())
                        / (param.getValue().getMaquinas() != null ? param.getValue().getMaquinas().length : 1);
                final Duration duration = Duration.ofSeconds(segundos);
                if (segundos > 0) {
                    tiempo.set((duration.toDaysPart() > 0 ? duration.toDaysPart() + "d " : "") + duration.toHoursPart() + "h " + duration.toMinutesPart() + "m " + duration.toSecondsPart() + "s");
                } else {
                    tiempo.set("LLEGÓ");
                }
            } else {
                tiempo.set("-");
            }

            return tiempo;
        });

        colHorario.setCellValueFactory(param -> {
            final SimpleStringProperty horario = new SimpleStringProperty();
            if (param.getValue().getProducir() != null && param.getValue().getProducir() > 0 && param.getValue().getProduciendo().contains("SI") && param.getValue().getIdealCycle() > 0) {
                final long segundos = param.getValue().getIdealCycle() * (param.getValue().getProducir() * 24 - param.getValue().getUnidades())
                        / (param.getValue().getMaquinas() != null ? param.getValue().getMaquinas().length : 1);
                final Duration duration = Duration.ofSeconds(segundos);

                if (segundos > 0) {
                    // Adding duration to the current time
                    final LocalDateTime futureTime = LocalDateTime.now().plus(duration);
                    horario.set(futureTime.format(formatter));
                } else {
                    horario.set("-");
                }
            } else {
                horario.set("-");
            }

            return horario;
        });

        colProduciendo.setComparator((t1, t2) -> {
            int total1 = 0;
            if (t1 != null && t1.contains("SI")) {
                total1 = 1;
            }

            int total2 = 0;
            if (t2 != null && t2.contains("SI")) {
                total2 = 1;
            }

            return Integer.compare(total1, total2);
        });

        colTiempo.setComparator((t1, t2) -> {
            int dias1 = 0;
            int horas1;
            int total1;
            if (t1 == null || t1.equals("LLEGÓ")) {
                total1 = 0;
            } else if (t1.equals("-")) {
                total1 = Integer.MAX_VALUE;
            } else {
                if (t1.contains("d")) {
                    dias1 = Integer.parseInt(t1.substring(0, t1.indexOf('d')));
                    horas1 = Integer.parseInt(t1.substring(t1.indexOf('d') + 2, t1.indexOf('h')));
                } else {
                    horas1 = Integer.parseInt(t1.substring(0, t1.indexOf('h')));
                }
                int minutos1 = Integer.parseInt(t1.substring(t1.indexOf('h') + 2, t1.indexOf('m')));
                int segundos1 = Integer.parseInt(t1.substring(t1.indexOf('m') + 2, t1.indexOf('s')));

                total1 = (dias1 * 24 * 60 * 60) + (horas1 * 60 * 60) + (minutos1 * 60) + segundos1;
            }

            int dias2 = 0;
            int horas2;
            int total2;
            if (t2 == null || t2.equals("LLEGÓ")) {
                total2 = 0;
            } else if (t2.equals("-")) {
                total2 = Integer.MAX_VALUE;
            } else {
                if (t2.contains("d")) {
                    dias2 = Integer.parseInt(t2.substring(0, t2.indexOf('d')));
                    horas2 = Integer.parseInt(t2.substring(t2.indexOf('d') + 2, t2.indexOf('h')));
                } else {
                    horas2 = Integer.parseInt(t2.substring(0, t2.indexOf('h')));
                }
                int minutos2 = Integer.parseInt(t2.substring(t2.indexOf('h') + 2, t2.indexOf('m')));
                int segundos2 = Integer.parseInt(t2.substring(t2.indexOf('m') + 2, t2.indexOf('s')));

                total2 = (dias2 * 24 * 60 * 60) + (horas2 * 60 * 60) + (minutos2 * 60) + segundos2;
            }

            return Integer.compare(total1, total2);
        });

        articulosTableView.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(ArticuloProducido articuloProducido, boolean empty) {
                super.updateItem(articuloProducido, empty);
                setStyle("");
                if (articuloProducido != null) {
                    if (articuloProducido.getProducir() != null && articuloProducido.getProduciendo().contains("SI") && articuloProducido.getDocenas() >= articuloProducido.getProducir()) {
                        setStyle("-fx-background-color: #f580ab;");
                    } else if (articuloProducido.getProducir() != null && articuloProducido.getProduciendo().equals("NO") && articuloProducido.getDocenas() >= articuloProducido.getProducir()) {
                        setStyle("-fx-background-color: #f580ab;");
                    } else if (!"NO".equals(articuloProducido.getProduciendo())) {
                        setStyle("-fx-background-color: #c6d4ff;");
                    }
                }
            }
        });
    }

    @FXML
    public void buscarPdf(ActionEvent event) {
        Scene scene = ((Node) event.getSource()).getScene();
        Stage stage = (Stage) scene.getWindow();

        produccionCheckBox.selectedProperty().set(false);
        buscador.clear();
        logTextArea.clear();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Elige archivo .PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo PDF", "*.pdf"));
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        archivoPdf = fileChooser.showOpenDialog(stage);
        if (archivoPdf != null) {
            ubicacionPdf.setText(archivoPdf.getAbsolutePath());
            generar();
        } else {
            ubicacionPdf.clear();
        }
    }

    // buscador
    @FXML
    public void buscarArticulo(KeyEvent event) {
        filtrar(buscador, produccionCheckBox);
    }

    // checkbox
    @FXML
    public void enProduccion(ActionEvent event) {
        filtrar(buscador, produccionCheckBox);
    }

    @FXML
    private void handleButtonRefresh(ActionEvent actionEvent) {
        logTextArea.clear();
        generar();
    }

    @FXML
    private void handleButtonVolver(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Produccion.fxml"));
            AnchorPane pane = loader.load();
            Scene scene = ((Node) actionEvent.getSource()).getScene();
            Stage stage = (Stage) scene.getWindow();
            scene.setRoot(pane);
            stage.setScene(scene);
            stage.setTitle("Producción");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void imprimir(ActionEvent event) {

        if (articulosTableView.getItems().size() > 0) {
            // Create a PrinterJob
            final PrinterJob job = PrinterJob.createPrinterJob();
            if (job == null) {
                logTextArea.setStyle("-fx-text-fill: red;");
                logTextArea.setText("No se encontraron impresoras.\n");
                return;
            }

            // Get the default printer
            final Printer printer = Printer.getDefaultPrinter();

            // Customize the page layout
            final PageLayout pageLayout = printer.createPageLayout(
                    Paper.A4, // Default paper size
                    PageOrientation.LANDSCAPE, // Set orientation (PORTRAIT or LANDSCAPE)
                    Printer.MarginType.HARDWARE_MINIMUM // Set to minimum margins
            );

            // Display the print dialog to allow users to choose settings
            boolean proceed = job.showPrintDialog(articulosTableView.getScene().getWindow());
            if (!proceed) {
                return;
            }

            // Set the job's page layout
            job.getJobSettings().setPageLayout(pageLayout);

            // Create a label to show the date at the bottom
//            Label dateLabel = new Label(formatter.format(LocalDateTime.now()));
//            dateLabel.setStyle("-fx-font-size: 10px; -fx-alignment: center;");
//            dateLabel.setPrefWidth(pageLayout.getPrintableWidth());
//            dateLabel.setPadding(new Insets(50, 0, 0, 0));

            // Combine the table and the date
//            VBox printableContent = new VBox(articulosTableView, dateLabel);
//            printableContent.setPrefHeight(pageLayout.getPrintableHeight());
//            printableContent.setPrefWidth(pageLayout.getPrintableWidth());
////            printableContent.setAutoSizeChildren(false);
//            printableContent.setAlignment(Pos.CENTER);

            // Calculate the scale factor to fit the TableView to the page
            double scaleX = pageLayout.getPrintableWidth() / articulosTableView.getBoundsInParent().getWidth();
            double scaleY = pageLayout.getPrintableHeight() / articulosTableView.getBoundsInParent().getHeight();
            double scale = Math.min(scaleX, scaleY);

            // Apply the scaling transformation
            Scale scaleTransform = new Scale(scale, scale);
            articulosTableView.getTransforms().add(scaleTransform);

            // Print the table
            boolean success = job.printPage(pageLayout, articulosTableView);

            // Reset the table's transformations and height
            articulosTableView.getTransforms().remove(scaleTransform);
            if (success) {
                job.endJob();
            } else {
                logTextArea.setStyle("-fx-text-fill: red;");
                logTextArea.setText("Error al imprimir.\n");
            }
        } else {
            logTextArea.setStyle("-fx-text-fill: red;");
            logTextArea.setText("No hay ninguna tabla para imprimir.\n");
        }
    }

    private void filtrar(TextField buscador, CheckBox produccionCheckBox) {
        if (!buscador.getText().isBlank() && produccionCheckBox.isSelected()) {
            // Filter the data based on the input
            final ObservableList<ArticuloProducido> filteredData = FXCollections.observableArrayList();
            for (ArticuloProducido articuloProducido : this.articulosProducidosList) {
                if (articuloProducido.getPunto() != null && (articuloProducido.getNumero() + "." + articuloProducido.getPunto()).contains(buscador.getText()) && articuloProducido.getProduciendo().contains("SI")) {
                    filteredData.add(articuloProducido);
                }
            }
            // Update the table with the filtered data
            articulosTableView.setItems(filteredData);
        } else if (!buscador.getText().isBlank() && !produccionCheckBox.isSelected()) {
            // Filter the data based on the input
            final ObservableList<ArticuloProducido> filteredData = FXCollections.observableArrayList();
            for (ArticuloProducido articuloProducido : this.articulosProducidosList) {
                if (articuloProducido.getPunto() != null && (articuloProducido.getNumero() + "." + articuloProducido.getPunto()).contains(buscador.getText())) {
                    filteredData.add(articuloProducido);
                }
            }
            // Update the table with the filtered data
            articulosTableView.setItems(filteredData);
        } else if (buscador.getText().isBlank() && produccionCheckBox.isSelected()) {
            // Filter the data based on the input
            final ObservableList<ArticuloProducido> filteredData = FXCollections.observableArrayList();
            for (ArticuloProducido articuloProducido : this.articulosProducidosList) {
                if (articuloProducido.getProduciendo().contains("SI")) {
                    filteredData.add(articuloProducido);
                }
            }
            // Update the table with the filtered data
            articulosTableView.setItems(filteredData);
        } else {
            articulosTableView.setItems(this.articulosProducidosList);
        }
    }

    private void generar() {

        articulosTableView.getItems().clear();
        articulosSinPunto = new HashSet<>();
        articulosFaltantesProgramada = new HashSet<>();

        if (archivoPdf != null && archivoPdf.isFile()) {

            final ProgramadaService service = new ProgramadaService(archivoPdf);

            service.setOnRunning(e -> {
                articulosTableView.setDisable(true);
                logTextArea.setStyle("-fx-text-fill: darkblue;");
                logTextArea.appendText("Generando...\n");
            });

            service.setOnSucceeded(e -> {
                if (service.getValue() != null) {
                    this.articulosProducidosList = FXCollections.observableArrayList(service.getValue());
                    articulosTableView.setItems(this.articulosProducidosList);
                    // Sort the table by the desired column in descending order
//                    articulosTableView.sort();
                    articulosTableView.getSortOrder().add(colTiempo);
                    articulosTableView.getSortOrder().add(colProduciendo);
                    articulosTableView.getSortOrder().add(colArticulo);
                    articulosTableView.getSortOrder().add(colTalle);
                    colTiempo.setSortType(TableColumn.SortType.ASCENDING);
                    colProduciendo.setSortType(TableColumn.SortType.DESCENDING);
                    colArticulo.setSortType(TableColumn.SortType.ASCENDING);
                    colTalle.setSortType(TableColumn.SortType.ASCENDING);
                }
                articulosTableView.setDisable(false);
                logTextArea.setStyle("-fx-text-fill: darkgreen;");

                final StringBuilder logText = new StringBuilder("Producción del mes de " + LocalDate.now().getMonth().getDisplayName(TextStyle.FULL, Locale.forLanguageTag("es-AR")) + ".");

                if (!articulosSinPunto.isEmpty()) {
                    logText.append("\nArtículos que falta asignarle el punto:");
                    for (String art : articulosSinPunto) {
                        logText.append("\n-").append(art);
                    }
                    logTextArea.setStyle("-fx-text-fill: red;");
                }
                if (!articulosFaltantesProgramada.isEmpty()) {
                    logText.append("\nArtículos que faltan en la programada:");
                    for (String art : articulosFaltantesProgramada) {
                        logText.append("\n-").append(art);
                    }
                    logTextArea.setStyle("-fx-text-fill: red;");
                }

                logTextArea.setText(logText.toString());
            });

            service.setOnFailed(e -> {
                articulosTableView.setDisable(false);
//                service.getException().printStackTrace();
                logTextArea.setStyle("-fx-text-fill: firebrick;");
                logTextArea.appendText("Error: " + service.getException().getLocalizedMessage() + "\n");
            });
            service.start();
        } else {
            logTextArea.setStyle("-fx-text-fill: firebrick;");
            logTextArea.appendText("Error: las ubicaciones son incorrectas.\n");
        }
    }

    public static class TableCellArticuloWithTooltip<T> extends TableCell<ArticuloProducido, T> {
        @Override
        protected void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null || empty) {
                setText(null);
                setTooltip(null);
            } else {
                setText(item.toString());
                // Dynamic tooltip
//                final String styleCode = getTableView().getItems().get(getIndex()).getStyleCode().substring(0, 8);
                final String styleCode = getTableRow().getItem().getStyleCode().substring(0, 8);
                final Tooltip tooltip = new Tooltip(styleCode);
                tooltip.setShowDelay(new javafx.util.Duration(100));
                tooltip.setShowDuration(javafx.util.Duration.INDEFINITE);
                setTooltip(tooltip);
            }
        }
    }

    public static class TableCellProducirWithTooltip<T> extends TableCell<ArticuloProducido, T> {
        @Override
        protected void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null || empty) {
                setText(null);
                setTooltip(null);
            } else {
                setText(item.toString());
                setTooltip(null);

                final Integer producir = getTableRow().getItem().getProducir();
                final Double producido = getTableRow().getItem().getDocenas();
                final String[] maquinas = getTableRow().getItem().getMaquinas();

                if (producir != null && producido != null && maquinas != null) {
                    // Dynamic tooltip
                    final int target = BigDecimal.valueOf(producir - producido)
                            .multiply(BigDecimal.valueOf(24 * 1.02))
                            .divide(BigDecimal.valueOf(maquinas.length), RoundingMode.HALF_UP)
                            .setScale(0, RoundingMode.HALF_UP)
                            .intValue();
                    if (target > 0) {
                        final Tooltip tooltip = new Tooltip("+" + target + " un./maq.");
                        tooltip.setShowDelay(new javafx.util.Duration(100));
                        tooltip.setShowDuration(javafx.util.Duration.INDEFINITE);
                        setTooltip(tooltip);
                    }
                }
            }
        }
    }

}
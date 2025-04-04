package ar.com.leo.produccion.fx.controller;

import ar.com.leo.produccion.fx.service.ArticuloProducidoTask;
import ar.com.leo.produccion.fx.service.ExcelTask;
import ar.com.leo.produccion.jdbc.DataSourceConfig;
import ar.com.leo.produccion.model.ArticuloProducido;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.print.PageLayout;
import javafx.print.PageOrientation;
import javafx.print.Paper;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class ProduccionController implements Initializable {

    @FXML
    private ComboBox<String> sectorComboBox;
    @FXML
    private DatePicker fechaInicioDatePicker;
    @FXML
    private DatePicker fechaFinDatePicker;
    @FXML
    private CheckBox actualCheckBox;
    @FXML
    private TextField articuloTextBox;
    @FXML
    private Label mensajeLabel;
    @FXML
    private Label horaLabel;
    @FXML
    private TableView<ArticuloProducido> articulosTableView;
    @FXML
    private TableColumn<ArticuloProducido, String> colArticulo;
    @FXML
    private TableColumn<ArticuloProducido, Integer> colUnidades;
    @FXML
    private TableColumn<ArticuloProducido, Double> colDocenas;
    @FXML
    private TableColumn<ArticuloProducido, String> colProduciendo;
    @FXML
    private Region region;
    @FXML
    private ProgressIndicator progress;

    private ObservableList<ArticuloProducido> articulosProducidosList;
    private ArticuloProducidoTask articuloProducidoTask;
    private ExcelTask excelTask;

    final DateTimeFormatter fromDateFormatter = DateTimeFormatter.ofPattern("[dd/MM/yyyy][dd/M/yyyy][dd/M/yy][dd/MM/yy][dd-MM-yyyy][dd-MM-yy][ddMMyyyy][ddMMyy][ddMyy]");
    final DateTimeFormatter toDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Initializes the controller after its root element has been completely processed.
     * Sets an error message if the database is not initialized, otherwise proceeds with initialization.
     *
     * @param url the location used to resolve relative paths for the root object, or null if the location is not known.
     * @param rb the resources used to localize the root object, or null if the root object was not localized.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (DataSourceConfig.dataSource == null) {
//            System.out.println("Error: Base de datos no inicializada.");
            mensajeLabel.setText("Error: Base de datos no inicializada.");
        } else {
            init();
        }
    }

    private void init() {
        // Set default locale
        Locale.setDefault(Locale.getDefault());

        Platform.runLater(() -> {
            // Initialize sector combo box with predefined values
            sectorComboBox.setItems(FXCollections.observableList(List.of("HOMBRE", "SEAMLESS")));
            sectorComboBox.getSelectionModel().select(1);

            // Set date converters for date pickers
            fechaInicioDatePicker.setConverter(new StringConverter<>() {
                @Override
                public String toString(LocalDate date) {
                    // Format date to string
                    return (date != null) ? toDateFormatter.format(date) : "";
                }

                @Override
                public LocalDate fromString(String string) {
                    // Parse string to LocalDate
                    return (string != null && !string.isEmpty()) ? LocalDate.parse(string, fromDateFormatter) : null;
                }
            });

            fechaFinDatePicker.setConverter(new StringConverter<>() {
                @Override
                public String toString(LocalDate date) {
                    // Format date to string
                    return (date != null) ? toDateFormatter.format(date) : "";
                }

                @Override
                public LocalDate fromString(String string) {
                    // Parse string to LocalDate
                    return (string != null && !string.isEmpty()) ? LocalDate.parse(string, fromDateFormatter) : null;
                }
            });

            // Set initial date values
            fechaInicioDatePicker.setValue(LocalDate.now().withDayOfMonth(1));
            fechaFinDatePicker.setValue(LocalDate.now());

            // Configure table column cell value factories
            colArticulo.setCellValueFactory(new PropertyValueFactory<>("styleCode"));
            colUnidades.setCellValueFactory(new PropertyValueFactory<>("unidades"));
            colUnidades.setCellFactory(new Callback<>() {
                @Override
                public TableCell<ArticuloProducido, Integer> call(TableColumn<ArticuloProducido, Integer> param) {
                    return new TableCell<>() {
                        @Override
                        protected void updateItem(Integer item, boolean empty) {
                            super.updateItem(item, empty);

                            if (empty || item == null) {
                                setText(null);
                                setTooltip(null);
                            } else {
                                // Get the current ArticuloProducido item
                                final ArticuloProducido articuloProducido = getTableRow().getItem();
                                if (articuloProducido != null) {
                                    setText(item.toString());
                                    // Set tooltip based on style code
                                    if (articuloProducido.getStyleCode().contains("#")) {
                                        final int unidades = articuloProducido.getUnidades() / 2;
                                        final Tooltip tooltip = new Tooltip("" + unidades);
                                        tooltip.setShowDelay(new Duration(100));
                                        setTooltip(tooltip);
                                    } else if (articuloProducido.getStyleCode().contains("%") || articuloProducido.getStyleCode().contains("$")) {
                                        final int unidades = articuloProducido.getUnidades() * 2;
                                        final Tooltip tooltip = new Tooltip("" + unidades);
                                        tooltip.setShowDelay(new Duration(100));
                                        setTooltip(tooltip);
                                    } else {
                                        setTooltip(null);
                                    }
                                }
                            }
                        }
                    };
                }
            });

            colDocenas.setCellValueFactory(new PropertyValueFactory<>("docenas"));
            colProduciendo.setCellValueFactory(new PropertyValueFactory<>("produciendo"));

            // Set row factory for table view
            articulosTableView.setRowFactory(tv -> new TableRow<>() {
                @Override
                protected void updateItem(ArticuloProducido articuloProducido, boolean empty) {
                    super.updateItem(articuloProducido, empty);
                    setStyle("");
                    if (articuloProducido != null) {
                        // Highlight rows that are producing
                        if (!articuloProducido.getProduciendo().equals("NO"))
                            setStyle("-fx-background-color: #c6d4ff;");
                    }
                }
            });

            // Request focus on articulo text box
            articuloTextBox.requestFocus();
        });
    }

    // private String getTurno(final LocalDateTime localDateTime) {
    //     int hour = localDateTime.getHour();
    //     if (hour >= 6 && hour < 14) {
    //         return "1";
    //     } else if (hour >= 14 && hour < 22) {
    //         return "2";
    //     } else {
    //         return "3";
    //     }
    // }

    private void mostrarTablaArticulos() {
        LocalDateTime fechaInicio;
        LocalDateTime fechaFin;

        // Set start date and time
        fechaInicio = fechaInicioDatePicker.getValue().atTime(6, 0, 1);

        // Set end date and time based on actual checkbox
        if (actualCheckBox.isSelected()) {
            // If checkbox is selected, set end date to now
            fechaFin = LocalDateTime.now();
        } else {
            // If checkbox is not selected, set end date and time based on date picker
            fechaFin = fechaFinDatePicker.getValue().atTime(6, 0, 0);
        }

        // Check if start date is before end date
        if (fechaInicio.isBefore(fechaFin)) {
            // Create new ArticuloProducidoTask
            articuloProducidoTask = new ArticuloProducidoTask(
                    // Get selected sector from combo box
                    sectorComboBox.getSelectionModel().getSelectedItem(),
                    // Get start date
                    fechaInicio,
                    // Get end date
                    fechaFin,
                    // Get checkbox state
                    actualCheckBox.isSelected(),
                    // Get text from text box
                    articuloTextBox.getText()
            );

            // Set on failed handler
            articuloProducidoTask.setOnFailed(event -> {
//                event.getSource().getException().printStackTrace();
                // Set error message
                mensajeLabel.setText("Error: " + event.getSource().getException().getMessage());
                System.out.println("Error: " + event.getSource().getException().getMessage());
            });

            // Set on running handler
            articuloProducidoTask.setOnRunning(event -> {
                // Disable table view
                articulosTableView.setDisable(true);
                // Show progress bar and label
                region.setVisible(true);
                progress.setVisible(true);
                // Set message label
                mensajeLabel.setText("Buscando...");
            });

            // Set on succeeded handler
            articuloProducidoTask.setOnSucceeded(event -> {
                // Create date time formatter
                final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm:ss");
                // Set message label
                mensajeLabel.setText((articuloTextBox.getText().isBlank() ? "" : "ART. " + articuloTextBox.getText() + " --> ")
                        + dateTimeFormatter.format(fechaInicio) + " al " + dateTimeFormatter.format(fechaFin));
            });

            // Set value property listener
            articuloProducidoTask.valueProperty().addListener((observable, oldValue, newValue) -> {
                // Check if new value is not null and has items
                if (newValue != null && newValue.size() > 0) {
                    // Create observable list
                    this.articulosProducidosList = FXCollections.observableArrayList(newValue);
                    // Set items on table view
                    articulosTableView.setItems(this.articulosProducidosList);
                }
                // Enable table view
                articulosTableView.setDisable(false);
                // Hide progress bar and label
                region.setVisible(false);
                progress.setVisible(false);
            });

            // Create new thread
            Thread thread = new Thread(articuloProducidoTask);
            // Set thread to daemon
            thread.setDaemon(true);
            // Start thread
            thread.start();
        } else {
            // Set error message if dates are incorrect
            mensajeLabel.setText("Fechas incorrectas.");
        }
    }

    /**
     * Handle actual checkbox click event.
     * 
     * Disable or enable the end date picker and the hour label when the checkbox is selected or deselected.
     * 
     * @param event the {@link ActionEvent} that triggered this method
     */
    @FXML
    private void handleCheckBoxActual(ActionEvent event) {
        if (actualCheckBox.isSelected()) {
            fechaFinDatePicker.setDisable(true);
            horaLabel.setDisable(true);
        } else {
            fechaFinDatePicker.setDisable(false);
            horaLabel.setDisable(false);
        }
    }

    @FXML
    private void handleButtonProduccion(ActionEvent actionEvent) {
        mensajeLabel.setText(null);
        this.mostrarTablaArticulos();
    }

    @FXML
    private void handleEnter(KeyEvent event) {
        mensajeLabel.setText(null);
        if (event.getCode() == KeyCode.ENTER) {
            this.mostrarTablaArticulos();
        }
    }

    /**
     * Handles the export button click event.
     *
     * This method is triggered when the export button is clicked. It initializes
     * the start and end dates based on the user input and the state of the 
     * "actual" checkbox. If the start date is before the end date, it creates
     * an ExcelTask to export production data to an Excel file. The task's 
     * success, failure, and running states are handled to provide feedback 
     * to the user through the mensajeLabel.
     *
     * @param actionEvent the {@link ActionEvent} that triggered this method
     * @throws IOException if an I/O error occurs during the export process
     */
    @FXML
    private void handleButtonExportar(ActionEvent actionEvent) throws IOException {
        mensajeLabel.setText(null);
        LocalDateTime fechaInicio = fechaInicioDatePicker.getValue().atTime(6, 0, 1);
        LocalDateTime fechaFin;
        if (actualCheckBox.isSelected()) {
            fechaFin = LocalDateTime.now();
        } else {
            fechaFin = fechaFinDatePicker.getValue().atTime(6, 0, 0);
        }
        if (fechaInicio.isBefore(fechaFin)) {
            excelTask = new ExcelTask(sectorComboBox.getSelectionModel().getSelectedItem(), fechaInicio, fechaFin, actualCheckBox.isSelected(), articuloTextBox.getText());
            excelTask.setOnFailed(event -> {
//                event.getSource().getException().printStackTrace();
                mensajeLabel.setText("Error: No se ha podido exportar.");
            });
            excelTask.setOnRunning(event -> {
                mensajeLabel.setText("Exportando...");
            });
            excelTask.setOnSucceeded(event -> mensajeLabel.setText("Excel generado en: " + System.getProperty("user.dir") + "\\Produccion.xls"));
            Thread thread = new Thread(excelTask);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    public void imprimir(ActionEvent event) {
        // Create a PrinterJob
        final PrinterJob job = PrinterJob.createPrinterJob();
        if (job == null) {
            mensajeLabel.setStyle("-fx-text-fill: red;");
            mensajeLabel.setText("No se encontraron impresoras.");
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
    
        // Calculate the scale factor to fit the view to the page
        Node root = articulosTableView.getScene().getRoot(); // Get the root node
        double scaleX = pageLayout.getPrintableWidth() / root.getBoundsInParent().getWidth();
        double scaleY = pageLayout.getPrintableHeight() / root.getBoundsInParent().getHeight();
        double scale = Math.min(scaleX, scaleY);
    
        // Create a new Scale object
        Scale scaleTransform = new Scale(scale, scale);
    
        // Apply the scaling transformation
        root.getTransforms().add(scaleTransform);
    
        // Print the whole view
        boolean success = job.printPage(pageLayout, root);
    
        // Remove the scaling transformation
        root.getTransforms().remove(scaleTransform);
    
        // End the job
        if (success) {
            job.endJob();
        } else {
            mensajeLabel.setStyle("-fx-text-fill: red;");
            mensajeLabel.setText("Error al imprimir.");
        }
    }


    
    /**
     * Handles the button click event to navigate to the "Máquinas" view.
     *
     * This method is triggered when the "Máquinas" button is clicked. It loads the
     * "Maquinas.fxml" file, sets a new `MaquinaController` with the selected sector
     * from the `sectorComboBox` as its controller, and replaces the current scene
     * with the loaded "Máquinas" view. It also sets the window title to "Máquinas".
     *
     * @param actionEvent the {@link ActionEvent} that triggered this method
     */
    @FXML
    private void handleButtonMaquinas(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Maquinas.fxml"));
            loader.setControllerFactory(controllerClass -> new MaquinaController(sectorComboBox.getSelectionModel().getSelectedItem()));
            AnchorPane pane = loader.load();
            Scene scene = ((Node) actionEvent.getSource()).getScene();
            Stage stage = (Stage) scene.getWindow();
            scene.setRoot(pane);
            stage.setTitle("Máquinas");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleButtonProgramada(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Programada.fxml"));
            AnchorPane pane = loader.load();
            Scene scene = ((Node) actionEvent.getSource()).getScene();
            Stage stage = (Stage) scene.getWindow();
            scene.setRoot(pane);
            stage.setTitle("Programada");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

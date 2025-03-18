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
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
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
        Locale.setDefault(Locale.getDefault());

        Platform.runLater(() -> {
            sectorComboBox.setItems(FXCollections.observableList(List.of("HOMBRE", "SEAMLESS")));
            sectorComboBox.getSelectionModel().select(1);

            fechaInicioDatePicker.setConverter(
                    new StringConverter<>() {
                        @Override
                        public String toString(LocalDate date) {
                            return (date != null) ? toDateFormatter.format(date) : "";
                        }

                        @Override
                        public LocalDate fromString(String string) {
                            return (string != null && !string.isEmpty()) ? LocalDate.parse(string, fromDateFormatter) : null;
                        }
                    });
            fechaFinDatePicker.setConverter(
                    new StringConverter<>() {
                        @Override
                        public String toString(LocalDate date) {
                            return (date != null) ? toDateFormatter.format(date) : "";
                        }

                        @Override
                        public LocalDate fromString(String string) {
                            return (string != null && !string.isEmpty()) ? LocalDate.parse(string, fromDateFormatter) : null;
                        }
                    });

            fechaInicioDatePicker.setValue(LocalDate.now().withDayOfMonth(1));
            fechaFinDatePicker.setValue(LocalDate.now());
            // Table
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
                                final ArticuloProducido articuloProducido = getTableRow().getItem();
                                if (articuloProducido != null) {
                                    setText(item.toString());
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

            articulosTableView.setRowFactory(tv -> new TableRow<>() {
                @Override
                protected void updateItem(ArticuloProducido articuloProducido, boolean empty) {
                    super.updateItem(articuloProducido, empty);
                    setStyle("");
                    if (articuloProducido != null) {
                        if (!articuloProducido.getProduciendo().equals("NO"))
                            setStyle("-fx-background-color: #c6d4ff;");
                    }
                }
            });
            articuloTextBox.requestFocus();
        });
    }

    private String getTurno(final LocalDateTime localDateTime) {
        int hour = localDateTime.getHour();
        if (hour >= 6 && hour < 14) {
            return "1";
        } else if (hour >= 14 && hour < 22) {
            return "2";
        } else {
            return "3";
        }
    }

    private void mostrarTablaArticulos() {
        LocalDateTime fechaInicio;
        LocalDateTime fechaFin;

        fechaInicio = fechaInicioDatePicker.getValue().atTime(6, 0, 1);
        if (actualCheckBox.isSelected()) {
            fechaFin = LocalDateTime.now();
        } else {
            fechaFin = fechaFinDatePicker.getValue().atTime(6, 0, 0);
        }
        if (fechaInicio.isBefore(fechaFin)) {
            articuloProducidoTask = new ArticuloProducidoTask(sectorComboBox.getSelectionModel().getSelectedItem(), fechaInicio, fechaFin, actualCheckBox.isSelected(), articuloTextBox.getText());
            articuloProducidoTask.setOnFailed(event -> {
//                event.getSource().getException().printStackTrace();
                mensajeLabel.setText("Error: " + event.getSource().getException().getMessage());
            });
            articuloProducidoTask.setOnRunning(event -> {
                articulosTableView.setDisable(true);
                region.setVisible(true);
                progress.setVisible(true);
                mensajeLabel.setText("Buscando...");
            });
            articuloProducidoTask.setOnSucceeded(event -> {
                final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm:ss");
                mensajeLabel.setText((articuloTextBox.getText().isBlank() ? "" : "\"" + articuloTextBox.getText() + "\"\n")
                        + dateTimeFormatter.format(fechaInicio) + " al " + dateTimeFormatter.format(fechaFin));
            });
            articuloProducidoTask.valueProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null && newValue.size() > 0) {
                    this.articulosProducidosList = FXCollections.observableArrayList(newValue);
                    articulosTableView.setItems(this.articulosProducidosList);
                }
                articulosTableView.setDisable(false);
                region.setVisible(false);
                progress.setVisible(false);
            });
            Thread thread = new Thread(articuloProducidoTask);
            thread.setDaemon(true);
            thread.start();
        } else {
            mensajeLabel.setText("Fechas incorrectas.");
        }
    }

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

}

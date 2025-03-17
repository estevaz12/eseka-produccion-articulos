package ar.com.leo.produccion.fx.controller;

import ar.com.leo.produccion.fx.service.MaquinaTask;
import ar.com.leo.produccion.jdbc.DataSourceConfig;
import ar.com.leo.produccion.model.Maquina;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
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
import javafx.print.PageLayout;
import javafx.print.PageOrientation;
import javafx.print.Paper;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;

import java.net.URL;
import java.time.Duration;
import java.util.Comparator;
import java.util.ResourceBundle;

public class MaquinaController implements Initializable {

    @FXML
    private TextArea logTextArea;
    @FXML
    private TableView<Maquina> maquinasTableView;
    @FXML
    private TextArea logTextArea;
    @FXML
    private TableColumn<Maquina, Integer> colMaquina;
    @FXML
    private TableColumn<Maquina, String> colArticulo;
    @FXML
    private TableColumn<Maquina, Integer> colUnidades;
    @FXML
    private TableColumn<Maquina, String> colProduccion;
    @FXML
    private TableColumn<Maquina, Integer> colTarget;
    @FXML
    private TableColumn<Maquina, String> colTiempo;
    @FXML
    private TableColumn<Maquina, String> colEstado;
    @FXML
    private Region region;
    @FXML
    private ProgressIndicator progress;

    private ObservableList<Maquina> maquinasList;
    private MaquinaTask maquinaTask;

    private final String roomCode;


    public MaquinaController(String roomCode) {
        this.roomCode = roomCode;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (DataSourceConfig.dataSource == null) {
            System.out.println("Error: Base de datos no inicializada.");
        } else {
            init();
            this.mostrarTablaMaquinas(roomCode);
        }
    }

    private void init() {
        Platform.runLater(() -> {
            // Table
            colMaquina.setCellValueFactory(new PropertyValueFactory<>("machCode"));
            colArticulo.setCellValueFactory(param -> {
                SimpleStringProperty articulo = new SimpleStringProperty();
                String styleCode = param.getValue().getStyleCode();
                if (styleCode.length() > 6) {
                    String art = styleCode.substring(0, 5);
                    String talle;
                    if (styleCode.charAt(5) == '9') {
                        talle = "PARCHE";
                    } else {
                        talle = "T." + styleCode.charAt(5);
                    }
                    String color = styleCode.substring(6, 8);
                    if (styleCode.length() > 8 && styleCode.startsWith("02", 14)) // .2
                        articulo.set(art + " " + talle + " " + color + " (.2)");
                    else if (styleCode.length() > 8 && styleCode.startsWith("06", 14)) // .6
                        articulo.set(art + " " + talle + " " + color + " (.6)");
                    else if (styleCode.length() > 8 && styleCode.startsWith("08", 14)) // .8
                        articulo.set(art + " " + talle + " " + color + " (.8)");
                    else
                        articulo.set(art + " " + talle + " " + color);
                }

                return articulo;
            });
            colUnidades.setCellValueFactory(new PropertyValueFactory<>("pieces"));
            colTarget.setCellValueFactory(new PropertyValueFactory<>("targetOrder"));
            colProduccion.setCellValueFactory(param -> {
                SimpleStringProperty produccion = new SimpleStringProperty();
                produccion.set(param.getValue().getProduccion() + "%");
                return produccion;
            });

            colTiempo.setCellValueFactory(param -> {
                SimpleStringProperty tiempo = new SimpleStringProperty();
                if (param.getValue().getTargetOrder() > 0 && param.getValue().getIdealCycle() > 0) {
                    long segundos = param.getValue().getIdealCycle() * (param.getValue().getTargetOrder() - param.getValue().getPieces());
                    Duration duration = Duration.ofSeconds(segundos);
                    if (segundos > 0) {
                        tiempo.set((duration.toDaysPart() > 0 ? duration.toDaysPart() + "d " : "") + duration.toHoursPart() + "h " + duration.toMinutesPart() + "m " + duration.toSecondsPart() + "s");
                    } else if (param.getValue().getState() != 8) {
                        tiempo.set("LLEGÓ");
                    }
                } else {
                    tiempo.set("-");
                }

                return tiempo;
            });
            colEstado.setCellValueFactory(param -> {
                SimpleStringProperty estado = new SimpleStringProperty();
                switch (param.getValue().getState()) {
                    case 0:
                        estado.set("RUN");
                        break;
                    case 1:
                        estado.set("OFF");
                        break;
                    case 2:
                        estado.set("GENERAL STOP");
                        break;
                    case 3:
                        estado.set("STOP ERROR");
                        break;
                    case 4:
                        estado.set("TARGET");
                        break;
                    case 5:
                        estado.set("NO CYCLE");
                        break;
                    case 6:
                        estado.set("ELECTRONICO");
                        break;
                    case 7:
                        estado.set("MECANICO");
                        break;
                    case 8:
                        estado.set("PRODUCCION");
                        break;
                    case 9:
                        estado.set("FALTA HILADO");
                        break;
                    case 10:
                        estado.set("FALTA REPUESTO");
                        break;
                    case 11:
                        estado.set("MUESTRA");
                        break;
                    case 12:
                        estado.set("CAMBIO ARTICULO");
                        break;
                    case 13:
                        estado.set("TURBINA");
                        break;
                    case 56:
                        estado.set("OFFLINE");
                        break;
                    case 65535:
                        estado.set("DESINCRONIZADA");
                        break;
                }
                return estado;
            });

            // SORTING
            colProduccion.setComparator(Comparator.comparingInt(s -> Integer.parseInt(s.substring(0, s.length() - 1))));
            colTiempo.setComparator((t1, t2) -> {
                int dias1 = 0;
                int horas1;
                int total1;
                if (t1 == null || t1.length() <= 1 || t1.equals("LLEGÓ")) {
                    total1 = 0;
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
                if (t2 == null || t2.length() <= 1 || t2.equals("LLEGÓ")) {
                    total2 = 0;
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

            maquinasTableView.setRowFactory(tv -> new TableRow<>() {
                @Override
                protected void updateItem(Maquina maquina, boolean empty) {
                    super.updateItem(maquina, empty);
                    String style = "";
                    if (maquina != null) {
                        if (maquina.getProduccion() >= 100 && maquina.getState() != 8) {
                            style += "-fx-font-weight: bold;";
                        }

                        if (maquina.getState() == 8 || maquina.getState() == 13)
                            style += "-fx-text-background-color: white;-fx-background-color: #9b0000;";
                        else if (maquina.getState() == 6 || maquina.getState() == 7)
                            style += "-fx-background-color: #ff0000;";
                        else if (maquina.getState() == 1 || maquina.getState() == 56 || maquina.getState() == 65535)
                            style += "-fx-background-color: #8d8d8d;";
                        else if (maquina.getState() == 9 || maquina.getState() == 10)
                            style += "-fx-background-color: #dcd900;";
                        else {
                            if (maquina.getProduccion() >= 100) {
                                style += "-fx-background-color: #be16d5;";
                            }
                        }
                    }
                    setStyle(style);
                }
            });

        });
    }

    private void mostrarTablaMaquinas(String roomCode) {
        maquinaTask = new MaquinaTask(roomCode);
        maquinaTask.setOnFailed(event -> event.getSource().getException().printStackTrace());
        maquinaTask.setOnRunning(event -> {
            maquinasTableView.setDisable(true);
            region.setVisible(true);
            progress.setVisible(true);
        });
        maquinaTask.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.size() > 0) {
                this.maquinasList = FXCollections.observableArrayList(newValue);
                maquinasTableView.setItems(this.maquinasList);
                maquinasTableView.setDisable(false);
                region.setVisible(false);
                progress.setVisible(false);
            }
        });
        Thread thread = new Thread(maquinaTask);
        thread.setDaemon(true);
        thread.start();
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

        if (maquinasTableView.getItems().size() > 0) {
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
            boolean proceed = job.showPrintDialog(maquinasTableView.getScene().getWindow());
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
//            VBox printableContent = new VBox(maquinasTableView, dateLabel);
//            printableContent.setPrefHeight(pageLayout.getPrintableHeight());
//            printableContent.setPrefWidth(pageLayout.getPrintableWidth());
////            printableContent.setAutoSizeChildren(false);
//            printableContent.setAlignment(Pos.CENTER);

            // Calculate the scale factor to fit the TableView to the page
            double scaleX = pageLayout.getPrintableWidth() / maquinasTableView.getBoundsInParent().getWidth();
            double scaleY = pageLayout.getPrintableHeight() / maquinasTableView.getBoundsInParent().getHeight();
            double scale = Math.min(scaleX, scaleY);

            // Apply the scaling transformation
            Scale scaleTransform = new Scale(scale, scale);
            maquinasTableView.getTransforms().add(scaleTransform);

            // Print the table
            boolean success = job.printPage(pageLayout, maquinasTableView);

            // Reset the table's transformations and height
            maquinasTableView.getTransforms().remove(scaleTransform);
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

}

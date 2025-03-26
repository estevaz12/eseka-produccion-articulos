package ar.com.leo.produccion.fx;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class MainApp extends Application {

    /**
     * Start the JavaFX application.
     * 
     * This function will be called after the Application class is loaded. It is
     * the entry point to the JavaFX application.
     * 
     * The function sets the window to be maximized and loads the GUI from the
     * FXML file.
     */
    @Override
    public void start(Stage stage) {
        stage.initStyle(StageStyle.DECORATED);
        final Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        // set Stage boundaries to visible bounds of the main screen
        stage.setX(primaryScreenBounds.getMinX());
        stage.setY(primaryScreenBounds.getMinY());
        stage.setWidth(primaryScreenBounds.getWidth());
        stage.setHeight(primaryScreenBounds.getHeight());
//        System.out.println(primaryScreenBounds.getWidth() + " x " + primaryScreenBounds.getHeight());
        stage.setTitle("Producción");
//        stage.centerOnScreen();
        stage.setMaximized(true);
//        stage.toFront();
//        stage.setAlwaysOnTop(true);
//        stage.setFullScreen(true);
//        stage.setResizable(false);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Produccion.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
//            LOG.error(e);
            e.printStackTrace();
        }
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // start JavaFX
        launch(args);
    }
}

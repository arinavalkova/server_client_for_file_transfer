import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import networks.Tools;

public class ClientWindow extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("clientWindow.fxml"));
        primaryStage.setScene(new Scene(root));
        primaryStage.show();

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                Tools.sendBytes(
                        ClientWindowController.DataOutputStreamGetter(),
                        "quit".getBytes(),
                        Tools.Settings.SERVICE
                );
                byte[] message = Tools.getBytes(
                        ClientWindowController.DataInputStreamGetter(),
                        Tools.Settings.SERVICE,
                        null
                );

                Tools.closeSocketConnection(
                        ClientWindowController.clientSocketGetter(),
                        ClientWindowController.DataInputStreamGetter(),
                        ClientWindowController.DataOutputStreamGetter()
                );
            }
        });
    }

    public static void main(String[] args)
    {
        launch(args);
    }
}

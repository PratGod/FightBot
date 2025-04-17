import gearth.extensions.ExtensionForm;
import gearth.extensions.ExtensionFormCreator;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class FightBotLauncher extends ExtensionFormCreator {

    @Override
    public ExtensionForm createForm(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Design.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("Optimization");
        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(true);
        primaryStage.setAlwaysOnTop(true);
        return loader.getController();
    }

    public static void main(String[] args) {
        runExtensionForm(args, FightBotLauncher.class);
    }
}
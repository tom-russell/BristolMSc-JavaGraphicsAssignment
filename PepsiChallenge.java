import javafx.util.Duration;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.animation.*;

/** Blend a coke can and a pepsi can to find the difference. */
public class PepsiChallenge extends Application {
    @Override
    public void start(Stage stage) {
        Image coke = new Image(
            "http://ddragon.leagueoflegends.com/cdn/img/champion/loading//Aatrox_0.jpg"
        );

        Image pepsi = new Image(
            "http://ddragon.leagueoflegends.com/cdn/img/champion/loading//Bard_0.jpg"
        );

        ImageView bottom = new ImageView(coke);
        ImageView top = new ImageView(pepsi);
        top.setOpacity(0.0);

        Group blend = new Group(
                bottom,
                top
        );

        HBox layout = new HBox(10);
        layout.setStyle("-fx-background-color: #000000;");
        layout.getChildren().addAll(
                new ImageView(coke),
                blend,
                new ImageView(pepsi)
        );
        layout.setPadding(new Insets(10));
        stage.setScene(new Scene(layout));
        stage.show();
        
        FadeImage(bottom, 1, 0, 0);
        FadeImage(top,    0, 1, 2000);
        
    }

    private void FadeImage(ImageView img, float start, float end, int delay) 
    {
        FadeTransition ft = new FadeTransition(Duration.millis(2000), img);
        ft.setFromValue(start);
        ft.setToValue(end);
        ft.setCycleCount(1);
        ft.setDelay(Duration.millis(delay));
        ft.play();
    }
    
    public static void main(String[] args) {
        launch();
    }
}
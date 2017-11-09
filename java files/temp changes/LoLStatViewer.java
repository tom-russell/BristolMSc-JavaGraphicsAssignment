import java.util.*;
import java.io.IOException;
import javafx.application.*;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.effect.ColorAdjust;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;

public class LoLStatViewer extends Application
{
    private RiotAPIData api;
    private SceneSetup sSetup;
    private List<String> champKeys;
    
    // Window dimensions
    private final int wWidth = 950;
    private final int wHeight = 800;
    
    // UI Element Objects
    private Scene scene;
    private BorderPane mainRoot;

    @Override
    public void start(Stage stage)
    {
        api = new RiotAPIData();
        champKeys = api.GetKeys();
        
        mainRoot = new BorderPane();
        mainRoot.setLeft(sSetup.InitLeftPanel());
        UpdateLeftPanel(champKeys.get(0));
        mainRoot.setCenter(sSetup.InitLoadUI());
        
        scene = new Scene(mainRoot, wWidth, wHeight);
        scene.getStylesheets().add("stylesheet.css");
        stage.setResizable(false);
        stage.setTitle("LoL Champion Viewer");
        stage.setScene(scene);
        stage.show();
        
        PopulateGrid();
    }

    // Set up a background task to fill the champion grid, once finished replace the loading UI with the grid
    private void PopulateGrid()
    {
        ScrollPane mainPanel = sSetup.InitGrid();
        
        Task<Void> gridTask = new Task<Void>() {
            @Override protected Void call() throws Exception 
            {
                int counter = 1;
                // Loop through each champion key, add a button and set the image & text for each
                for (String key : champKeys)
                {
                    AddToGrid(key);
                    updateProgress(counter++, champKeys.size());
                }
                return null;
            }
        };
        
        gridTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override public void handle(WorkerStateEvent t) 
            {
                mainRoot.setCenter(mainPanel);
            }
        });
        
        // Start the grid loading task running in the background with loading bar set to indicate progress
        sSetup.progBar.progressProperty().bind(gridTask.progressProperty());
        Thread th = new Thread(gridTask);
        th.setDaemon(true);
        th.start();
    }
    
    // Set up a champion button with the champion portrait & name and add to the grid
    private void AddToGrid(String key)
    {
        Button champ = new Button();
        
        String imageUrl = api.GetImageUrl(true) + "img/champion/" + key +".png";
        Image cImage = new Image(api.GetImageUrl(true) + "img/champion/" + key +".png");
        ImageView iv = new ImageView(cImage);
        iv.setFitWidth(80);
        iv.setPreserveRatio(true);
        
        champ.setText(api.GetDetails(key).get("name").toString());
        champ.setGraphic(iv);
        champ.setContentDisplay(ContentDisplay.TOP);
        TilePane.setMargin(champ, new Insets(5));
        
        // This function is called during a background task, so requires a runLater in order to modify the scene graph
        Platform.runLater(new Runnable() {
            @Override public void run() {
                sSetup.champGrid.getChildren().add(champ);
            }
        });
        
        // Each button is given the relevant champion key as the ID, so that when a button is 
        // clicked it can be identified
        champ.setId(key);
        champ.setOnAction(this::ChampClicked);
    }
    
    // When a champion button is clicked the left panel is updated with their details
    private void ChampClicked(ActionEvent event)
    {
        Button clicked = (Button) event.getSource();
        UpdateLeftPanel(clicked.getId());
    }
    
    // Update each of the UI elements & image in the 'details' left panel for the given champion key
    public void UpdateLeftPanel(String key)
    {
        Map<String,Object> champDetails = api.GetDetails(key);
        
        sSetup.name.setText(champDetails.get("name").toString());
        sSetup.title.setText(champDetails.get("title").toString());
        sSetup.bigImage.setImage(new Image(api.GetImageUrl(false) + "img/champion/loading//" + key +"_0.jpg"));
        sSetup.detailButton.setOnAction(this::ExtraDetails);
    }
    
    private void ExtraDetails(ActionEvent event)
    {
        
    }
}
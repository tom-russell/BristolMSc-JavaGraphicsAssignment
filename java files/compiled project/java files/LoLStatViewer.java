import org.json.*;
import java.util.*;
import java.io.IOException;
import javafx.application.*;
import javafx.concurrent.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.text.*;
import javafx.scene.layout.*;
import javafx.scene.image.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.paint.Color;
import javafx.geometry.*;
import javafx.util.Duration;
import javafx.animation.*;

/* Main class for the StatViewer program. Runs the javafx application and deals with all control 
 * and modification of nodes (initialisation of nodes takes place in SceneSetup class).
 */
public class LoLStatViewer extends Application
{
    private Scene scene;
    private RiotAPIData api;
    private SceneSetup ui;
    private List<String> champKeys;
    
    // Window dimensions
    private final int wWidth = 950;
    private final int wHeight = 800;
    
    @Override
    public void start(Stage stage)
    {
        api = new RiotAPIData();
        ui = new SceneSetup();
        champKeys = api.getKeys();
        
        ui.InitScene();
        
        scene = new Scene(ui.mainRoot, wWidth, wHeight, Color.web("#000c11"));
        scene.getStylesheets().add("stylesheet.css");
        stage.setResizable(false);
        stage.setTitle("LoL Champion Viewer");
        stage.setScene(scene);
        stage.show();
        
        // Initialise the left panel with the first key alphabetically
        updateLeftPanel(champKeys.get(0));
        populateGrid();
    }
    
    // Create and start a background task to fill the champion grid (background task required to prevent UI freeze 
    // because 130+ images are loaded). Once finished replace the loading UI with the populated grid
    private void populateGrid()
    {
        Task<Void> gridTask = new Task<Void>() {
            @Override protected Void call() throws Exception 
            {
                int counter = 1;
                // Loop through each champion key, add a button and set the image & text for each
                for (String key : champKeys)
                {
                    addToGrid(key);
                    updateProgress(counter++, champKeys.size());
                }
                return null;
            }
        };
        
        gridTask.setOnSucceeded(this::gridTaskFinished);
        
        // Start the grid loading task running in the background, bound to the loading bar
        ui.progBar.progressProperty().bind(gridTask.progressProperty());
        Thread th = new Thread(gridTask);
        th.setDaemon(true);
        th.start();
    }
    
    // When the grid is fully populated filled make it visible by setting it the the center of the root node
    private void gridTaskFinished(WorkerStateEvent event)
    {
        ui.mainRoot.setCenter(ui.scrollPane);
    }
    
    // Set up a champion button with the champion portrait & name, then add it to the grid
    private void addToGrid(String key)
    {
        String imageUrl = api.getImageUrl(true) + "img/champion/" + key +".png";
        Image cImage = new Image(api.getImageUrl(true) + "img/champion/" + key +".png");
        ImageView iv = new ImageView(cImage);
        iv.setFitWidth(80);
        iv.setPreserveRatio(true);
        
        Button champ = new Button();
        champ.setText(api.getDetails(key).get("name").toString());
        champ.setGraphic(iv);
        champ.setContentDisplay(ContentDisplay.TOP);
        TilePane.setMargin(champ, new Insets(5));
        
        // Each button is given a unique key for identification but the same function call when pressed
        champ.setId(key);
        champ.setOnAction(this::champClicked);
        
        // This function is called during a background task, so requires a runLater to modify the scene graph
        Platform.runLater(new Runnable() {
            @Override public void run() {
                ui.champGrid.getChildren().add(champ);
            }
        });
    }
    
    // When a champion button is clicked the left panel is updated with their details
    private void champClicked(ActionEvent event)
    {
        Button clicked = (Button) event.getSource();
        updateLeftPanel(clicked.getId());
    }
    
    // Update each of the UI elements & image in the 'details' left panel for the given champion key
    private void updateLeftPanel(String key)
    {
        JSONObject champDetails = api.getDetails(key);
        
        ui.name.setText(champDetails.get("name").toString());
        ui.title.setText(champDetails.get("title").toString());
        ui.detailButton.setOnAction(this::extraDetailsPressed);
        
        // Set the previous image as the back image and fade it out
        ui.leftPanelImage.setId(key);
        ui.prevImage.setImage(ui.currImage.getImage());
        ui.currImage.setOpacity(0);
        fadeNode(ui.prevImage, 1, 0, 1000);
        // Load in the new image, fading in when complete
        updateImage(key, true);
    }
    
    // transition to the details view by performing transition animations for the main view then loading in the 
    // detailed view
    private void extraDetailsPressed(ActionEvent event)
    {
        fadeTranslateNode(ui.leftPanel, 0, -600, 1, 0, 1000);
        ParallelTransition tt = fadeTranslateNode(ui.scrollPane, 0, 600, 1, 0, 1000);
        tt.setOnFinished(this::loadDetailView);
    }
    
    // Perform a fade animation on the given node
    private FadeTransition fadeNode(Node node, float start, float end, float time) 
    {
        FadeTransition ft = new FadeTransition(Duration.millis(time), node);
        ft.setFromValue(start);
        ft.setToValue(end);
        ft.setCycleCount(1);
        ft.play();
        
        return ft;
    }
    
    // Perform a combined translate and fade animation on the given node
    private ParallelTransition fadeTranslateNode(Node node, float startX, float endX, float fadeFrom, float fadeTo, float time)
    {
        TranslateTransition tt = new TranslateTransition(Duration.millis(time), node);
        tt.setFromX(startX);
        tt.setToX(endX);
        
        FadeTransition ft = fadeNode(node, fadeFrom, fadeTo, time);
        
        ParallelTransition pt = new ParallelTransition();
        pt.getChildren().addAll(tt, ft);
        pt.play();
        
        return pt;
    }
    
    // Updates a given imageview image in a separate task, preventing UI hang during image loading. if portraitImage 
    // is == true, the smaller image is loaded, else the larger 'splash art' image is loaded.
    private void updateImage(String key, Boolean portraitImage)
    {
        ui.portrait = portraitImage;
        
        Task<Void> imageTask = new Task<Void>() {
            @Override protected Void call() throws Exception 
            {
                String s;
                if (ui.portrait == true) s = "loading";
                else                  s = "splash";
                
                ui.loadImage = new Image(api.getImageUrl(false) + "img/champion/" + s + "//" + key +"_0.jpg");
                
                return null;
            }
        };
        imageTask.setOnSucceeded(this::imageTaskFinish);

        Thread th = new Thread(imageTask);
        th.setDaemon(true);
        th.start();
    }

    // When the image has been downloaded, set the ImageView to it and fade it in
    private void imageTaskFinish(WorkerStateEvent event)
    {
        if (ui.portrait == true) {
            ui.currImage.setImage(ui.loadImage);
            fadeNode(ui.currImage, 0, 1, 1000);
        }
        else {
            ui.background.setImage(ui.loadImage);
            fadeNode(ui.background, 0, 1, 1000);
        }
    }
    
    // Load the detail view, setting up the UI and fading in the background image
    private void loadDetailView(ActionEvent event)
    {
        ui.bgButton.setOnAction(this::showBackground);
        updateImage(ui.leftPanelImage.getId(), false);
        
        scene.setRoot(ui.detailRoot);
        fadeNode(ui.detailRoot, 0, 1, 1000);
        ui.returnButton.setOnAction(this::unloadDetailView);
        
        for (Button b : ui.abilities) {
            b.setOnAction(this::updateAbilityView);
        }
        // By default set the ability view to show the passive 
        updateAbilityView(new ActionEvent(ui.abilities.get(0), null));
    }
    
    // If the background is currently not shown, fade the detail view out so it is visible (and vice versa)
    private void showBackground(ActionEvent event)
    {
        float bOpacity, eOpacity; // begin and end opacity values
        if (ui.bgShown == false) {
            bOpacity = 1; 
            eOpacity = 0;
            ui.bgShown = true;
            ui.bgButton.setText("Show Details");
        }
        else {
            bOpacity = 0;
            eOpacity = 1;
            ui.bgShown = false;
            ui.bgButton.setText("Show Background");
        }

        // Fade out/in all of the nodes overlaying the background
        fadeNode(ui.returnButton, bOpacity, eOpacity, 1000);
        fadeNode(ui.panelLayout,  bOpacity, eOpacity, 1000);
        fadeNode(ui.box, bOpacity * ui.boxOpacity, eOpacity * ui.boxOpacity, 1000);
    }
    
    // Return back to the main view, fading out the detail view and reloading the main view once the animation finishes
    public void unloadDetailView(ActionEvent event)
    {
        FadeTransition ft = fadeNode(ui.detailRoot, 1, 0, 1000);
        ft.setOnFinished(this::reloadMainView);
    }
    
    // Transition back into the main grid view by fading in and translating the two panels inwards
    private void reloadMainView(ActionEvent event)
    {
        ui.description.getChildren().clear();
        ui.background.setImage(null);
        
        scene.setRoot(ui.mainRoot);
        
        ui.leftPanel.setOpacity(0);
        ui.scrollPane.setOpacity(0);
        ui.leftPanel.setTranslateX(0);
        ui.scrollPane.setTranslateX(0);
        fadeTranslateNode(ui.leftPanel, -250, 0, 0, 1, 1000);
        fadeTranslateNode(ui.scrollPane, 600, 0, 0, 1, 1000);
    }
    
    // Update the ability view (with the full ability title, image & description) each time a new ability is shown
    private void updateAbilityView(ActionEvent event)
    {
        Button clicked = (Button) event.getSource();
        String buttonId = clicked.getId();
        JSONObject champData = api.getDetails(ui.leftPanelImage.getId());
        Image iconImage;
        String descriptionField;
        String urlSpell;
        
        // Retrieving the correct JSONObject from the nested JSON structure (passive/Q/W/E or R)
        if (buttonId.equals("Passive")) 
        {
            champData = champData.getJSONObject("passive");
            urlSpell = "img/passive/";
        }
        else {
            JSONArray spells = champData.getJSONArray("spells");
            
            if      (buttonId.equals("Q")) champData = spells.getJSONObject(0);
            else if (buttonId.equals("W")) champData = spells.getJSONObject(1);
            else if (buttonId.equals("E")) champData = spells.getJSONObject(2);
            else if (buttonId.equals("R")) champData = spells.getJSONObject(3);
            
            urlSpell = "img/spell/";
        }
        
        // Set up the abilty image and name text
        String imgUrl = api.getImageUrl(true) + urlSpell + champData.getJSONObject("image").getString("full");
        iconImage = new Image(imgUrl);
        
        ui.abilityName.setText(champData.getString("name"));
        ui.icon.setImage(iconImage);
        
        // Clear the description of existing text, then add the text for the new ability. Separate Text nodes are used 
        // within a TextFlow to allow different font styles within the same text block.
        ui.description.getChildren().clear();
        DescriptionFormatter formatter = new DescriptionFormatter();
        Text[] textNodes = formatter.formatInput(champData);
        
        for (Text t : textNodes) {
            ui.description.getChildren().addAll(t); 
        }
    }
}
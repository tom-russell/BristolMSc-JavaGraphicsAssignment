import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

class SceneSetup 
{
    public Label name;
    public Label title;
    public ImageView bigImage;
    public Button detailButton;
    public TilePane champGrid;
    public ProgressBar progBar;
    public ScrollPane scrollPane;
    
    //Initialise the UI elements for the left panel in the main scene
    public VBox InitLeftPanel()
    {
        VBox nameAndTitle = new VBox();
        name = new Label();
        name.setId("nameLabel");
        title = new Label();
        title.setId("titleLabel");
        nameAndTitle.getChildren().addAll(name, title);
        
        bigImage = new ImageView();
        detailButton = new Button("Ability Details");
        detailButton.setPrefWidth(250);
        
        VBox leftPanel = new VBox(30);
        leftPanel.getStyleClass().add("leftPanel");
        leftPanel.getChildren().addAll(nameAndTitle, bigImage, detailButton);
        
        return leftPanel;
    }
    
    // Initialise the loading UI progress bar and text 
    public VBox InitLoadUI()
    {
        Label l = new Label("Loading data...");
        progBar = new ProgressBar(0f);
        
        VBox loadingUI = new VBox(10);
        loadingUI.getChildren().addAll(l, progBar);
        
        return loadingUI;
    }
    
    // Set up the Javafx nodes for the champion grid
    public ScrollPane InitGrid()
    {
        scrollPane = new ScrollPane();
        champGrid = new TilePane();
        champGrid.setPrefColumns(5);
        scrollPane.setContent(champGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        
        return scrollPane;
    }
}
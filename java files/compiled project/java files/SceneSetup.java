import java.util.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.*;
import javafx.scene.effect.BlendMode;
import javafx.scene.text.TextFlow;
import javafx.scene.shape.*;
import javafx.geometry.*;

/* Initialises all JavaFX nodes used by LoLStatViewer and stores a publically accessible reference to them.
 */
class SceneSetup 
{
    // The two root nodes for the two views
    public BorderPane mainRoot;
    public StackPane detailRoot;
    
    // Main view - left panel
    public VBox leftPanel;
    public Label name;
    public Label title;
    public Group leftPanelImage;
    public ImageView currImage;
    public ImageView prevImage;
    public Button detailButton;

    // Main view - centre panel
    public ProgressBar progBar;
    public ScrollPane scrollPane;
    public TilePane champGrid;
    
    // Detail view
    public Button returnButton;
    public Button bgButton;
    public ImageView background;
    public HBox panelLayout;
    public Rectangle box;
    public List<Button> abilities;
    public Label abilityName;
    public ImageView icon;
    public TextFlow description;
    
    // Other references
    public Image loadImage;
    public Boolean portrait;
    public Boolean bgShown;
    public final float boxOpacity = 0.75f;
    
    // Initialise the two root scene views for the program, for the main & detailed views
    public void InitScene()
    {
        mainRoot = new BorderPane();
        detailRoot = new StackPane();
        detailRoot.setOpacity(0);
        
        mainRoot.setLeft(InitLeftPanel());
        mainRoot.setCenter(InitLoadUI());
        InitGrid();
        InitDetailView();
    }
    
    //Initialse the main view left panel
    public VBox InitLeftPanel()
    {
        VBox nameAndTitle = new VBox();
        name = new Label();
        name.setId("nameLabel");
        title = new Label();
        title.setId("titleLabel");
        nameAndTitle.setId("leftBox");
        nameAndTitle.getChildren().addAll(name, title); 
        
        // prev & curr required as separate ImageViews to allow fading between them
        prevImage = new ImageView();
        currImage = new ImageView();
        currImage.setFitWidth(308);
        currImage.setFitHeight(560);
        leftPanelImage = new Group(prevImage, currImage);
        
        detailButton = new Button("Ability Details");
        detailButton.setPrefWidth(250);
        
        leftPanel = new VBox(30);
        leftPanel.setId("leftBox");
        leftPanel.getStyleClass().add("leftPanel");
        leftPanel.getChildren().addAll(nameAndTitle, leftPanelImage, detailButton);
        
        return leftPanel;
    }
    
    // Initialise the loading UI progress bar and text
    public VBox InitLoadUI()
    {
         
        Label l = new Label("Loading images...");
        progBar = new ProgressBar(0f);
        
        VBox loadingUI = new VBox(10);
        loadingUI.setId("loadBox");
        loadingUI.getChildren().addAll(l, progBar);
        
        return loadingUI;
    }
    
    // Set up the JavaFX layout for the champion grid (tilepane within a scrollpane)
    public void InitGrid()
    {
        scrollPane = new ScrollPane();
        champGrid = new TilePane();
        champGrid.setPrefColumns(5);
        scrollPane.setContent(champGrid);
    }
    
    // Set up the JavaFX layout for the detailed view
    public void InitDetailView()
    {
        // Champion art used as background for detail view (fades in when loaded)
        background = new ImageView();
        background.setOpacity(0);
        background.setFitHeight(810);
        background.setPreserveRatio(true);
        
        // Rectangle used as a background overlay to make text visible
        box = new Rectangle(750, 600);
        box.setOpacity(boxOpacity);
        
        // Initialise the two buttons
        returnButton = new Button("Return to Grid View");
        StackPane.setAlignment(returnButton, Pos.TOP_LEFT);
        StackPane.setMargin(returnButton, new Insets(20, 0, 0, 20));

        bgButton = new Button("Show Background");
        StackPane.setAlignment(bgButton, Pos.BOTTOM_LEFT);
        StackPane.setMargin(bgButton, new Insets(0, 0, 20, 20));
        bgShown = false;
        
        // Set up the ability buttons in a VBox, along the left side
        VBox abilityButtons = new VBox(15);
        abilityButtons.setId("buttonBox");
        abilities = new ArrayList<Button>();
        String[] abilityList = {"Passive", "Q", "W", "E", "R"};
        
        for (String ability : abilityList)
        {
            Button b = new Button(ability);
            b.setId(ability);
            abilityButtons.getChildren().add(b);
            abilities.add(b);
        }
        
        // Add a line separator between buttons and details
        Line separator = new Line(0, 0, 0, 575);
        
        // Set up the panel containing the detailed view buttons and text
        panelLayout = new HBox(15);
        panelLayout.setId("abilityPanel");
        StackPane.setAlignment(panelLayout, Pos.CENTER_LEFT);
        StackPane.setMargin(panelLayout, new Insets(0, 0, 0, 120));
        panelLayout.getChildren().addAll(abilityButtons, separator, SetupDetailPanel());
        detailRoot.getChildren().addAll(background, box, panelLayout, returnButton, bgButton);
    }
    
    // Initialise the right-hand section of the detailed view, with the ability details
    private VBox SetupDetailPanel()
    {
        abilityName = new Label();
        abilityName.setId("nameLabel");
        
        icon = new ImageView();
        icon.setId("abilityIcon");
        icon.setFitWidth(75);
        icon.setFitHeight(75);
        
        HBox abilityNameIcon = new HBox(40);
        abilityNameIcon.setAlignment(Pos.CENTER_LEFT);
        abilityNameIcon.getChildren().addAll(icon, abilityName);
        
        description = new TextFlow();
        
        VBox abilityDetails = new VBox(10);
        abilityDetails.setAlignment(Pos.TOP_CENTER);
        abilityDetails.setId("abilityDetails");
        abilityDetails.getChildren().addAll(abilityNameIcon, description);
        
        return abilityDetails;
    }
} 
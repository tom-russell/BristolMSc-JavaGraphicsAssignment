import java.util.*;
import javafx.scene.text.*;
import org.json.*;

/* DescriptionFormatter takes an ability data object from the Riot Games API as input, then formats that into an array
 * of Text object nodes. Each node has different styles applied, which is extracted from HTML tags within the ability 
 * descriptions. The HTML tags are then removed, and 'effect' placeholders are replaced with the values they reference.
 */
class DescriptionFormatter 
{
    private String[] dNodes;
    private HtmlTags[] tags;
    private String[] fontColours;
    private Text[] textNodes;
    private JSONObject champData;
    private Boolean tooltip;
    
    // the possible htmltags, IGNORE is either invalid or meaningless tags, REMOVE denotes a region of the description
    // that is invalid and must be removed
    private enum HtmlTags { IGNORE, ITALICS, UNDERLINE, BREAK, FONT, REMOVE };
    
    // Primary function of the class. Formats an ability object into a readable and styled description
    public Text[] formatInput(JSONObject data)
    {
        String input;
        champData = data;
        // If the data contains a tooltip (more detailed info) then use it, else use the description
        if (data.has("tooltip")) {
            input = data.getString("tooltip");
            tooltip = true;
        }
        else {
            input = data.getString("description");
            tooltip = false;
        }
        
        // Split the description into an array for each HTML open/close tag found 
        dNodes = input.split("<");
        
        // Initialise the arrays to store the tag and colour information, and the output array of text nodes
        tags = new HtmlTags[dNodes.length];
        fontColours = new String[dNodes.length];
        textNodes = new Text[dNodes.length];
        
        analyseHtmlTags();
        applyStyles();
        
        return textNodes;
    }
    
    private void analyseHtmlTags()
    {
        // Label each html tag present in the data, storing the tag information in a separate array
        for (int i = 0; i < dNodes.length; i++)
        {
            // Ignore this node if it is empty (such as the first node)
            if (dNodes[i].equals("") == false)
            {
                if      (dNodes[i].charAt(0) == '/')       tags[i] = HtmlTags.IGNORE; // Ignore close tags
                else if (dNodes[i].startsWith("i"))        tags[i] = HtmlTags.ITALICS;
                else if (dNodes[i].startsWith("mainText")) tags[i] = HtmlTags.IGNORE;
                else if (dNodes[i].startsWith("u"))        tags[i] = HtmlTags.UNDERLINE;
                else if (dNodes[i].startsWith("br"))       tags[i] = HtmlTags.BREAK;
                else if (dNodes[i].startsWith("span class=\"size")) tags[i] = HtmlTags.REMOVE;
                // The two font colour tags also need to store a reference to the specified colour
                else if (dNodes[i].startsWith("span") ||
                         dNodes[i].startsWith("font") ) 
                {
                    tags[i] = HtmlTags.FONT;
                    int index = dNodes[i].indexOf("\"color");
                    
                    if (index == -1) index = dNodes[i].indexOf("='#") + 3;
                    else             index += 6;

                    fontColours[i] = "#" + dNodes[i].substring(index, index + 6);
                }
            }
            
            // Cut out the remaining html tag, replace the effect placeholders and create a text object for the node
            int index = dNodes[i].indexOf('>');
            if (index != -1)
                dNodes[i] = dNodes[i].substring(index + 1);
                
            if (tooltip == true) 
                dNodes[i] = effectInsert(dNodes[i]);
            
            textNodes[i] = new Text("\u200B" + dNodes[i]); // an invisible character is inserted to fix a graphical bug
        }
    }
    
    // Where specific tags are found such as {{ e1 }}, insert the relevant values from the champion data JSON
    private String effectInsert(String dNode)
    {
        int index;

        // Replace text placeholders of the form {{ eX }}
        JSONArray effects = champData.getJSONArray("effectBurn");
        while ((index = dNode.indexOf("{{ e")) != -1)
        {
            char effectNo = dNode.charAt(index + 4);
            dNode = dNode.replace("{{ e" + effectNo + " }}", effects.getString(Character.getNumericValue(effectNo)));
        }
        
        if (champData.has("vars")) {
            // Replace text placeholders of the form {{ aX }} and {{ fX }}
            JSONArray vars = champData.getJSONArray("vars");
            while ((index = dNode.indexOf("{{ ")) != -1)
            {
                Boolean replaced = false;
                String varKey = dNode.substring(index + 3, index + 5);

                // Search for the placeholder key in the vars array, replace with the data if the key is found
                for (int i = 0; i < vars.length(); i++)
                {
                    if (vars.getJSONObject(i).getString("key").equals(varKey))
                    {
                        dNode = dNode.replace("{{ " + varKey + " }}", "" + vars.getJSONObject(i).getJSONArray("coeff").getDouble(0));
                        replaced = true;
                    }
                }
                // If the key was not found then data is missing and this reference must be removed
                if (replaced == false) dNode = removeNextPlaceholder(index, dNode);
            }
        }
        // Else the vars data is missing so loop to remove all remaining placeholders
        else {
            while ((index = dNode.indexOf("{{ ")) != -1)
            {
                dNode = removeNextPlaceholder(index, dNode);
            }
        }
        
        return dNode;
    }
    
    // Remove the next effect placeholder formatted {{ XY }}
    private String removeNextPlaceholder(int startIndex, String input)
    {
        int endIndex = input.indexOf("}}") + 2;
        
        String output = input.substring(0, startIndex) + input.substring(endIndex);
        return output;
    }
    
    // Apply the extracted HTML tag styles to each Text node
    private void applyStyles()
    {
        String previousStyle = "";
        
        for (int i = 0; i < dNodes.length; i++)
        {
            if      (tags[i] == HtmlTags.BREAK)  textNodes[i].setText("\n" + textNodes[i].getText());
            else if (tags[i] == HtmlTags.REMOVE) textNodes[i].setText("");
            
            String style = "";
            if      (tags[i] == HtmlTags.ITALICS)   style = "-fx-font-style: italic;";
            else if (tags[i] == HtmlTags.UNDERLINE) style = "-fx-underline: true;";
            else if (tags[i] == HtmlTags.FONT)      style = "-fx-fill: " + fontColours[i] + ";";
            
            style += previousStyle;
            
            // If the node is empty, we have a nested tag so apply the style to the next node instead
            if (textNodes[i].getText().length() == 1) {
                previousStyle = style;
            }
            // Else apply the style to this node as usual
            else {
                textNodes[i].setStyle(style);
                previousStyle = "";
            }
        }
    }
}
import org.json.*;
import java.net.*;
import java.util.*;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.IOException;

/* RiotAPIData contains all methods that access the RiotAPI to download data. The data is retrieved
 * and converted to and stored as JSON objects within this class, then all data is outputted as  
 * standard java variables (string/map/list).
 */
class RiotAPIData
{
    // Stores the additional champion data, to save unnecessary API calls
    private JSONObject champData;
    
    // Component strings for the image api URLs
    private String imageBaseUrl;
    private String urlVersion;
    
    // API method URL components
    private final String urlStart = "https://euw1.api.riotgames.com/lol/";
    private final String urlAPIKey = "api_key=f083d3c8-2600-454b-ba0d-ac25bf9f5a1f";
    private final String staticDataUrl = "static-data/v3/";
    
    // Main function for use only when testing Table by itself
    public static void main(String[] args) 
    {
        boolean testing = false;
        assert(testing = true);
        if (testing) {
            RiotAPIData api = new RiotAPIData();
            api.Test();
        }
    }
    
    // Constructor, makes the required API calls and stores the results in variables and objects when this class is instantiated
    RiotAPIData()
    {
        // Retrieve the champion data json from the API
        String urlString = urlStart + staticDataUrl + "champions?champListData=info&" + urlAPIKey;
        champData = APICall(urlString).getJSONObject("data");
        
        // Retrieve the up to date image url version
        urlString = urlStart + staticDataUrl + "realms?" + urlAPIKey;
        JSONObject realmData = APICall(urlString);
        imageBaseUrl = realmData.getString("cdn") + "/";
        urlVersion = realmData.getString("v") + "/";
    }
    
    // Return a JSONObject created from the JSON data returned by the API method URL
    private JSONObject APICall(String urlString)
    {
        try
        {
            // Connect to the URL
            URL url = new URL(urlString);
            HttpURLConnection request = (HttpURLConnection) url.openConnection();
            request.connect();
            
            // Read in the url page content line by line into a string
            BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));
            StringBuilder requestContent = new StringBuilder();
            
            String line;
            while ((line = br.readLine()) != null)
            {
                requestContent.append(line);
            }
            
            // Parse the string into a JSON Object and return it
            String jsonString = requestContent.toString();
            return new JSONObject(jsonString);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
    
    // Return an alphabetically sorted list of all unique key identifiers for each champion
    public List<String> GetKeys()
    {
        List<String> keyList = new ArrayList<String>(champData.keySet());
        keyList.sort(null);
        
        return keyList;
    }
    
    // Return a map of data regarding a specific champion (given their key value)
    public Map<String,Object> GetDetails(String key)
    {
        return champData.getJSONObject(key).toMap();
    }
    
    // Returns the base url for an image by retrieving the latest version numbers from the API
    // Not all urls require a version so 'withVersion' allows ommision of version from the url.
    public String GetImageUrl(Boolean withVersion)
    {
        if (withVersion == true) 
            return imageBaseUrl + urlVersion;
        else 
            return imageBaseUrl;
    }
    
    // Test function for RiotAPIData functions
    private void Test()
    {
        // Tests for APICall with invalid & valid inputs (invalid inputs don't return JSON format)
        assert(URLTest("https://www.google.com") == false);
        assert(URLTest("https://www.wikipedia.org") == false);
        assert(URLTest(urlStart + "status/v3/shard-data?" + urlAPIKey) == true);

        // Tests for GetKeys() function. Should return a sorted list of champion keys. (size of 
        // list is a rough estimate so it doesn't need to be updated)
        List<String> keyList = GetKeys();
        assert(keyList.size() > 130);
        String prevKey = "";
        for(String key : keyList) // Check the list is correctly sorted
        {
            assert(key.compareTo(prevKey) >= 0);
            prevKey = key;
        }
        
        // Tests the Map returned from GetDetails() (for two examples) against expected values from the API 
        Map<String, Object> champ = GetDetails("Aatrox");
        assert(champ.get("name").equals("Aatrox"));
        assert( (int) champ.get("id") == 266);
        assert(( (HashMap)champ.get("info") ).size() == 4);
        
        champ = GetDetails("MonkeyKing");
        assert(champ.get("name").equals("Wukong"));
        assert( (int) champ.get("id") == 62);
        assert(( (HashMap)champ.get("info") ).size() == 4);
        // Tests for GetImageUrl() function
    }
    
    // returns false if the URL has no JSON data (and an exception is thrown), else true
    private Boolean URLTest(String URL)
    {https://euw1.api.riotgames.com/lol/status/v3/shard-data?api_key=f083d3c8-2600-454b-ba0d-ac25bf9f5a1f
        try {
            JSONObject testObj = APICall(URL);
        }
        catch (JSONException e) {
            return false;
        }
        return true;
    }
}

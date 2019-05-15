package root;

import jdk.nashorn.internal.parser.JSONParser;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class GetRequests {
    public static final int queueNumber = 460;

    public List<String> createInitialRequest(boolean masterPlus, String path)  {
        String response = "";
        URIBuilder builder = new URIBuilder();
        builder.setScheme("https").setHost("na1.api.riotgames.com").setPath(path).setParameter("page", "1");
        URI uri = null;

        JSONArray jsonArr = null;
        List<String> result = new ArrayList<>();
        try {
            // get the summoner ID
            uri = builder.build();
            response = sendGet(uri.toString());
            if(masterPlus){
                JSONObject jsonObj = new JSONObject(response);
                jsonArr = jsonObj.getJSONArray("entries");
            } else {
                jsonArr = new JSONArray(response);
            }
                for (int i = 0; i < jsonArr.length() / 100; i++) {
                    // get the account ID using summoner ID
                    String summonerAccountInfo = sendGet("https://na1.api.riotgames.com/lol/summoner/v4/summoners/" +
                            jsonArr.getJSONObject(i).get("summonerId"));

                    // add account ID to return
                    JSONObject jsonObjAccount = new JSONObject(summonerAccountInfo);
                    result.add(jsonObjAccount.get("accountId").toString());
                    System.out.println(jsonObjAccount.get("accountId").toString());
                }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return result;
    }

    public void processQueueItem(Data data, DataBase dataBase, String accountId){
        Date date = new Date();
        long timeMilli = date.getTime();


        Long lastUsed = data.getAllIdsLastUsed().get(accountId);
        String result = "";
        JSONArray jsonArr = null;
        // the account hasn't ever been checked or its been more than 6 days since the account was checked
        if(lastUsed == -1 || timeMilli - lastUsed > 518400000)
            result = sendGet(String.format("https://na1.api.riotgames.com/lol/match/v4/matchlists/by-account/%s?queue=%s", accountId, queueNumber));
        else
            result = sendGet(String.format("https://na1.api.riotgames.com/lol/match/v4/matchlists/by-account/%s?queue=%s&beginTime=%s", accountId, queueNumber, lastUsed));
        try {
            JSONObject jsonObj = new JSONObject(result);
            jsonArr = jsonObj.getJSONArray("matches");
            for(int i = 0; i < jsonArr.length(); i++){
                // checks if the game ID has been processed previously before processing
                String tempId = jsonArr.getJSONObject(i).get("gameId").toString();
                if(!data.getUsedMatchIdsSet().contains(tempId)){
                    data.getUsedMatchIdsSet().add(tempId);
                    parseMatchData(tempId);
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
        // remove processed item from the queue set and update last time used
        data.getQueueSet().remove(accountId);
        data.getAllIdsLastUsed().put(accountId, timeMilli);
    }

    public void parseMatchData(String gameId){
        String result = sendGet("https://na1.api.riotgames.com/lol/match/v4/matches/" + gameId);
        try {
            JSONObject jsonObj = new JSONObject(result);
            JSONArray players = jsonObj.getJSONArray("participantIdentities");
            for(int i = 0; i < players.length(); i++){
                JSONObject playerIndexed = new JSONObject(players.get(i));
                String accountid = playerIndexed.get("summonerName").toString();
            }
        } catch (Exception e){
            System.out.println(e);
        }
    }

    public String sendGet(String url)  {
        try {
            URL obj = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) obj.openConnection();

            // optional default is GET
            connection.setRequestMethod("GET");

            //add request header
            connection.setRequestProperty("Origin", "https://developer.riotgames.com");
            connection.setRequestProperty("Accept-Charset", "application/x-www-form-urlencoded; charset=UTF-8");
            connection.setRequestProperty("X-Riot-Token", "RGAPI-4bc7778e-8802-49a4-8da7-b311dc502c1b");
            connection.setRequestProperty("Accept-Language", "en-US,en;q=0.9");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.131 Safari/537.36");


            int responseCode = connection.getResponseCode();
            String[] limits = connection.getHeaderField("X-App-Rate-Limit").split(",");
            String[] current = connection.getHeaderField("X-App-Rate-Limit-Count").split(",");

            int methodMax = Integer.parseInt(connection.getHeaderField("X-Method-Rate-Limit").substring(0,connection.getHeaderField("X-Method-Rate-Limit").indexOf(":")));
            int methodActual = Integer.parseInt(connection.getHeaderField("X-Method-Rate-Limit-Count").substring(0,connection.getHeaderField("X-Method-Rate-Limit-Count").indexOf(":")));

            // method rate limit prevention
            if(methodMax - methodActual <= 5){
                TimeUnit.SECONDS.sleep(Integer.parseInt(connection.getHeaderField("X-Method-Rate-Limit")
                        .substring(connection.getHeaderField("X-Method-Rate-Limit").indexOf(":") + 1)));
                System.out.println("method rate limit was hit");
            }

            // application rate limit prevention
            for(int i = 0; i < limits.length; i++){
                int max = Integer.parseInt(limits[i].substring(0, limits[i].indexOf(":")));
                int actual = Integer.parseInt(current[i].substring(0, current[i].indexOf(":")));
                if(max - actual <= 5){
                    int timeFrame = Integer.parseInt(limits[i].substring(limits[i].indexOf(":")+1));
                    System.out.println("application rate Limit was hit");
                    System.out.println(timeFrame);
                    TimeUnit.SECONDS.sleep(timeFrame);
                }
            }

            // if rate limit was met and a 429 was returned (something went wrong and needs to be addressed)
            if (responseCode == 429) {
                System.out.println("********************************** 429 ERROR ***************************************");
                TimeUnit.SECONDS.sleep(Integer.parseInt(connection.getHeaderField("Retry-After")) * 2);
                return sendGet(url);
            }
            System.out.println("\nSending 'GET' request to URL : " + url);
            System.out.println("Response Code : " + responseCode);
            System.out.println(connection.getHeaderField("Retry-After"));

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            //print result
            System.out.println(response);
            return response.toString();
//        JSONObject jsonObj = new JSONObject(response.toString());
//        System.out.println(jsonObj.length());


        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}

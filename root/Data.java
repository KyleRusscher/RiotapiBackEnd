package root;

import java.util.*;

public class Data {
    private String[] ranks = new String[]{"IRON","BRONZE","SILVER","GOLD","PLATINUM","DIAMOND"};
    private String[] masterPlus = new String[]{"master", "grandmaster", "challenger"};
    private String[] divisions = new String[]{"I","II","III","IV"};
    /* queue of account ids to get match ids */
    private Queue<String> summonerIdQueue;
    /* < AccountId : time last used in ms > */
    private HashMap<String, Long> allIdsLastUsed;
    /* Account ids in the queue for quick lookup */
    private HashSet<String> queueSet;
    /* Holds all the matchIds that have already been processed */
    private HashSet<String> usedMatchIdsSet;

    public Data(){
        summonerIdQueue = new LinkedList<>();
        queueSet = new HashSet<>();
        usedMatchIdsSet = new HashSet<>();
        allIdsLastUsed = new HashMap<>();
    }

    public Data(Queue<String> summonerIdQueue, HashSet<String> queueSet, HashSet<String> usedMatchIdsSet, HashMap<String, Long> allIdsLastUsed){
        this.summonerIdQueue = summonerIdQueue;
        this.queueSet = queueSet;
        this.usedMatchIdsSet = new HashSet<>();
        this.allIdsLastUsed = allIdsLastUsed;
    }

    /**
     * parses ids and adds them to queue if they are not already in the queue
     * @param ids unparsed list with bad 1st index and no trimmed ends
     */
    public void addArrayListToQueue(List<String> ids){
        List<String> result =  new ArrayList<>();
        for(int i = 0; i < ids.size(); i++){
            String currentAccountId = ids.get(i);
            if(!queueSet.contains(currentAccountId)){
                result.add(currentAccountId);
                queueSet.add(currentAccountId);
                if(!allIdsLastUsed.containsKey(currentAccountId))
                    allIdsLastUsed.put(currentAccountId, (long)-1);
            }

        }
        summonerIdQueue.addAll(result);
    }

    public HashSet<String> getUsedMatchIdsSet() {
        return usedMatchIdsSet;
    }

    public HashMap<String, Long> getAllIdsLastUsed() {
        return allIdsLastUsed;
    }

    public HashSet<String> getQueueSet() {
        return queueSet;
    }

    public String[] getRanks() {
        return ranks;
    }
    public String[] getMasterPlus() {
        return masterPlus;
    }

    public String[] getDivisions() {
        return divisions;
    }

    public Queue<String> getSummonerIdQueue() {
        return summonerIdQueue;
    }


}

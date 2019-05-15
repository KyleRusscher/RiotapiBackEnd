package root;

public class Main {
    public static void main(String[] args)  {
        Main main = new Main();
        Data data = new Data();
        GetRequests requests = new GetRequests();
        DataBase dataBase = new DataBase();

        main.initializeQueue(data,requests);
        System.out.println("done!!!!");
        main.processQueue(data, requests, dataBase);

    }

        public void initializeQueue(Data data, GetRequests requests){
            // populate stack with initial data across all ranks
            for(int rank = 0; rank < data.getRanks().length; rank++){
                for(int division = 0; division < data.getDivisions().length; division++){
                    data.addArrayListToQueue(requests.createInitialRequest(false, String.format("/lol/league/v4/entries/%s/%s/%s",
                            "RANKED_FLEX_TT", data.getRanks()[rank], data.getDivisions()[division])));
                }
            }
            // gets initial master+ people
            for(int tier = 0; tier < data.getMasterPlus().length; tier++){
                data.addArrayListToQueue(requests.createInitialRequest(true, String.format("/lol/league/v4/%sleagues/by-queue/%s",
                        data.getMasterPlus()[tier], "RANKED_FLEX_TT")));
            }
        }

        public void processQueue(Data data, GetRequests requests, DataBase dataBase){
            while(!data.getSummonerIdQueue().isEmpty()){
                // removing account id from the queue and set
                String top = data.getSummonerIdQueue().remove();
                requests.processQueueItem(data, dataBase, top);


            }
//            initializeQueue(data, requests);
//            processQueue(data, requests, dataBase);
        }






}


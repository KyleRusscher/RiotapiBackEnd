# RiotapiBackEnd
<h3>No longer being used</h3>
grabs match data from all points in ladder


RiotApiBackEnd crawls over riot api to find as many players as possible and analyzing their matches.  To alter what data is collected, the
parseMatchData method inside the GetRequests class can be modified to fit your needs. To use simply configure your own database connection
in the DataBase class and put your own api key into the headers in the sendGet method. Both production and development keys will work.
Rate limiting will cause a timeout once met and the application will continue upon completion.  

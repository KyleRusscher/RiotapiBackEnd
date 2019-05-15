package root;

import java.sql.*;

public class DataBase {
    Connection connection;
    Statement statement;
    public DataBase(){
        try {
            //step1 load the driver class
            Class.forName("oracle.jdbc.driver.OracleDriver");

            //step2 create  the connection object
            connection = DriverManager.getConnection(
                    "jdbc:oracle:thin:@localhost:1521:xe","system","oracle");

            //step3 create the statement object
            statement = connection.createStatement();

        } catch (Exception e ){
            System.out.println(e.toString());
        }
    }

}

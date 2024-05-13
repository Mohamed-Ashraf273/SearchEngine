import java.sql.*;

public class DB {

    private Connection connection;

    public DB()
    {
        String URL = "jdbc:mysql://localhost:3306/searchengine";
        String user="root";
        String password = "password";
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL,user,password);

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public boolean InsertURL(String url,String tableName)
    {
        String query = "INSERT INTO "+tableName+" (document) VALUES (?)";
        int countinserted = 0;

        try {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1,url);
            countinserted = statement.executeUpdate();
            statement.close();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if(countinserted>0)
        {
            return true;
        }
        return false;
    }
    public int clearTable(String tableName)
    {
        String query ="DELETE FROM " + tableName;
        int deletedrows=0;
        try {
            PreparedStatement statement = connection.prepareStatement(query);
            deletedrows = statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return deletedrows;
    }
    public int getCount(String tableName)
    {
        String query = "SELECT Count(*) AS count from "+tableName;
        try {
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next())
            {
                int rows = resultSet.getInt("count");
                return rows;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return -1;
    }
    public ResultSet selectURLs(String tableName)
    {
        String query = "SELECT document FROM " + tableName;
        ResultSet resultSet;
        try {
            PreparedStatement statement = connection.prepareStatement(query);
            resultSet = statement.executeQuery();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return resultSet;
    }
    public void closeConnection()
    {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


    }



}

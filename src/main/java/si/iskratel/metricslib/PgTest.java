package si.iskratel.metricslib;

import java.sql.*;
import java.util.Arrays;

public class PgTest {

    private String url = "jdbc:postgresql://elasticvm:5432/mydb";
    private String user = "postgres";
    private String password = "object00";

    public static void main(String... args) {
        PgTest pg = new PgTest();
        try {
            pg.createTable2();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        pg.parameterizedBatchUpdate();
    }

    private static final String createTableSQL = "CREATE TABLE users " +
            "(ID INT PRIMARY KEY ," +
            " NAME TEXT, " +
            " EMAIL VARCHAR(50), " +
            " COUNTRY VARCHAR(50), " +
            " PASSWORD VARCHAR(50))";


    public void createTable2() throws SQLException {

        System.out.println(createTableSQL);
        // Step 1: Establishing a Connection
        try (Connection connection = DriverManager.getConnection(url, user, password);

             // Step 2:Create a statement using connection object
             Statement statement = connection.createStatement();) {

            // Step 3: Execute the query or update query
            statement.execute(createTableSQL);
        } catch (SQLException e) {

            // print SQL exception information
            printSQLException(e);
        }
    }

    private void parameterizedBatchUpdate() {

        String INSERT_USERS_SQL = "INSERT INTO users" + "  (id, name, email, country, password) VALUES " +
                " (?, ?, ?, ?, ?);";

        try (Connection connection = DriverManager.getConnection(url, user, password);
             // Step 2:Create a statement using connection object
             PreparedStatement preparedStatement = connection.prepareStatement(INSERT_USERS_SQL)) {
            connection.setAutoCommit(false);

            preparedStatement.setInt(1, 10);
            preparedStatement.setString(2, "a");
            preparedStatement.setString(3, "a@gmail.com");
            preparedStatement.setString(4, "India");
            preparedStatement.setString(5, "secret");
            preparedStatement.addBatch();

            preparedStatement.setInt(1, 11);
            preparedStatement.setString(2, "b");
            preparedStatement.setString(3, "b@gmail.com");
            preparedStatement.setString(4, "India");
            preparedStatement.setString(5, "secret");
            preparedStatement.addBatch();

            preparedStatement.setInt(1, 12);
            preparedStatement.setString(2, "c");
            preparedStatement.setString(3, "c@gmail.com");
            preparedStatement.setString(4, "India");
            preparedStatement.setString(5, "secret");
            preparedStatement.addBatch();

            preparedStatement.setInt(1, 13);
            preparedStatement.setString(2, "d");
            preparedStatement.setString(3, "d@gmail.com");
            preparedStatement.setString(4, "India");
            preparedStatement.setString(5, "secret");
            preparedStatement.addBatch();

            int[] updateCounts = preparedStatement.executeBatch();
            System.out.println(Arrays.toString(updateCounts));
            connection.commit();
            connection.setAutoCommit(true);
        } catch (BatchUpdateException batchUpdateException) {
            printBatchUpdateException(batchUpdateException);
        } catch (SQLException e) {
            printSQLException(e);
        }
    }

    public static void printBatchUpdateException(BatchUpdateException b) {

        System.err.println("----BatchUpdateException----");
        System.err.println("SQLState:  " + b.getSQLState());
        System.err.println("Message:  " + b.getMessage());
        System.err.println("Vendor:  " + b.getErrorCode());
        System.err.print("Update counts:  ");
        int[] updateCounts = b.getUpdateCounts();

        for (int i = 0; i < updateCounts.length; i++) {
            System.err.print(updateCounts[i] + "   ");
        }
    }

    public static void printSQLException(SQLException ex) {
        for (Throwable e: ex) {
            if (e instanceof SQLException) {
                e.printStackTrace(System.err);
                System.err.println("SQLState: " + ((SQLException) e).getSQLState());
                System.err.println("Error Code: " + ((SQLException) e).getErrorCode());
                System.err.println("Message: " + e.getMessage());
                Throwable t = ex.getCause();
                while (t != null) {
                    System.out.println("Cause: " + t);
                    t = t.getCause();
                }
            }
        }
    }

}

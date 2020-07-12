package si.iskratel.metricslib;

import io.prometheus.client.Histogram;
import si.iskratel.simulator.Start;

import java.sql.*;
import java.util.Arrays;

public class PgClient {

//    private final String url = "jdbc:postgresql://localhost/mydb";
    private String url = "jdbc:postgresql://elasticvm:5432/mydb";
    private String user = "postgres";
    private String password = "object00";

    public PgClient(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }


    public void createTable(PMetric pMetric) throws SQLException {

        System.out.println(pMetric.toPgCreateTableString());
        try (Connection connection = DriverManager.getConnection(Start.PG_URL, Start.PG_USER, Start.PG_PASS);
             Statement statement = connection.createStatement();) {
            statement.execute(pMetric.toPgCreateTableString());
        } catch (SQLException e) {
            printSQLException(e);
        }
    }

    public void sendBulk(PMetric pMetric) {

        Histogram.Timer t = PromExporter.prom_bulkSendHistogram.labels("PgClient", "sendBulk").startTimer();

        String INSERT_SQL = pMetric.toPgInsertMetricString();
        System.out.println(INSERT_SQL);

        PromExporter.prom_postgresBulkInsertCount.labels("thread0").inc();

        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement preparedStatement = connection.prepareStatement(INSERT_SQL)) {
            connection.setAutoCommit(false);

            for (PTimeSeries ts : pMetric.getTimeSeries()) {

                for (int i = 0; i < pMetric.getLabelNames().length; i++) {
                    preparedStatement.setString(i + 1, ts.getLabelValues()[i]);
                }
                preparedStatement.setLong(pMetric.getLabelNames().length + 1, pMetric.getTimestamp());
                preparedStatement.setDouble(pMetric.getLabelNames().length + 2, ts.getValue());
                preparedStatement.addBatch();

            }

            int[] updateCounts = preparedStatement.executeBatch();
            System.out.println("Inserted: " + updateCounts.length);
            connection.commit();
            connection.setAutoCommit(true);

        } catch (BatchUpdateException batchUpdateException) {
            printBatchUpdateException(batchUpdateException);
        } catch (SQLException e) {
            printSQLException(e);
        }

        t.observeDuration();
        PromExporter.prom_bulkSendHistogram.labels("PgClient", "sendBulk").observe(pMetric.getTimeSeriesSize());
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
        PromExporter.prom_postgresExceptionsCount.labels("thread0").inc();
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

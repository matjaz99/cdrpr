package si.iskratel.metricslib;

import io.prometheus.client.Histogram;

import java.sql.*;

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

        System.out.println(PMetricFormatter.toPgCreateTableString(pMetric));
        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement statement = connection.createStatement();) {
            statement.execute(PMetricFormatter.toPgCreateTableString(pMetric));
        } catch (SQLException e) {
            printSQLException(e);
        }
    }

    public void sendBulk(PMetric pMetric) {

        if (pMetric.getTimeSeriesSize() == 0) {
            System.out.println("WARN: Metric " + pMetric.getName() + " contains no time-series points. It will be ignored.");
            return;
        }

        if (pMetric.getTimestamp() == 0) pMetric.setTimestamp(System.currentTimeMillis());

        Histogram.Timer t = PromExporter.metricslib_bulk_request_time.labels("PgClient", url, "sendBulk").startTimer();

        String INSERT_SQL = PMetricFormatter.toPgInsertMetricString(pMetric);
        System.out.println(INSERT_SQL);

        PromExporter.metricslib_attempted_requests_total.labels("PgClient", url).inc();

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
        PromExporter.metricslib_bulk_request_time.labels("PgClient", url, "sendBulk").observe(pMetric.getTimeSeriesSize());
    }

    private void printBatchUpdateException(BatchUpdateException b) {
        PromExporter.metricslib_failed_requests_total.labels("PgClient", url, "BatchUpdateException").inc();
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

    private void printSQLException(SQLException ex) {
        PromExporter.metricslib_failed_requests_total.labels("PgClient", url, "SQLException").inc();
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

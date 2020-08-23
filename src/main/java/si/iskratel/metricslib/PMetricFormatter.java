package si.iskratel.metricslib;

public class PMetricFormatter {

    public static String toEsNdJsonString(PMetric metric, String index) {
        StringBuilder sb = new StringBuilder();

        for (PTimeSeries ts : metric.getTimeSeries()) {
            sb.append("{ \"index\":{ \"_index\":\"").append(index).append("\"} }\n");
            sb.append("{");
            sb.append("\"m_name\":\"").append(metric.getName()).append("\",");
            for (int i = 0; i < metric.getLabelNames().length; i++) {
                sb.append("\"").append(metric.getLabelNames()[i]).append("\":\"").append(ts.getLabelValues()[i]).append("\",");
            }
            sb.append("\"value\":").append(ts.getValue()).append(",");
            sb.append("\"timestamp\":").append(metric.getTimestamp());
            sb.append("}\n");
        }

        return sb.toString();
    }

    public static String toPgCreateTableString(PMetric metric) {
        String createTableSQL = "CREATE TABLE " + metric.getName() + " (";
        createTableSQL += "ID BIGSERIAL PRIMARY KEY, ";
        for (int i = 0; i < metric.getLabelNames().length; i++) {
            createTableSQL += metric.getLabelNames()[i] + " VARCHAR(256), ";
        }
        createTableSQL += "timestamp BIGINT, value NUMERIC)";
        return createTableSQL;
    }

    public static String toPgInsertMetricString(PMetric metric) {
//        Example: INSERT INTO m_name (id, name, email, country, password) VALUES (?, ?, ?, ?, ?);
        String INSERT_SQL = "INSERT INTO " + metric.getName() + " (";
        String q = "(";
        for (int i = 0; i < metric.getLabelNames().length; i++) {
            INSERT_SQL += metric.getLabelNames()[i] + ", ";
            q += "?, ";
        }
        INSERT_SQL += "timestamp, value";
        q += "?, ?";
        INSERT_SQL += ") VALUES " + q + ");";
        return INSERT_SQL;
    }

}

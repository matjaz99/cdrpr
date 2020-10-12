package si.iskratel.metricslib;

import java.util.HashMap;
import java.util.Map;

public class PMetricFormatter {

    public static String toEsNdJsonString(PMetric metric) {

        String indexName = MetricsLib.ES_AUTO_CREATE_INDEX ? metric.getParentRegistry() + "_alias" : metric.getParentRegistry();

        StringBuilder sb = new StringBuilder();

        for (PTimeSeries ts : metric.getTimeSeries()) {
            sb.append("{ \"index\":{ \"_index\":\"").append(indexName).append("\"} }\n");
            sb.append("{");
            sb.append("\"metric_name\":\"").append(metric.getName()).append("\",");
            for (int i = 0; i < metric.getLabelNames().length; i++) {
                sb.append("\"").append(metric.getLabelNames()[i]).append("\":\"").append(ts.getLabelValues()[i]).append("\",");
            }
            sb.append("\"value\":").append(ts.getValue()).append(",");
            sb.append("\"timestamp\":").append(metric.getTimestamp());
            sb.append("}\n");
        }

        return sb.toString();
    }

    public static String toEsIndexMappingJsonString(PMetric metric) {

        // collect all labels from all metrics in registry
        Map<String, Object> allLabelsMap = new HashMap<>();
        for (PMetric m : PMetricRegistry.getRegistry(metric.getParentRegistry()).getMetricsList()) {
            for (int i = 0; i < m.getLabelNames().length; i++) {
                allLabelsMap.put(m.getLabelNames()[i], null);
            }
        }

        StringBuilder sb = new StringBuilder();

        sb.append("{\n");
        sb.append("  \"aliases\": {\n");
        sb.append("    \"${ALIAS_NAME}\": {}\n");
        sb.append("  },");

//        sb.append("  \"settings\": {\n");
//        sb.append("    \"number_of_shards\": 1,\n");
//        sb.append("    \"number_of_replicas\" : 0\n");
//        sb.append("  },");

        sb.append("  \"mappings\": {\n");
        sb.append("    \"properties\": {\n");
        sb.append("      \"metric_name\": {\"type\": \"keyword\"},\n");

        // add mapping for all labels in metric
//        for (String s : allLabelsMap.keySet()) {
//            sb.append("      \"").append(s).append("\": {\"type\": \"keyword\"},\n");
//        }

        sb.append("      \"value\": {\"type\": \"double\"},\n");
        sb.append("      \"timestamp\": {\"type\": \"date\", \"format\": \"epoch_millis\"}\n");
        sb.append("    }\n");
        sb.append("  }\n");
        sb.append("}");

        return sb.toString().replace("${ALIAS_NAME}", metric.getParentRegistry() + "_alias");
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

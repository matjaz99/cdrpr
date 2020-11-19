package si.iskratel.metricslib;

import java.util.HashMap;
import java.util.Map;

public class PMetricFormatter {

    public static String toEsNdJsonString(PMetric metric) {

        StringBuilder sb = new StringBuilder();

        for (PTimeSeries ts : metric.getTimeSeries()) {
            sb.append("{\"index\":{\"_index\":\"").append(metric.getParentRegistry()).append("\"}}\n");
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

    public static String toEsNdJsonString(PMultiValueMetric metric) {

        StringBuilder sb = new StringBuilder();

        sb.append("{\"index\":{\"_index\":\"").append(metric.getParentRegistry()).append("\"}}\n");
        sb.append("{");
        sb.append("\"metric_name\":\"").append(metric.getName()).append("\",");
        for (String key : metric.getLabelsMap().keySet()) {
            sb.append("\"").append(key).append("\":\"").append(metric.getLabelsMap().get(key)).append("\",");
        }
        for (String key : metric.getValuesMap().keySet()) {
            sb.append("\"").append(key).append("\":\"").append(metric.getValuesMap().get(key)).append("\",");
        }
        sb.append("\"value\":").append(0).append(",");
        sb.append("\"timestamp\":").append(metric.getTimestamp());
        sb.append("}\n");

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
        sb.append("  },\n");

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
        sb.append("}\n");

        return sb.toString().replace("${ALIAS_NAME}", metric.getParentRegistry() + "_alias");
    }

    public static String toIndexJson(String alias) {

        StringBuilder sb = new StringBuilder();

        sb.append("{\n");
        sb.append("  \"aliases\": {\n");
        sb.append("    \"${ALIAS_NAME}\": {\n");
        sb.append("      \"is_write_index\":true\n");
        sb.append("    }\n");
        sb.append("  }\n");
        sb.append("}\n");

        String s = sb.toString().replace("${ALIAS_NAME}", alias);
        System.out.println(s);
        return s;
    }

    /**
     * Template name must end with _tmpl.
     * @param templateName
     * @return
     */
    public static String toTemplateJson(String templateName) {

        StringBuilder sb = new StringBuilder();

        sb.append("{\n");
        sb.append("  \"order\": 10,\n");
        sb.append("  \"index_patterns\": [\"${INDEX_PATTERNS}\"],\n");
        sb.append("  \"settings\": {\n");
        sb.append("    \"index\": {\n");
        sb.append("      \"number_of_shards\" : ${NUMBER_OF_SHARDS},\n");
        sb.append("      \"number_of_replicas\" : ${NUMBER_OF_REPLICAS},\n");
        sb.append("      \"lifecycle.name\": \"pmon_ilm_policy\",\n");
        sb.append("      \"lifecycle.rollover_alias\": \"${ROLLOVER_ALIAS}\"\n");
        sb.append("    }\n");
        sb.append("  },\n");
        sb.append("  \"mappings\": {\n");
        sb.append("    \"properties\": {\n");
        sb.append("      \"metric_name\": {\"type\": \"keyword\"},\n");
        sb.append("      \"value\": {\"type\": \"double\"},\n");
        sb.append("      \"timestamp\": {\"type\": \"date\", \"format\": \"epoch_millis\"}\n");
        sb.append("    }\n");
        sb.append("  }\n");
        sb.append("}\n");

        String rolloverAlias = templateName.replace("_tmpl", "");
        String indexPattern = templateName.replace("_tmpl", "");
        if (indexPattern.endsWith("_idx")) {
            indexPattern = indexPattern.replace("_idx", "_*");
        } else {
            indexPattern = indexPattern + "*";
        }

        String s = sb.toString();
        s = s.replace("${INDEX_PATTERNS}", indexPattern);
        s = s.replace("${ROLLOVER_ALIAS}", rolloverAlias);
        s = s.replace("${NUMBER_OF_SHARDS}", Integer.toString(MetricsLib.ES_NUMBER_OF_SHARDS));
        s = s.replace("${NUMBER_OF_REPLICAS}", Integer.toString(MetricsLib.ES_NUMBER_OF_REPLICAS));
        System.out.println(s);
        return s;

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

package si.iskratel.metricslib;

import java.util.*;

public class PMetric {

    private long timestamp = System.currentTimeMillis();
    private String name;
    private String help;
    private String[] labelNames;
    private Map<String, PTimeSeries> timeSeries = new HashMap<>();

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public static PMetric build() {
        PMetric m = new PMetric();
        return m;
    }

    public PMetric setHelp(String help) {
        this.help = help;
        return this;
    }

    public String getHelp() {
        if (help == null) return "Help missing";
        return help;
    }

    public PMetric setName(String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }

    public PMetric setLabelNames(String... labels) {
        labelNames = labels;
        return this;
    }

    public String[] getLabelNames() {
        return labelNames;
    }

    public PTimeSeries setLabelValues(String... values) throws PMetricException {
        if (labelNames.length != values.length) throw new PMetricException("Number of label names is different than number of values.");
        String tsId = "";
        for (int i = 0; i < values.length; i++) {
            tsId += values[i];
        }
        PTimeSeries ts = timeSeries.getOrDefault(tsId, new PTimeSeries());
        ts.setLabelValues(values);
        timeSeries.put(tsId, ts);
        return ts;
    }

    public PMetric register() {
        PMetricRegistry.registerMetric(this);
        return this;
    }

    public void clear() {
        timeSeries.clear();
    }

    public int getTimeSeriesSize() {
        return timeSeries.size();
    }

    public List<PTimeSeries> getTimeSeries() {
        return new ArrayList<PTimeSeries>(timeSeries.values());
    }

    @Override
    public String toString() {
        String s = "CdrMetric{" + "timestamp=" + timestamp + ", name=" + name + ", timeseries={\n";
        for (Map.Entry<String, PTimeSeries> entry : timeSeries.entrySet()) {
            s += "\t" + entry.getValue().toString() + "\n";
        }
        return s + "}]";
    }

    public String toEsBulkJsonString() {
        StringBuilder sb = new StringBuilder();

        for (Map.Entry<String, PTimeSeries> entry : timeSeries.entrySet()) {
            sb.append("{ \"index\":{} }\n");
            sb.append("{");
            sb.append("\"name\":\"").append(name).append("\",");
            for (int i = 0; i < labelNames.length; i++) {
                sb.append("\"").append(labelNames[i]).append("\":\"").append(entry.getValue().getLabelValues()[i]).append("\",");
            }
            sb.append("\"value\":").append(entry.getValue().getValue()).append(",");
            sb.append("\"timestamp\":").append(timestamp);
            sb.append("}\n");
        }

        return sb.toString();
    }

    public String toPgCreateTableString() {
        String createTableSQL = "CREATE TABLE " + name + " (";
        createTableSQL += "ID BIGSERIAL PRIMARY KEY, ";
        for (int i = 0; i < labelNames.length; i++) {
            createTableSQL += labelNames[i] + " VARCHAR(256), ";
        }
        createTableSQL += "timestamp BIGINT, value NUMERIC)";
        return createTableSQL;
    }

    public String toPgInsertMetricString() {
//        Example: INSERT INTO m_name (id, name, email, country, password) VALUES (?, ?, ?, ?, ?);
        String INSERT_SQL = "INSERT INTO " + name + " (";
        String q = "(";
        for (int i = 0; i < labelNames.length; i++) {
            INSERT_SQL += labelNames[i] + ", ";
            q += "?, ";
        }
        INSERT_SQL += "timestamp, value";
        q += "?, ?";
        INSERT_SQL += ") VALUES " + q + ");";
        return INSERT_SQL;
    }
}

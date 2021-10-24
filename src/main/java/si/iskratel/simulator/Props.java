package si.iskratel.simulator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.Map;

public class Props {

    private static Logger logger = LoggerFactory.getLogger(Props.class);

    public static String HOSTNAME = "localhost0";
    public static int BULK_SIZE = 100;
    public static int SEND_INTERVAL_SEC = 60;
    public static int SIMULATOR_NUM_OF_THREADS = 1;
    public static boolean DEBUG_ENABLED = false;
    public static int RETRIES = 3;
    public static String ES_SCHEMA;
    public static String ES_BASIC_USER;
    public static String ES_BASIC_PASS;
    public static String ES_HOST;
    public static int ES_PORT;
    public static boolean ES_AUTO_CREATE_INDEX;
    public static int ES_NUMBER_OF_SHARDS = 1;
    public static int ES_NUMBER_OF_REPLICAS = 0;
    public static String PG_URL;
    public static String PG_USER;
    public static String PG_PASS;
    public static boolean PG_CREATE_TABLES_ON_START = true;
    public static String ES_INDEX_PREFIX = "";
    public static boolean EXIT_WHEN_DONE = true;
    public static String SIMULATOR_MODE;
    public static String SIMULATOR_STORAGE_TYPE;
    public static String SIMULATOR_NODEID;
    public static int SIMULATOR_CALL_DELAY = 10;
    public static int SIMULATOR_CALL_REASON = 0;
    public static int SIMULATOR_ANUM_START = 0;
    public static int SIMULATOR_ANUM_RANGE = 0;
    public static int SIMULATOR_BNUM_START = 0;
    public static int SIMULATOR_BNUM_RANGE = 0;
    public static boolean SIMULATOR_MINIMUM_DATA = false;
    public static boolean ENABLE_PROMETHEUS_METRICS = false;
    public static boolean ENABLE_DUMP_TO_FILE = false;
    public static String ALARM_DESTINATION = "http://localhost:9097/webhook";
    public static String KAFKA_BOOTSTRAP_SERVER = "centosvm:9092";

    public static void initialize() {

        Map<String, String> getenv = System.getenv();
        SIMULATOR_NUM_OF_THREADS = Integer.parseInt(getenv.getOrDefault("CDRPR_THREADS", "1"));
        SIMULATOR_CALL_DELAY = Integer.parseInt(getenv.getOrDefault("CDRPR_SIMULATOR_DELAY", "3000"));
        SIMULATOR_CALL_REASON = Integer.parseInt(getenv.getOrDefault("CDRPR_SIMULATOR_CALL_REASON", "0"));
        SIMULATOR_ANUM_START = Integer.parseInt(getenv.getOrDefault("CDRPR_SIMULATOR_ANUM_START", "100000000"));
        SIMULATOR_ANUM_RANGE = Integer.parseInt(getenv.getOrDefault("CDRPR_SIMULATOR_ANUM_RANGE", "99999999"));
        SIMULATOR_BNUM_START = Integer.parseInt(getenv.getOrDefault("CDRPR_SIMULATOR_BNUM_START", "800000000"));
        SIMULATOR_BNUM_RANGE = Integer.parseInt(getenv.getOrDefault("CDRPR_SIMULATOR_BNUM_RANGE", "99999999"));
        SIMULATOR_MINIMUM_DATA = Boolean.parseBoolean(getenv.getOrDefault("CDRPR_SIMULATOR_MINIMUM_DATA", "false"));
        SIMULATOR_NODEID = getenv.getOrDefault("CDRPR_SIMULATOR_NODEID", "" +
                "Moscow, Ljubljana, Berlin, London, Paris, Moscow, Amsterdam, Belgrade, Madrid, " +
                "Paris, Berlin, Copenhagen, Madrid, Moscow, Rome, Zurich, Lisbon, Warsaw, Berlin, Helsinki, Prague, " +
                "Vienna, London, Paris, Budapest, Zagreb, Belgrade, Kiev, Moscow, Amsterdam, Brussels, London, Paris," +
                "Moscow, Oslo, Helsinki, Dublin, Sarajevo, Skopje, Minsk, Barcelona, Lyon, Copenhagen, Stockholm, Moscow," +
                "London, Zurich, Minsk, Manchester, Frankfurt, Grenoble, Madrid, Moscow");

        // possible values:
        SIMULATOR_MODE = getenv.getOrDefault("CDRPR_SIMULATOR_MODE", "CDR_AGGS_TO_ES");
        // possible values: ELASTICSEARCH, POSTGRES
        SIMULATOR_STORAGE_TYPE = getenv.getOrDefault("CDRPR_SIMULATOR_STORAGE_TYPE", "ELASTICSEARCH");

        BULK_SIZE = Integer.parseInt(getenv.getOrDefault("CDRPR_BULK_SIZE", "50000"));
        SEND_INTERVAL_SEC = Integer.parseInt(getenv.getOrDefault("CDRPR_SEND_INTERVAL_SEC", "900"));
        DEBUG_ENABLED = Boolean.parseBoolean(getenv.getOrDefault("CDRPR_DEBUG_ENABLED", "false"));
        RETRIES = Integer.parseInt(getenv.getOrDefault("CDRPR_RETRIES", "0"));
        ES_SCHEMA = getenv.getOrDefault("CDRPR_ES_SCHEMA", "http");
        ES_BASIC_USER = getenv.getOrDefault("CDRPR_ES_BASIC_USER", "admin");
        ES_BASIC_PASS = getenv.getOrDefault("CDRPR_ES_BASIC_PASS", "admin");
        ES_HOST = getenv.getOrDefault("CDRPR_ES_HOST", "elasticvm");
        ES_PORT = Integer.parseInt(getenv.getOrDefault("CDRPR_ES_PORT", "9200"));
        ES_AUTO_CREATE_INDEX = Boolean.parseBoolean(getenv.getOrDefault("CDRPR_ES_AUTO_CREATE_INDEX", "true"));
        ES_INDEX_PREFIX = getenv.getOrDefault("CDRPR_ES_INDEX_PREFIX", "");
        ES_NUMBER_OF_SHARDS = Integer.parseInt(getenv.getOrDefault("CDRPR_ES_NUMBER_OF_SHARDS", "1"));
        ES_NUMBER_OF_REPLICAS = Integer.parseInt(getenv.getOrDefault("CDRPR_ES_NUMBER_OF_REPLICAS", "0"));
        PG_URL = getenv.getOrDefault("CDRPR_PG_URL", "jdbc:postgresql://elasticvm:5432/cdraggs");
        PG_USER = getenv.getOrDefault("CDRPR_PG_USER", "postgres");
        PG_PASS = getenv.getOrDefault("CDRPR_PG_PASS", "object00");
        PG_CREATE_TABLES_ON_START = Boolean.parseBoolean(getenv.getOrDefault("CDRPR_PG_CREATE_TABLES_ON_START", "false"));
        EXIT_WHEN_DONE = Boolean.parseBoolean(getenv.getOrDefault("CDRPR_EXIT_WHEN_DONE", "true"));
        ENABLE_PROMETHEUS_METRICS = Boolean.parseBoolean(getenv.getOrDefault("CDRPR_ENABLE_PROMETHEUS_METRICS", "false"));
        ENABLE_DUMP_TO_FILE = Boolean.parseBoolean(getenv.getOrDefault("CDRPR_DUMP_TO_FILE", "false"));
        ALARM_DESTINATION = getenv.getOrDefault("CDRPR_ALARM_DESTINATION", "http://172.29.100.32:9070/webhook");
        KAFKA_BOOTSTRAP_SERVER = getenv.getOrDefault("CDRPR_KAFKA_BOOTSTRAP_SERVER", "localhost:9092");

        try {
            HOSTNAME = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {}

        logger.info("HOSTNAME: " + HOSTNAME);
        logger.info("CURRENT_DIR: " + System.getProperty("user.dir"));
        logger.info("SIMULATOR_MODE: " + SIMULATOR_MODE);
        logger.info("NUM_OF_THREADS: " + SIMULATOR_NUM_OF_THREADS);
        logger.info("SIMULATOR_CALL_REASON: " + SIMULATOR_CALL_REASON);
        logger.info("SIMULATOR_DELAY: " + SIMULATOR_CALL_DELAY);
        logger.info("SEND_INTERVAL_SEC: " + SEND_INTERVAL_SEC);
        logger.info("BULK_SIZE: " + BULK_SIZE);
        logger.info("ES_SCHEMA: " + ES_SCHEMA);
        logger.info("ES_BASIC_USER: " + ES_BASIC_USER);
        logger.info("ES_BASIC_PASS: " + ES_BASIC_PASS);
        logger.info("ES_HOST: " + ES_HOST);
        logger.info("ES_PORT: " + ES_PORT);
        logger.info("ES_AUTO_CREATE_INDEX: " + ES_AUTO_CREATE_INDEX);
        logger.info("ES_INDEX_PREFIX: " + ES_INDEX_PREFIX);
        logger.info("ES_NUMBER_OF_SHARDS: " + ES_NUMBER_OF_SHARDS);
        logger.info("ES_NUMBER_OF_REPLICAS: " + ES_NUMBER_OF_REPLICAS);
        logger.info("PG_URL: " + PG_URL);
        logger.info("SIMULATOR_NODEID: " + SIMULATOR_NODEID);
        logger.info("KAFKA_BOOTSTRAP_SERVER: " + KAFKA_BOOTSTRAP_SERVER);

        SimulatorMetrics.defaultBulkSize.set(BULK_SIZE);
        SimulatorMetrics.maxQueueSize.set(200 * BULK_SIZE);

    }

}

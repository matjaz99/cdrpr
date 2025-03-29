package si.matjazcerkvenik.datasims.cdrpr.simulator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.time.Instant;
import java.util.Properties;

public class Props {

    private static Logger logger = LoggerFactory.getLogger(Props.class);
    public static String HOSTNAME = "localhost0";
    public static int BULK_SIZE = 100;
    public static int SEND_INTERVAL_SEC = 60;
    public static int SIMULATOR_SIMULATOR_THREADS = 1; // not used?
    public static boolean DEBUG_ENABLED = false;
    public static int RETRIES = 3;
    public static boolean CLIENT_WAIT_UNTIL_READY = true;
    public static String ES_SCHEMA;
    public static String ES_BASIC_USER;
    public static String ES_BASIC_PASS;
    public static String ES_HOST;
    public static int ES_PORT;
    public static String CDRPR_ES_INDEX_NAME;
    public static boolean ES_AUTO_CREATE_INDEX;
    public static int ES_NUMBER_OF_SHARDS = 1;
    public static int ES_NUMBER_OF_REPLICAS = 0;
    public static String PG_URL;
    public static String PG_USER;
    public static String PG_PASS;
    public static boolean PG_CREATE_TABLES_ON_START = true;
    public static String ES_INDEX_PREFIX = "";
    public static String SIMULATOR_MODE;
    public static long SIMULATOR_TIME_OFFSET_MONTHS = 0L;
    public static String SIMULATOR_START_TIME = "2024/01/01T00:00:00";
    public static long SIMULATOR_START_TIME_SECONDS = 0L;
    public static String SIMULATOR_END_TIME = "2024/12/31T23:59:59";
    public static long SIMULATOR_END_TIME_SECONDS = 0L;
    public static int SIMULATOR_SAMPLING_INTERVAL_SECONDS = 900;
    public static String SIMULATOR_STORAGE_TYPE;
    public static String SIMULATOR_NODEID;
    public static boolean SIMULATOR_EXIT_WHEN_DONE = true;
    public static String HANDLE_FILES_WHEN_PROCESSED = "nothing";
    public static int SIMULATOR_CALL_DELAY = 10;
    public static int SIMULATOR_CALL_REASON = 0;
    public static int SIMULATOR_ANUM_START = 0;
    public static int SIMULATOR_ANUM_RANGE = 0;
    public static int SIMULATOR_BNUM_START = 0;
    public static int SIMULATOR_BNUM_RANGE = 0;
    public static boolean SIMULATOR_MINIMUM_DATA = false;
    public static boolean PROMETHEUS_ENABLE_METRICS = false;
    public static boolean ENABLE_DUMP_TO_FILE = false;
    public static String ALARM_DESTINATION = "http://localhost:9097/webhook";
    public static String KAFKA_BOOTSTRAP_SERVER = "centosvm:9092";

    public static Properties releaseCausesProps;

    public static void initialize() {

        Properties getenv = new Properties();
        try {
            getenv.load(new FileInputStream("config/cdrpr.properties"));
        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
        }

//        Map<String, String> getenv = System.getenv();

        SIMULATOR_SIMULATOR_THREADS = Integer.parseInt(getenv.getOrDefault("CDRPR_SIMULATOR_THREADS", "16").toString());
        SIMULATOR_CALL_DELAY = Integer.parseInt(getenv.getOrDefault("CDRPR_SIMULATOR_DELAY", "100").toString());
        SIMULATOR_CALL_REASON = Integer.parseInt(getenv.getOrDefault("CDRPR_SIMULATOR_CALL_REASON", "0").toString());
        SIMULATOR_ANUM_START = Integer.parseInt(getenv.getOrDefault("CDRPR_SIMULATOR_ANUM_START", "100000000").toString());
        SIMULATOR_ANUM_RANGE = Integer.parseInt(getenv.getOrDefault("CDRPR_SIMULATOR_ANUM_RANGE", "99999999").toString());
        SIMULATOR_BNUM_START = Integer.parseInt(getenv.getOrDefault("CDRPR_SIMULATOR_BNUM_START", "800000000").toString());
        SIMULATOR_BNUM_RANGE = Integer.parseInt(getenv.getOrDefault("CDRPR_SIMULATOR_BNUM_RANGE", "99999999").toString());
        SIMULATOR_MINIMUM_DATA = Boolean.parseBoolean(getenv.getOrDefault("CDRPR_SIMULATOR_MINIMUM_DATA", "false").toString());
//        SIMULATOR_NODEID = getenv.getOrDefault("CDRPR_SIMULATOR_NODEID", "" +
//                "Moscow, Ljubljana, Berlin, London, Paris, Moscow, Amsterdam, Belgrade, Madrid, " +
//                "Paris, Berlin, Copenhagen, Madrid, Moscow, Rome, Zurich, Lisbon, Warsaw, Berlin, Helsinki, Prague, " +
//                "Vienna, London, Paris, Budapest, Zagreb, Belgrade, Kiev, Moscow, Amsterdam, Brussels, London, Paris," +
//                "Moscow, Oslo, Helsinki, Dublin, Sarajevo, Skopje, Minsk, Barcelona, Lyon, Copenhagen, Stockholm, Moscow," +
//                "London, Zurich, Minsk, Manchester, Frankfurt, Grenoble, Madrid, Moscow");
        SIMULATOR_NODEID = getenv.getOrDefault("CDRPR_SIMULATOR_NODEID", "" +
                "Moscow, Ljubljana, London, Berlin, London, Paris, " +
//                "Amsterdam, Belgrade, Madrid, Copenhagen, Rome, " +
//                "Zurich, Lisbon, Warsaw, Kiev, Leipzig, " +
//                "Frankfurt, Aachen, Lyon, Geneva, Salzburg, " +
//                "Prague, Warsaw, Budapest, Athens, Ankara, " +
//                "Vienna, Sofia, Skopje, Tirana, Bucurest, " +
//                "Linz, Graz, Munchen, Milano, Luxemburg, " +
                "London, Moscow, Zagreb, Bonn, Prague").toString();

        // possible values:
//        SIMULATOR_MODE = getenv.getOrDefault("CDRPR_SIMULATOR_MODE", "GENERATE_CDR_AND_STORE_ALL_TO_ES").toString();
        SIMULATOR_MODE = getenv.getOrDefault("CDRPR_SIMULATOR_MODE", "SEQUENTIAL_RANDOM_DATA_TO_OS").toString();
        SIMULATOR_TIME_OFFSET_MONTHS = Integer.parseInt(getenv.getOrDefault("SIMULATOR_TIME_OFFSET_MONTHS", "0").toString());
        SIMULATOR_START_TIME = getenv.getOrDefault("SIMULATOR_START_TIME", "2024-01-01T00:00:00Z").toString();
        SIMULATOR_START_TIME_SECONDS = Instant.parse(SIMULATOR_START_TIME).getEpochSecond();
        SIMULATOR_END_TIME = getenv.getOrDefault("SIMULATOR_END_TIME", "2024-12-31T23:59:59Z").toString();
        SIMULATOR_END_TIME_SECONDS = Instant.parse(SIMULATOR_END_TIME).getEpochSecond();
        SIMULATOR_SAMPLING_INTERVAL_SECONDS = Integer.parseInt(getenv.getOrDefault("SIMULATOR_SAMPLING_INTERVAL_SECONDS", "900").toString());
        // possible values: ELASTICSEARCH, POSTGRES
        SIMULATOR_STORAGE_TYPE = getenv.getOrDefault("CDRPR_SIMULATOR_STORAGE_TYPE", "ELASTICSEARCH").toString();
        SIMULATOR_EXIT_WHEN_DONE = Boolean.parseBoolean(getenv.getOrDefault("CDRPR_EXIT_WHEN_DONE", "true").toString());
        HANDLE_FILES_WHEN_PROCESSED = getenv.getOrDefault("CDRPR_HANDLE_FILES_WHEN_PROCESSED", "nothing").toString();
        BULK_SIZE = Integer.parseInt(getenv.getOrDefault("CDRPR_BULK_SIZE", "10000").toString());
        SEND_INTERVAL_SEC = Integer.parseInt(getenv.getOrDefault("CDRPR_SEND_INTERVAL_SEC", "60").toString());
        DEBUG_ENABLED = Boolean.parseBoolean(getenv.getOrDefault("CDRPR_DEBUG_ENABLED", "false").toString());
        RETRIES = Integer.parseInt(getenv.getOrDefault("CDRPR_RETRIES", "0").toString());
        CLIENT_WAIT_UNTIL_READY = Boolean.parseBoolean(getenv.getOrDefault("CDRPR_CLIENT_WAIT_UNTIL_READY", "false").toString());

        ES_SCHEMA = getenv.getOrDefault("CDRPR_ES_SCHEMA", "https").toString();
        ES_BASIC_USER = getenv.getOrDefault("CDRPR_ES_BASIC_USER", "admin").toString();
        ES_BASIC_PASS = getenv.getOrDefault("CDRPR_ES_BASIC_PASS", "Administrator_#123").toString();
        ES_HOST = getenv.getOrDefault("CDRPR_ES_HOST", "ubuntu-vm").toString();
        ES_PORT = Integer.parseInt(getenv.getOrDefault("CDRPR_ES_PORT", "9200").toString());
        CDRPR_ES_INDEX_NAME = getenv.getOrDefault("CDRPR_ES_INDEX_NAME", "cdrpr").toString();
        ES_AUTO_CREATE_INDEX = Boolean.parseBoolean(getenv.getOrDefault("CDRPR_ES_AUTO_CREATE_INDEX", "false").toString());
        ES_INDEX_PREFIX = getenv.getOrDefault("CDRPR_ES_INDEX_PREFIX", "").toString();
        ES_NUMBER_OF_SHARDS = Integer.parseInt(getenv.getOrDefault("CDRPR_ES_NUMBER_OF_SHARDS", "1").toString());
        ES_NUMBER_OF_REPLICAS = Integer.parseInt(getenv.getOrDefault("CDRPR_ES_NUMBER_OF_REPLICAS", "0").toString());
        PG_URL = getenv.getOrDefault("CDRPR_PG_URL", "jdbc:postgresql://elasticvm:5432/cdraggs").toString();
        PG_USER = getenv.getOrDefault("CDRPR_PG_USER", "postgres").toString();
        PG_PASS = getenv.getOrDefault("CDRPR_PG_PASS", "object00").toString();
        PG_CREATE_TABLES_ON_START = Boolean.parseBoolean(getenv.getOrDefault("CDRPR_PG_CREATE_TABLES_ON_START", "false").toString());
        PROMETHEUS_ENABLE_METRICS = Boolean.parseBoolean(getenv.getOrDefault("CDRPR_ENABLE_PROMETHEUS_METRICS", "false").toString());
        ENABLE_DUMP_TO_FILE = Boolean.parseBoolean(getenv.getOrDefault("CDRPR_DUMP_TO_FILE", "true").toString());
        ALARM_DESTINATION = getenv.getOrDefault("CDRPR_ALARM_DESTINATION", "http://172.29.100.32:9070/webhook").toString();
        KAFKA_BOOTSTRAP_SERVER = getenv.getOrDefault("CDRPR_KAFKA_BOOTSTRAP_SERVER", "localhost:9092").toString();

        try {
            HOSTNAME = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {}

        logger.info("CONFIGURATION:");
        logger.info("- HOSTNAME: " + HOSTNAME);
        logger.info("- CURRENT_TIMESTAMP: " + System.currentTimeMillis());
        logger.info("- CURRENT_DIR: " + System.getProperty("user.dir"));
        logger.info("- SIMULATOR_MODE: " + SIMULATOR_MODE);
        logger.info("- SIMULATOR_SIMULATOR_THREADS: " + SIMULATOR_SIMULATOR_THREADS);
        logger.info("- SIMULATOR_CALL_REASON: " + SIMULATOR_CALL_REASON);
        logger.info("- SIMULATOR_DELAY: " + SIMULATOR_CALL_DELAY);
        logger.info("- SEND_INTERVAL_SEC: " + SEND_INTERVAL_SEC);
        logger.info("- SIMULATOR_START_TIME: " + SIMULATOR_START_TIME);
        logger.info("- SIMULATOR_START_TIME_MILLIS: " + SIMULATOR_START_TIME_SECONDS);
        logger.info("- SIMULATOR_END_TIME: " + SIMULATOR_END_TIME);
        logger.info("- SIMULATOR_END_TIME_MILLIS: " + SIMULATOR_END_TIME_SECONDS);
        logger.info("- SIMULATOR_SAMPLING_INTERVAL_SECONDS: " + SIMULATOR_SAMPLING_INTERVAL_SECONDS);
        logger.info("- BULK_SIZE: " + BULK_SIZE);
        logger.info("- ES_SCHEMA: " + ES_SCHEMA);
        logger.info("- ES_BASIC_USER: " + ES_BASIC_USER);
        logger.info("- ES_BASIC_PASS: " + ES_BASIC_PASS);
        logger.info("- ES_HOST: " + ES_HOST);
        logger.info("- ES_PORT: " + ES_PORT);
        logger.info("- ES_AUTO_CREATE_INDEX: " + ES_AUTO_CREATE_INDEX);
        logger.info("- ES_INDEX_PREFIX: " + ES_INDEX_PREFIX);
        logger.info("- ES_NUMBER_OF_SHARDS: " + ES_NUMBER_OF_SHARDS);
        logger.info("- ES_NUMBER_OF_REPLICAS: " + ES_NUMBER_OF_REPLICAS);
        logger.info("- PG_URL: " + PG_URL);
        logger.info("- SIMULATOR_NODEID: " + SIMULATOR_NODEID);
        logger.info("- KAFKA_BOOTSTRAP_SERVER: " + KAFKA_BOOTSTRAP_SERVER);

        SimulatorMetrics.defaultBulkSize.set(BULK_SIZE);
        SimulatorMetrics.maxQueueSize.set(200 * BULK_SIZE);

    }


    public static void loadReleaseCauses() {
        try {
            releaseCausesProps = new Properties();
            releaseCausesProps.load(new FileInputStream("config/call_release_causes.properties"));
        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
        }
    }

}

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Properties;
import java.util.UUID;

class Main {
    private static Connection connection;
    private static PreparedStatement stmt;
    private static final String url =
            "jdbc:hive2://bigdata129.depts.bingosoft.net:22129/user31_db";
    private static final String user = "user31";
    private static final String password = "pass@bingo31";
    private static final String driver = "org.apache.hive.jdbc.HiveDriver";

    static {
        try {
            Class.forName(driver);
            connection = DriverManager.getConnection(url, user, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // kafka参数
    public static String inputTopic = "mn_buy_ticket_1";
    public static String bootstrapServers = "bigdata35.depts.bingosoft.net:29035,bigdata36.depts.bingosoft.net:29036,bigdata37.depts.bingosoft.net:29037";

    public static void main(String[] args) {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        Properties kafkaProperties = new Properties();
        kafkaProperties.put("bootstrap.servers", bootstrapServers);
        kafkaProperties.put("group.id", UUID.randomUUID().toString());
        kafkaProperties.put("auto.offset.reset", "earliest");
        kafkaProperties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        kafkaProperties.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        FlinkKafkaConsumer010<String> kafkaConsumer = new FlinkKafkaConsumer010<>(inputTopic,
                new SimpleStringSchema(), kafkaProperties);
        kafkaConsumer.setCommitOffsetsOnCheckpoints(true);
        DataStream<String> inputKafkaStream = env.addSource(kafkaConsumer);

        inputKafkaStream.map(new MapFunction<String, Object>() {
            @Override
            public Object map(String s) throws Exception {
                String[] splits = s.split(",");
                String sql = "INSERT INTO " +
                        "VALUES (?,?,?,?,?)";
                stmt = connection.prepareStatement(sql);
                for (int i = 0; i < splits.length; ++i) {
                    stmt.setString(i + 1, splits[i]);
                }
                System.out.println(stmt.toString());
                stmt.executeUpdate();
                System.out.println("成功插入数据：" + s);
                return null;
            }
        });

        try {
            env.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

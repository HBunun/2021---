import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;

public class Main {
    private static Connection connection;
    private static Statement statement;
    private static final String url =
            "jdbc:hive2://bigdata129.depts.bingosoft.net:22129/user31_db";
    private static final String user = "user31";
    private static final String password = "pass@bingo31";
    private static final String driver = "org.apache.hive.jdbc.HiveDriver";

    //  kafka参数
    public static String topic = "mn_buy_ticket_1";
    public static String bootstrapServers = "bigdata35.depts.bingosoft.net:29035,bigdata36.depts.bingosoft.net:29036,bigdata37.depts.bingosoft.net:29037";

    public static void main(String[] args) {
        ArrayList<String> data = readFromMysql();
        produceToKafka(data);
    }

    public static ArrayList<String> readFromMysql(){
        ArrayList<String> data = null;
        //  加载驱动类
        try {
            Class.forName(driver);
            connection = DriverManager.getConnection(url, user, password);
            statement = connection.createStatement();
            String sql = "select * from nmsl";
            data = new ArrayList<>();
            ResultSet rs = statement.executeQuery(sql);

            ResultSetMetaData rsmd = rs.getMetaData();

            while (rs.next()) {
                StringBuilder rowData = new StringBuilder();
                for (int i = 0; i < rsmd.getColumnCount(); ++i) {
                    rowData.append(rs.getString(i + 1));
                    if (i != rsmd.getColumnCount() - 1) {
                        rowData.append(",");
                    }
                }
                data.add(rowData.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

    public static void produceToKafka(ArrayList<String> data) {
        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrapServers);
        props.put("acks", "all");
        props.put("key.serializer", "org.apache.kafka.common." +
                "serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common." +
                "serialization.StringSerializer");
        KafkaProducer<String, String> producer = new KafkaProducer<>(props);

        for (String s: data) {
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, null, s);
            System.out.println("开始生产数据：" + s);
            producer.send(record);
        }

        producer.flush();
        producer.close();
    }
}

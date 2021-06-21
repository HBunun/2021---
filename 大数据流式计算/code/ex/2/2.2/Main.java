import java.util.*;

import com.bingocloud.util.json.JSONObject;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010;


public class Main {

    public static int recordAmount=0;
    public static HashMap<String,Integer> count=new HashMap<String, Integer>();
    public static void main(String[] args) throws Exception {

        String inputTopic = "mn_buy_ticket_1";
        String bootstrapServers = "bigdata35.depts.bingosoft.net:29035,bigdata36.depts.bingosoft.net:29036,bigdata37.depts.bingosoft.net:29037";

        StreamExecutionEnvironment env=StreamExecutionEnvironment.getExecutionEnvironment();
        Properties kafkaProperties=new Properties();
        kafkaProperties.put("bootstrap.servers", bootstrapServers);
        kafkaProperties.put("group.id", UUID.randomUUID().toString());
        kafkaProperties.put("auto.offset.reset", "earliest");
        kafkaProperties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        kafkaProperties.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        FlinkKafkaConsumer010 kafkaConsumer = new FlinkKafkaConsumer010<>(inputTopic,new SimpleStringSchema(), kafkaProperties);
        kafkaConsumer.setCommitOffsetsOnCheckpoints(true);
        DataStreamSource<String> inputKafkaStream = env.addSource(kafkaConsumer);

        inputKafkaStream.map(x->{
            try {
                recordAmount++;
                JSONObject obj=new JSONObject(x);
                String des=obj.getString("destination");
                if(count.containsKey(des)==false){
                    count.put(des,1);
                }else {
                    int cnt=count.get(des);
                    count.put(des,cnt+1);
                }

                if(recordAmount%1000==0){
                    System.out.println(recordAmount);
                }
                if (recordAmount==53000){
                    Map<String, Integer> map = count;
                    List<Map.Entry<String, Integer>> list = new ArrayList<Map.Entry<String, Integer>>(map.entrySet());
                    Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
                        @Override
                        public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                            return o2.getValue().compareTo(o1.getValue());
                        }
                    });

                    int i = 0;
                    for (Map.Entry<String, Integer> mapping : list) {
                        if(i==5)break;
                        i++;
                        System.out.println(mapping.getKey() + ":" + mapping.getValue());
                    }
                }
                return x;
            }
            catch (Exception e){
            };

            return null;
        });

        env.execute();
    };
};

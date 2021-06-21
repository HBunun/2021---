import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.time._
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows


object Main {
  val target="b"
  def main(args: Array[String]) {
    val total=0;

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    //Linux or Mac:nc -l 9999
    //Windows:nc -l -p 9999

    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

    val text = env.socketTextStream("localhost", 9999)
    val counts = text.flatMap {
      _.toLowerCase.split("\\W+") filter {
        _.contains(target)
      }
    }.map {
      ("带有b的单词60秒内出现次数(右侧单词为该时间段第一个输入的单词)：",_,1)
    } .keyBy(0)
      .window(TumblingProcessingTimeWindows.of(Time.seconds(5)))
      .sum(2)

    counts.print();

    env.execute("Window Stream WordCount")
  }
}
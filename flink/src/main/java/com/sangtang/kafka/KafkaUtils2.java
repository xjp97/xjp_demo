package com.sangtang.kafka;

import com.alibaba.fastjson.JSON;
import com.sangtang.pojo.Student;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

/**
 * 往kafka中写数据
 * 可以使用这个main函数进行测试一下
 * weixin: zhisheng_tian
 * blog: http://www.54tianzhisheng.cn/
 */
public class KafkaUtils2 {
    public static final String broker_list = "192.168.237.129:9092";
    public static final String topic = "flink";  //kafka topic 需要和 flink 程序用同一个 topic

    public static void writeToKafka() throws InterruptedException {
        Properties props = new Properties();
        props.put("bootstrap.servers", broker_list);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        KafkaProducer producer = new KafkaProducer<String, String>(props);

        for (int i = 1; i <= 10; i++) {
            Student student = null;
            if (i%2==0){
                student = new Student(i, "zhisheng" + i, "password" + i, 18 + i);
            }else {
                student = new Student(i, "zhisheng" + i, "password" + i, 18 );
            }
            ProducerRecord record = new ProducerRecord<String, String>(topic, null, null, JSON.toJSONString(student));
            producer.send(record);
            System.out.println("发送数据: " + JSON.toJSONString(student));
        }



        producer.flush();
    }

    public static void main(String[] args) throws InterruptedException {
        writeToKafka();
    }
}
package com.sangtang.text;

import com.alibaba.fastjson.JSON;
import com.sangtang.pojo.Student;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer09;

import java.util.Properties;

/**
 * Desc:
 * weixin: zhisheng_tian
 * blog: http://www.54tianzhisheng.cn/
 */
public class Main3 {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        Properties props = new Properties();
        props.put("bootstrap.servers", "192.168.237.129:9092");
        props.put("zookeeper.connect", "192.168.237.129:2181");
        props.put("group.id", "metric-group");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("auto.offset.reset", "latest");

        SingleOutputStreamOperator<Student> student = env.addSource(new FlinkKafkaConsumer09<>(
                "flink",   //这个 kafka topic 需要和上面的工具类的 topic 一致
                new SimpleStringSchema(),
                props)).setParallelism(1)
                .map(string -> JSON.parseObject(string, Student.class)); //Fastjson 解析字符串成 student 对象
        /*//map的使用
        SingleOutputStreamOperator<Student> map = student.map(new MapFunction<Student, Student>() {
            @Override
            public Student map(Student value) throws Exception {
                Student s1 = new Student();
                s1.id = value.id;
                s1.name = value.name + "aaa";
                s1.password = value.password;
                s1.age = value.age + 5;
                return s1;
            }
        });*/

       /* //flatmap的使用,打印年龄为奇数的
        SingleOutputStreamOperator<Student> map = student.flatMap(new FlatMapFunction<Student, Student>() {
            @Override
            public void flatMap(Student student, Collector<Student> collector) throws Exception {

                if (student.age % 2 == 1){

                    collector.collect(student);
                }


            }
        });*/

        KeyedStream<Student, Integer> map = student.keyBy(new KeySelector<Student, Integer>() {
            @Override
            public Integer getKey(Student student) throws Exception {

                return student.age;
            }
        });


        map.print();

        //map.addSink(new SinkToMySQL()); //数据
        // sink 到 mysql

        env.execute("Flink add sink");
    }
}
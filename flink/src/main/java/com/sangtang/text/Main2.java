package com.sangtang.text;

import com.sangtang.flink.SourceFromMySQL;
import com.sangtang.pojo.Student;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.PrintSinkFunction;

/**
 * Desc:
 * weixi: zhisheng_tian
 * blog: http://www.54tianzhisheng.cn/
 */
public class Main2 {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStreamSource<Student> source = env.addSource(new SourceFromMySQL());

        source.addSink(new PrintSinkFunction<>());


        env.execute("Flink add data sourc");
    }
}
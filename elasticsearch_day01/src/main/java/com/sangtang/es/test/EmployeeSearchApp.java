package com.sangtang.es.test;

import com.google.common.collect.Maps;
import org.apache.lucene.queryparser.flexible.core.util.StringUtils;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.ExtendedBounds;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.avg.AvgAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.avg.InternalAvg;
import org.elasticsearch.search.aggregations.metrics.cardinality.Cardinality;
import org.elasticsearch.search.aggregations.metrics.max.InternalMax;
import org.elasticsearch.search.aggregations.metrics.max.MaxAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.sum.InternalSum;
import org.elasticsearch.search.aggregations.metrics.sum.SumAggregationBuilder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.junit.Test;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.*;

public class EmployeeSearchApp {

    private TransportClient client;

    //初始化连接
    private void init() throws Exception {
        Settings settings = Settings.EMPTY;
        client = new PreBuiltTransportClient(settings).
                addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("10.111.32.76"), 10229));

    }

    @Test
    public void groupMutilFields() throws Exception {
        init();
        //1：构建查询提交
        SearchRequestBuilder builder = client.prepareSearch("detect").setTypes("history");
        //2：指定聚合条件
        TermsAggregationBuilder aggregationBuilder = AggregationBuilders.terms("taskId").field("taskId.keyword")
                .subAggregation(AggregationBuilders.cardinality("alarmCount").field("eventId.keyword")).size(10000);
        //3:将聚合条件放入查询条件中
        //builder.addAggregation(aggregationBuilder);
        //4:执行action，返回searchResponse
        SearchResponse searchResponse = client
                .prepareSearch("detect")
                .setTypes("history")
                .addAggregation(aggregationBuilder)
                .get();
        //6:取出聚合的字段
        Terms terms = searchResponse.getAggregations().get("taskId");
        //7:对聚合的字段进行迭代
        Iterator<? extends Terms.Bucket> iterator = terms.getBuckets().iterator();
        while (iterator.hasNext()) {
            Terms.Bucket next = iterator.next();
            //8:获取所有子聚合
            Cardinality cardinality = next.getAggregations().get("alarmCount");

            long value = cardinality.getValue();
            System.out.println("id:" + next.getKey().toString() + "事件:" + value);
        }
    }

    /**
     * 聚合去重
     *
     * @throws Exception
     */
    @Test
    public void text1() throws Exception {

        init();
        SearchRequestBuilder builder = client.prepareSearch("detect").setTypes("history");

       /* TermsAggregationBuilder taskId = AggregationBuilders.terms("taskId").field("taskId.keyword");
        TermsAggregationBuilder eventIdCount = AggregationBuilders.terms("eventId_count").field("eventId.keyword");
*/
        TermsAggregationBuilder aggregationBuilder = AggregationBuilders.terms("taskId").field("taskId.keyword")
                .subAggregation(AggregationBuilders.cardinality("alarmCount").field("eventId.keyword")).size(10000);

        builder.addAggregation(aggregationBuilder);
        SearchResponse searchResponse = builder.execute().actionGet();

        Map<String, Aggregation> aggMap = searchResponse.getAggregations().asMap();

        StringTerms teamAgg = (StringTerms) aggMap.get("taskId");

        Iterator<StringTerms.Bucket> iterator = teamAgg.getBuckets().iterator();

        while (iterator.hasNext()) {

            StringTerms.Bucket next = iterator.next();
            Cardinality alarmCount = next.getAggregations().get("alarmCount");
            Object key = next.getKey();
            long docCount = next.getDocCount();
            long value = alarmCount.getValue();
            System.out.println("id : " + key + ";event : " + value);

        }

    }

    /**
     * 求最大值最小值以此类推
     * @throws Exception
     */
    @Test
    public void text2() throws Exception {
        init();
        SearchRequestBuilder builder = client.prepareSearch("detect").setTypes("history");
        TermsAggregationBuilder taskId = AggregationBuilders.terms("taskId").field("taskId.keyword");
        MaxAggregationBuilder quality = AggregationBuilders.max("quality").field("scenarioInfos.quality");

        builder.addAggregation(taskId.subAggregation(quality));

        SearchResponse searchResponse = builder.execute().actionGet();

        Map<String, Aggregation> aggregationMap = searchResponse.getAggregations().asMap();

        StringTerms teamAgg = (StringTerms)aggregationMap.get("taskId");

        Iterator<StringTerms.Bucket> iterator = teamAgg.getBuckets().iterator();

        while (iterator.hasNext()){

            StringTerms.Bucket next = iterator.next();

            long docCount = next.getDocCount();
            Object key = next.getKey();

            Map<String, Aggregation> stringAggregationMap = next.getAggregations().asMap();

            double value = ((InternalMax) stringAggregationMap.get("quality")).getValue();

            System.out.println("key : "+key+": docCount :"+docCount+":value"+value);
        }
    }


    @Test
    public void text3() throws Exception {
        init();
        SearchRequestBuilder builder = client.prepareSearch("detect").setTypes("history");

        TermsAggregationBuilder order = AggregationBuilders.terms("taskId").field("taskId.keyword").order(Terms.Order.aggregation("quality",false));
        SumAggregationBuilder quality = AggregationBuilders.sum("quality").field("camera.type");

        builder.addAggregation(order.subAggregation(quality));

        SearchResponse searchResponse = builder.execute().actionGet();

        Map<String, Aggregation> aggregationMap = searchResponse.getAggregations().asMap();

        StringTerms teamAgg = (StringTerms)aggregationMap.get("taskId");

        Iterator<StringTerms.Bucket> iterator = teamAgg.getBuckets().iterator();

        while (iterator.hasNext()){

            StringTerms.Bucket next = iterator.next();

            long docCount = next.getDocCount();
            Object key = next.getKey();

            Map<String, Aggregation> stringAggregationMap = next.getAggregations().asMap();

            double value = ((InternalSum) stringAggregationMap.get("quality")).getValue();

            System.out.println("key : "+key+": docCount :"+docCount+": value : "+value);
        }

    }

    @Test
    public void getTaskAlarmCount() throws Exception {

        init();
        List<Long> taskIds = new ArrayList<>();
        taskIds.add(12L);
        taskIds.add(11L);

        Long[] timeRange = new Long[2];
        timeRange[0] = 1575475200000L;
        timeRange[1] = 1576718728963L;

        BoolQueryBuilder builder = QueryBuilders.boolQuery();
        builder.must(QueryBuilders.termsQuery("taskId.keyword", taskIds));
        builder.must(QueryBuilders.existsQuery("eventId.keyword"));
        builder.must(QueryBuilders.termQuery("isAlarm", "1"));
        builder.must(QueryBuilders.termQuery("platform.keyword", "SCA"));

        // 根据日期聚合
        DateHistogramAggregationBuilder field = AggregationBuilders.dateHistogram("alarm").field("occurTime")
                .subAggregation(AggregationBuilders.cardinality("countEventId").field("eventId.keyword").precisionThreshold(4000));


        field.dateHistogramInterval(DateHistogramInterval.DAY);
        // 强制返回空的数据的日期,ES这个类的聚合本身不返回空数据
        field.minDocCount(0);
        // 指定时区
        field.offset("-8h");
        // 指定时间间隔
        field.extendedBounds(new ExtendedBounds(timeRange[0], timeRange[1]));


        SearchRequestBuilder searchRequestBuilder = client.prepareSearch("detect")
                .setTypes("history")
                .setQuery(builder)
                .addAggregation(field)
                .setSize(0);

        // date -> count : yyyy-MM-dd -> num
        Map<String, Long> alarmCount = Maps.newLinkedHashMap();

        SearchResponse response = searchRequestBuilder.execute().actionGet();
        Histogram histogram  = response.getAggregations().get("alarm");

        for (Histogram.Bucket entry : histogram.getBuckets()) {
            Cardinality value = entry.getAggregations().get("countEventId");
            String keyAsString = entry.getKeyAsString();
            String s = stampToTime(keyAsString);
            // 得到告警数
            long alarmNum = value.getValue();
            System.out.println("时间: "+s+"告警数: "+alarmNum);

        }
    }


    private String stampToTime(String s) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = simpleDateFormat.format(new Date(Long.parseLong(String.valueOf(s))));
        return date;
    }

}
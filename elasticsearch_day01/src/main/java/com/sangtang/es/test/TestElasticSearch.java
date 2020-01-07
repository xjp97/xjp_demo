package com.sangtang.es.test;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.junit.Test;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;

public class TestElasticSearch {

    private TransportClient client;

    /**
     * 新建索引
     * @throws Exception
     */
    @Test
    public void test1() throws Exception{
        client = new PreBuiltTransportClient(Settings.EMPTY).
                addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"),9300));
        // 方案二：组织Document数据（使用ES的api构建json）
        // {id:1,title:"xxx",content:"xxxxxx"}
        XContentBuilder builder = XContentFactory.jsonBuilder().startObject()
                .field("id", 1)
                .field("title", "ElasticSearch是一个基于Lucene的搜索服务器。")
                .field("content", "它提供了一个分布式多用户能力的全文搜索引擎，基于RESTful web接口。Elasticsearch是用Java开发的，并作为Apache许可条款下的开放源码发布，" +
                        "是当前流行的企业级搜索引擎。设计用于云计算中，能够达到实时搜索，稳定，可靠，快速，安装使用方便。")
                .endObject();
        //创建索引、创建文档类型、设置唯一主键。同时创建文档
        client.prepareIndex("blog1","article","1").setSource(builder).get();//执行动作
        //关闭资源
        client.close();
    }
    @Test
    public void text2() throws Exception {
        client = new PreBuiltTransportClient(Settings.EMPTY).
                addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"),9300));
        XContentBuilder builder = XContentFactory.jsonBuilder().startObject()
                .field("id",1)
                .field("title","我吃大芒果")
                .field("content","吃饭不吃西瓜,芒果要剥皮")
                .endObject();
        client.prepareIndex("blog2","artic").setId("3").setSource(builder).get();
        client.close();

    }

    @Test
    public void text3() throws Exception {
        client = new PreBuiltTransportClient(Settings.EMPTY).
                addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"),9300));

        GetResponse getResponse = client.prepareGet("blog2", "artic", "3").get();
        System.out.println(getResponse.getSourceAsString());
        client.close();

    }

    /**
     * 查询全部
     * @throws Exception
     */
    @Test
    public void text4() throws Exception {
        client = new PreBuiltTransportClient(Settings.EMPTY).
                addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"),9300));
        SearchResponse searchResponse = client.prepareSearch("blog1").setTypes("article").setQuery(QueryBuilders.matchAllQuery()).get();

        SearchHits hits = searchResponse.getHits();

        for (SearchHit hit : hits) {
            String sourceAsString = hit.getSourceAsString();
            System.out.println(sourceAsString);
            Object title = hit.getSource().get("title");
            System.out.println(title);
        }
        client.close();

    }

    /**
     * 字符串查询
     * @throws Exception
     */
    @Test
    public void text5() throws Exception {
        client = new PreBuiltTransportClient(Settings.EMPTY).
                addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"),9300));

        SearchResponse searchResponse = client.prepareSearch("blog1").setTypes("article")
                .setQuery(QueryBuilders.queryStringQuery("芒果").field("title")).get();

        SearchHits hits = searchResponse.getHits();

        System.out.println(hits);

        Iterator<SearchHit> iterator = hits.iterator();

        while (iterator.hasNext()){
            SearchHit next = iterator.next();
            System.out.println(next.getSourceAsString());
            System.out.println(next.getSource().get("title"));
        }

    }

    /**
     * 词条查询
     * @throws Exception
     */
    @Test
    public void text6() throws Exception {
        client = new PreBuiltTransportClient(Settings.EMPTY).
                addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"),9300));

        SearchResponse searchResponse = client.prepareSearch("blog1").setTypes("article")
                .setQuery(QueryBuilders.wildcardQuery("title", "*芒果*")).get();

        SearchHits hits = searchResponse.getHits();

        System.out.println(hits);

        Iterator<SearchHit> iterator = hits.iterator();

        while (iterator.hasNext()){
            SearchHit next = iterator.next();
            System.out.println(next.getSourceAsString());
            System.out.println(next.getSource().get("title"));
        }

    }




}

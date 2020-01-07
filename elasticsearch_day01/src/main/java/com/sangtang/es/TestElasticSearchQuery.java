package com.sangtang.es;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sangtang.es.vo.Article;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.junit.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TestElasticSearchQuery {

    private TransportClient client;

    //初始化连接
    private void init() throws Exception {
        Settings settings = Settings.EMPTY;
        client = new PreBuiltTransportClient(settings).
                addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"), 9300));
    }

    //条件查询
    @Test
    public void test1() throws Exception {
        init();
        SearchResponse searchResponse = client.prepareSearch("blog1").setTypes("article")
                .setQuery(QueryBuilders.termQuery("title","搜")).get();

        SearchHits hits = searchResponse.getHits();
        System.out.println("一共"+hits.getTotalHits()+"个");

        Iterator<SearchHit> iterator = hits.iterator();

        while (iterator.hasNext()){
            SearchHit next = iterator.next();
            System.out.println(next.getSourceAsString());
            System.out.println(next.getSource().get("title"));
        }

        client.close();
    }

    @Test
    public void test2() throws Exception{
        init();

        //json格式对象转换器
        ObjectMapper objectMapper = new ObjectMapper();
        for (int i = 1; i <= 100; i++) {
            // 描述json 数据
            Article article = new Article();
            article.setId(i);
            article.setTitle(i + "搜索工作其实很快乐");
            article.setContent(i + "我们希望我们的搜索解决方案要快，我们希望有一个零配置和一个完全免费的搜索模式，我们希望能够简单地使用JSON通过HTTP的索引数据" +
                    "，我们希望我们的搜索服务器始终可用，我们希望能够一台开始并扩展到数百，我们要实时搜索，" +
                    "我们要简单的多租户，我们希望建立一个云的解决方案。Elasticsearch旨在解决所有这些问题和更多的问题。");
            client.prepareIndex("blog1","article",article.getId().toString())
                    .setSource(objectMapper.writeValueAsString(article)).get();

        }
        client.close();
    }
    //组合查询
    @Test
    public void test3() throws Exception{
        init();
        SearchRequestBuilder requestBuilder = client.prepareSearch("blog1").setTypes("article").setQuery(QueryBuilders.boolQuery()
                .must(QueryBuilders.termQuery("title", "搜"))
                .must(QueryBuilders.rangeQuery("id").from("1").to("10"))
                .must(QueryBuilders.wildcardQuery("content", "Elastics*ch".toLowerCase())));

        SearchResponse searchResponse = requestBuilder.get();

        SearchHits hits = searchResponse.getHits();
        Iterator<SearchHit> iterator = hits.iterator();

        while (iterator.hasNext()){
            SearchHit next = iterator.next();
            System.out.println(next.getSourceAsString());
            System.out.println(next.getSource().get("title"));
        }

        client.close();
    }
    //分页排序
    @Test
    public void test4() throws Exception{
        init();
        SearchRequestBuilder searchRequestBuilder = client.prepareSearch("blog1").setTypes("article").setQuery(QueryBuilders.matchAllQuery());

        searchRequestBuilder.setFrom(0).setSize(20);

        searchRequestBuilder.addSort("id", SortOrder.ASC);

        SearchResponse response = searchRequestBuilder.get();

        SearchHits hits = response.getHits();
        Iterator<SearchHit> iterator = hits.iterator();

        while (iterator.hasNext()){
            SearchHit next = iterator.next();
            System.out.println(next.getSourceAsString());
            System.out.println(next.getSource().get("title"));
        }


        client.close();
    }

}

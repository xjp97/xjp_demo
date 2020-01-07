package com.sangtang.es.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sangtang.es.vo.Article;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class TestElasticSearchCRUD {

    private TransportClient client;

    //初始化连接
    private void init() throws UnknownHostException {
        Settings settings = Settings.EMPTY;
        client = new PreBuiltTransportClient(settings).
                addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"), 9300));
    }

    @Test
    //创建索引
    public void test1() throws Exception{
        // 创建Client连接对象
        init();
        //创建名称为blog2的索引
        client.admin().indices().prepareCreate("blog1").get();
        //释放资源
        client.close();
    }

    @Test
    //删除索引
    public void test2() throws Exception{
        // 创建Client连接对象
        init();
        //创建名称为blog2的索引
        client.admin().indices().prepareDelete("blog1").get();
        //释放资源
        client.close();
    }

    @Test
    public void test3() throws Exception{
        init();
        //client.admin().indices().prepareCreate("blog").get();
        // 添加映射
        XContentBuilder factory = XContentFactory.jsonBuilder()
                .startObject()
                    .startObject("article")
                        .startObject("properties")
                            .startObject("id")
                                .field("type","long")
                            .endObject()
                            .startObject("title")
                                .field("type","text")
                                .field("store","true")
                                .field("analyzer","ik_smart")
                            .endObject()
                            .startObject("content")
                                .field("type","text")
                                .field("store","true")
                                .field("analyzer","ik_smart")
                             .endObject()
                        .endObject()
                    .endObject()
                .endObject();

        //创建映射
        PutMappingRequest source = Requests.putMappingRequest("blog1")
                .type("article").source(factory);
        client.admin().indices().putMapping(source);

        client.close();

    }

    //增加
    @Test
    public void test4() throws Exception {
        init();
        XContentBuilder builder = XContentFactory.jsonBuilder()
                .startObject()
                .field("id",1)
                .field("title","ElasticSearch是一个基于Lucene的搜索服务器123")
                .field("content","它提供了一个分布式多用户能力的全文搜索引擎，基于RESTful web接口。Elasticsearch是\n" +
                        "用Java开发的，并作为Apache许可条款下的开放源码发布，是当前流行的企业级搜索引擎。设计用于云计算中，能\n" +
                        "够达到实时搜索，稳定，可靠，快速，安装使用方便。")
                .endObject();

        client.prepareIndex("blog1","article","1").setSource(builder).get();
        client.close();

    }

    //实体类转换新增
    @Test
    public void test5() throws Exception{
        init();
        Article article = new Article();
        article.setId(3);
        article.setTitle("搜索工作其实很快乐");
        article.setContent("我们希望我们的搜索解决方案要快，我们希望有一个零配置和一个完全免费的搜索模式，我们希望能够简单地使用JSON通过HTTP的索引数据" +
                "，我们希望我们的搜索服务器始终可用，我们希望能够一台开始并扩展到数百，我们要实时搜索，" +
                "我们要简单的多租户，我们希望建立一个云的解决方案。Elasticsearch旨在解决所有这些问题和更多的问题。");

        ObjectMapper mapper = new ObjectMapper();
        client.prepareIndex("blog1","article",article.getId().toString())
                .setSource(mapper.writeValueAsBytes(article),XContentType.JSON)
                .get();
        //关闭资源
        client.close();

    }

    //修改
   @Test
    public void test6() throws Exception {
        init();
       Article article = new Article();
       article.setId(2);
       article.setTitle("update修改：搜索工作我很喜欢");
       article.setContent("update修改：我们希望我们的搜索解决方案要快，我们希望有一个零配置和一个完全免费的搜索模式，我们希望能够简单地使用JSON通过HTTP的索引数据" +
               "，我们希望我们的搜索服务器始终可用，我们希望能够一台开始并扩展到数百，我们要实时搜索，" +
               "我们要简单的多租户，我们希望建立一个云的解决方案。Elasticsearch旨在解决所有这些问题和更多的问题。");
       ObjectMapper objectMapper = new ObjectMapper();

       client.prepareUpdate("blog1","article",article.getId().toString())
               .setDoc(objectMapper.writeValueAsString(article)).get();

       client.close();

   }
    //修改
   @Test
    public void test7() throws Exception{
        init();
       Article article = new Article();
       article.setId(3);
       article.setTitle("update修改：搜索工作我很喜欢");
       article.setContent("update修改：我们希望我们的搜索解决方案要快，我们希望有一个零配置和一个完全免费的搜索模式，我们希望能够简单地使用JSON通过HTTP的索引数据" +
               "，我们希望我们的搜索服务器始终可用，我们希望能够一台开始并扩展到数百，我们要实时搜索，" +
               "我们要简单的多租户，我们希望建立一个云的解决方案。Elasticsearch旨在解决所有这些问题和更多的问题。");
       ObjectMapper objectMapper = new ObjectMapper();

       client.update(new UpdateRequest("blog1","article",article.getId().toString())
                .doc(objectMapper.writeValueAsBytes(article),XContentType.JSON)).get();

       client.close();

   }
    //删除
   @Test
    public void test8() throws Exception{
        init();
        client.prepareDelete("blog1","article","4").get();

        client.close();

    }

    //删除
    @Test
    public void test9() throws Exception{
        init();

        client.delete(new DeleteRequest("blog1","article","3")).get();

        client.close();
    }


}

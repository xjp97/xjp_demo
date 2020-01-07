package com.sangtang.es.test;

import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class text {


    @Test
    public void text3() {

        List list = new ArrayList();
        list.add("a");
        list.add("b");
        list.add("monkey");
        list.add("d");
        list.add("e");

        // 使用ListIterator解决上面的问题
        ListIterator listIter = list.listIterator();
        while (listIter.hasNext()) {
            String str = (String) listIter.next();
            if (str.equals("monkey")) {
                // 使用list迭代器向集合中添加元素
                listIter.set(1);
            }
        }
        System.out.println(list);
    }


}

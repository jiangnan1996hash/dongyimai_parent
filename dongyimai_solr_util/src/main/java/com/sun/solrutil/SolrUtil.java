package com.sun.solrutil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.github.promeg.pinyinhelper.Pinyin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.stereotype.Component;
import com.sun.mapper.TbItemMapper;
import com.sun.pojo.TbItem;
import com.sun.pojo.TbItemExample;


@Component
public class SolrUtil {

    @Autowired
    private TbItemMapper tbItemMapper;

    @Autowired
    private SolrTemplate solrTemplate;


    /**
     * 导入所有审核成功的商品信息到索引库
     */
    public void importItemData(){
        //创建一个查询的条件
        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        //查询已经审核的商品，上架的商品
        criteria.andStatusEqualTo("1");

        List<TbItem> itemList = tbItemMapper.selectByExample(example);
        System.out.println("=========商品展示============");
        for(TbItem tbItem:itemList){
            System.out.println(tbItem.getTitle());
            //首先取出规格数据，并且进行装换，将json字符串转换成Map集合
            Map<String,String> sepcMap = JSON.parseObject(tbItem.getSpec(), Map.class);
            Map<String, String> mapPinyin = new HashMap<String, String>();
            for (String key:sepcMap.keySet()) {
                mapPinyin.put(Pinyin.toPinyin(key,"").toLowerCase(),sepcMap.get(key));
            }
            tbItem.setSpecMap(mapPinyin);
        }

        solrTemplate.saveBeans(itemList);
        solrTemplate.commit();
        System.out.println("==========导入结束==========");
    }

    //删除solr中的数据
    public void delete(){

        Query query=new SimpleQuery("*:*");
        solrTemplate.delete(query);
        solrTemplate.commit();
    }


    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath*:spring/applicationContext*.xml");
        SolrUtil solrUtil = (SolrUtil) context.getBean("solrUtil");
        solrUtil.importItemData();
        //删除数据
//        solrUtil.delete();
    }





}


package com.sun.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.promeg.pinyinhelper.Pinyin;
import com.sun.pojo.TbItem;
import com.sun.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service(timeout=90000)//timeout 超时时间
public class ItemSearchServiceImpl implements ItemSearchService {


    @Autowired
    private SolrTemplate solrTemplate;

    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 搜索
     * @param searchMap
     * @return
     */
    @Override
    public Map<String, Object> search(Map searchMap) {

        Map<String, Object> map = new HashMap<String, Object>();
//        Query simpleQuery = new SimpleQuery();
//        //is:基于分词后的结果和传入的参数匹配
//        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
//        //添加查询条件
//        simpleQuery.addCriteria(criteria);
//
//        ScoredPage<TbItem> page = solrTemplate.queryForPage(simpleQuery, TbItem.class);
//
//        map.put("rows",page.getContent());
        //1.查询列表
        map.putAll(searchList(searchMap));//key：rows

        //2.根据关键字查询商品分类
        List categoryList = searchCategoryList(searchMap);
        map.put("categoryList",categoryList); // key:categoryList

        //3.查询品牌和规格列表
//        if(categoryList.size()>0){
//            map.putAll(searchBrandAndSpecList((String)categoryList.get(0))); //keys:brandList和specList
//        }

        //3、根据商品类目查询对应的品牌、规格
        //读取分类名称
        String categoryName = (String) searchMap.get("category");
        if(!"".equals(categoryName)){
            //按照分类名称重新读取对应品牌，规格
            map.putAll(searchBrandAndSpecList(categoryName));
        }else{
            if(categoryList.size()>0){
                Map mapBrandAndSpec = searchBrandAndSpecList((String) categoryList.get(0));
                map.putAll(mapBrandAndSpec);
            }
        }
        return map;
    }

    /**
     * 导入索引库数据
     * @param list
     */
    @Override
    public void importList(List<TbItem> list) {
        //解析数据
        for(TbItem item:list){
            System.out.println(item.getTitle());
            //从数据库中提取规格json字符串转换为map
            Map<String,String> specMap = JSON.parseObject(item.getSpec(), Map.class);
            Map<String, String> map = new HashMap<String, String>();
            for(String key:specMap.keySet()){
                map.put("item_spec_"+Pinyin.toPinyin(key, "").toLowerCase(), specMap.get(key));
            }
            item.setSpecMap(map);	//给带动态域注解的字段赋值
        }
        //导入索引库
        solrTemplate.saveBeans(list);
        solrTemplate.commit();
    }

    /**
     * 删除数据根据Id
     * @param goodsIdList
     */
    @Override
    public void deleteByGoodsIds(List goodsIdList) {
        System.out.println("删除商品ID"+goodsIdList);
        //创建查询器
        Query query = new SimpleQuery();

        Criteria criteria = new Criteria("item_goodsid").in(goodsIdList);
        query.addCriteria(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();

    }


    /**
     * 根据关键字，对查询结果进行高亮显示
     * @param searchMap
     * @return
     */
    private Map searchList(Map searchMap){
        Map map = new HashMap();

        //1.创建一个可以支持高亮查询查询器对象
        SimpleHighlightQuery query = new SimpleHighlightQuery();

        //2.设定需要高亮处理的字段
        HighlightOptions highlightOptions = new HighlightOptions();
        highlightOptions.addField("item_title");

        //3.设置高亮的前缀和后缀
        highlightOptions.setSimplePrefix("<em style='color:red'>");
        highlightOptions.setSimplePostfix("</em>");

        //4、关联高亮选项到高亮查询器对象
        query.setHighlightOptions(highlightOptions);

        //处理关键字
        if(searchMap.get("keywords")!=null){
            searchMap.put("keywords",searchMap.get("keywords").toString().replace(" ",""));
        }


        //5、设定查询条件 根据关键字查询
        //创建一个查询条件
        //5.1按照搜索关键字查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));

        //5.2按照分类筛选
        if(!"".equals(searchMap.get("category"))){
            Criteria filterCriteria = new Criteria("item_category").is(searchMap.get("category"));
            //分类筛选器
            FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
            query.addFilterQuery(filterQuery);
        }
        //5.3按照品牌筛选
        if(!"".equals(searchMap.get("brand"))){
            Criteria filterCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
            FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
            query.addFilterQuery(filterQuery);
        }
        //5.4 过滤规格
        if(searchMap.get("spec")!=null){
            Map<String,String> specMap =(Map) searchMap.get("spec");
            for(String key:specMap.keySet()){
                Criteria filterCriteria = new Criteria("item_spec_"+ Pinyin.toPinyin(key,"").toLowerCase()).is(specMap.get(key));
                FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
        }

        //5.5 按价格筛选
        if(!"".equals(searchMap.get("price"))){
            String[] prices = ((String) searchMap.get("price")).split("-");
            if(prices != null && prices.length>0){
                if(!"0".equals(prices[0])){
                    //设置查询过滤器
                    Criteria filterCriteria = new Criteria("item_price").greaterThanEqual(prices[0]);
                    FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
                    query.addFilterQuery(filterQuery);
                }
                if(!"*".equals(prices[1])){
                    //设置查询过滤器
                    Criteria filterCriteria = new Criteria("item_price").lessThanEqual(prices[1]);
                    FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
                    query.addFilterQuery(filterQuery);
                }
            }
        }

        //5.6分页查询
        Integer pageNo = (Integer) searchMap.get("pageNo");
        if(pageNo == null || pageNo<=0){
            pageNo = 1;
        }
        Integer pageSize = (Integer) searchMap.get("pageSize");
        if(pageSize == null || pageSize <=0 ){ //启动默认设置
            pageSize = 10;
        }

        query.setOffset((pageNo - 1) * pageSize);//从指定的地址开始查询
        query.setRows(pageSize);

        //排序
        String sortValue= (String) searchMap.get("sort");//ASC  DESC
        String sortField= (String) searchMap.get("sortField");//排序字段

        if(sortValue!=null && !sortValue.equals("")){
            if(sortValue.equals("ASC")){
                //设置排序字段的配置
                Sort sort = new Sort(Sort.Direction.ASC, "item_" + sortField);
                query.addSort(sort);
            }
            if(sortValue.equals("DESC")){
                Sort sort = new Sort(Sort.Direction.DESC, "item_" + sortField);
                query.addSort(sort);
            }
        }

        //关联查询条件对象到高亮查询器对象中
        query.addCriteria(criteria);

        //6.发出带有高亮查询数据的查询请求
        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);

        //7.获取高亮集合的入口
        List<HighlightEntry<TbItem>> highlighted = page.getHighlighted();

        //8.遍历高亮集合
        for (HighlightEntry<TbItem> highlightEntry:highlighted) {
            //获取基本数据对象
            TbItem tbItem = highlightEntry.getEntity();
            if(highlightEntry.getHighlights().size()>0 && highlightEntry.getHighlights().get(0).getSnipplets().size()>0) {
                //有高亮
                List<HighlightEntry.Highlight> highlightList = highlightEntry.getHighlights();
                //高亮结果集合
                List<String> snipplets = highlightList.get(0).getSnipplets();
                //获取第一个高亮字段对应的高亮结果，设置到商品标题
                tbItem.setTitle(snipplets.get(0));
            }
        }

        //把带高亮数据集合存放map
        map.put("rows",page.getContent());
        map.put("totalPages",page.getTotalPages());//返回总页数
        map.put("total",page.getTotalElements());//返回总记录数
        return map;
    }

    /**
     * 查询分类列表
     * @param searchMap
     * @return
     */
    private List searchCategoryList(Map searchMap){
        List<String> list = new ArrayList<String>();
        Query query = new SimpleQuery();
        //按照关键字查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);

        //设置分组
        GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");
        //将分组选项添加到查询对象上
        query.setGroupOptions(groupOptions);

        //查询索引库得到分组页面
        GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
        //根据列得到分组的结果集
        GroupResult<TbItem> groupResult = page.getGroupResult("item_category");

        //得到分组结果入口页面
        Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
        //得到分组入口的集合
        List<GroupEntry<TbItem>> content = groupEntries.getContent();

        for(GroupEntry<TbItem> entry:content){
            list.add(entry.getGroupValue());//将分组结果的名称封装到返回值中
        }
        return list;
    }

    /**
     * 从缓存中查询品牌和规格列表
     * @param category
     * @return
     */
    private Map searchBrandAndSpecList(String category){
        Map map=new HashMap();
        Long typeId = (Long) redisTemplate.boundHashOps("itemCat").get(category);//获取模板ID
        if(typeId!=null){
            //根据模板ID查询品牌列表
            List brandList = (List) redisTemplate.boundHashOps("brandList").get(typeId);
            map.put("brandList", brandList);//返回值添加品牌列表
            //根据模板ID查询规格列表
            List specList = (List) redisTemplate.boundHashOps("specList").get(typeId);
            map.put("specList", specList);
        }
        return map;
    }


}

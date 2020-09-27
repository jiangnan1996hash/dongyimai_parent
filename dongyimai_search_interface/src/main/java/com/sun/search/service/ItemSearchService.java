package com.sun.search.service;

import com.sun.pojo.TbItem;

import java.util.List;
import java.util.Map;

public interface ItemSearchService {


    /**
     *  搜索
     * @param searchMap
     * @return
     */
    public Map<String,Object> search(Map searchMap);

    /**
     * 导入数据
     * @param list
     */
    public void importList(List<TbItem> list);


    /**
     * 删除数据
     * @param goodsList
     */
    public void deleteByGoodsIds(List goodsList);



}

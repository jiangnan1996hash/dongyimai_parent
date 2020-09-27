package com.sun.page.service;

/**
 * 商品详情页的接口
 */
public interface ItemPageService {

    /**
     * 生成商品详细页
     * @param goodsId
     * @return
     */
    public boolean genItemHtml(Long goodsId);

    /**
     * 删除静态页面
     * @param goodsIds
     * @return
     */
    public boolean deleteItemHtml(Long[] goodsIds);


}

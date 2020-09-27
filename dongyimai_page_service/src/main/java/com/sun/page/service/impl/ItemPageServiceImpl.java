package com.sun.page.service.impl;


import com.sun.mapper.TbGoodsDescMapper;
import com.sun.mapper.TbGoodsMapper;
import com.sun.mapper.TbItemCatMapper;
import com.sun.mapper.TbItemMapper;
import com.sun.page.service.ItemPageService;
import com.sun.pojo.TbGoods;
import com.sun.pojo.TbGoodsDesc;
import com.sun.pojo.TbItem;
import com.sun.pojo.TbItemExample;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemPageServiceImpl implements ItemPageService {


    @Value("${pagedir}")
    private String pagedir;

    @Autowired
    private FreeMarkerConfig freeMarkerConfig;

    @Autowired
    private TbGoodsMapper goodsMapper;

    @Autowired
    private TbGoodsDescMapper goodsDescMapper;

    @Autowired
    private TbItemCatMapper itemCatMapper;

    @Autowired
    private TbItemMapper itemMapper;

    /**
     * 生成商品详情页
     * @param goodsId
     * @return
     */
    @Override
    public boolean genItemHtml(Long goodsId){
        try{
            //获得配置对象
            Configuration configuration = freeMarkerConfig.getConfiguration();
            //加载一个模板
            Template template = configuration.getTemplate("item.ftl");

            Map<String,Object> dataModel=new HashMap<String,Object>();
            //1.加载商品表数据
            TbGoods goods = goodsMapper.selectByPrimaryKey(goodsId);
            dataModel.put("goods", goods);
            //2.加载商品扩展表数据
            TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
            dataModel.put("goodsDesc", goodsDesc);

            //3.商品分类
            String itemCat1 = itemCatMapper.selectByPrimaryKey(goods.getCategory1Id()).getName();
            String itemCat2 = itemCatMapper.selectByPrimaryKey(goods.getCategory2Id()).getName();
            String itemCat3 = itemCatMapper.selectByPrimaryKey(goods.getCategory3Id()).getName();
            dataModel.put("itemCat1", itemCat1);
            dataModel.put("itemCat2", itemCat2);
            dataModel.put("itemCat3", itemCat3);

            TbItemExample itemExample = new TbItemExample();
            TbItemExample.Criteria criteria = itemExample.createCriteria();
            criteria.andStatusEqualTo("1");//上架的状态，
            criteria.andGoodsIdEqualTo(goodsId);//添加spu的id
            itemExample.setOrderByClause("is_default desc");//按照状态降序，保证第一个为默认
            List<TbItem> itemList = itemMapper.selectByExample(itemExample);

            dataModel.put("itemList",itemList);


            //E:\\item\\149187842867952.html
            Writer out=new FileWriter(pagedir+goodsId+".html");
            template.process(dataModel, out);
            out.close();
            return true;
        } catch(Exception e) {
            e.printStackTrace();

            return false;
        }

    }


    /**
     * 删除静态页面（广播）
     * @param goodsIds
     * @return
     */
    @Override
    public boolean deleteItemHtml(Long[] goodsIds) {
        try{
            for(Long goodId:goodsIds) {
                File file = new File(pagedir+goodId+".html");
                if(file.exists()){
                    file.delete();
                    System.out.println("成功删除"+goodId+"页面");
                }
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }


}

package com.sun.sellergoods.controller;

import com.alibaba.dubbo.config.annotation.Reference;

import com.alibaba.fastjson.JSON;
import com.sun.entity.Result;
import com.sun.entity.PageResult;
import com.sun.group.Goods;
import com.sun.pojo.TbGoods;
import com.sun.pojo.TbItem;
import com.sun.sellergoods.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.util.List;

/**
 * controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

	@Reference
	private GoodsService goodsService;

//	解耦合
//	@Reference
//	private ItemSearchService itemSearchService;

//	@Reference(timeout=40000)
//	private ItemPageService itemPageService;


	@Autowired
	private Destination queueTextDestination;//用于发送solr添加信息 点播

	@Autowired
	private Destination queueSolrDeleteDestination;//用于发送solr删除信息 点播

	@Autowired
	private Destination topicPageDestination;//用于发送生成静态页面消息 广播

	@Autowired
	private Destination topicPageDeleteDestination;//用于发送生成静态页面消息 广播

	@Autowired
	private JmsTemplate jmsTemplate;//模版类



	/**
	 * 生成静态页（测试）
	 * @param goodsId
	 */
//	@RequestMapping("/genHtml")
//	public void genHtml(Long goodsId){
//		System.out.println(goodsId);
//
//
//
//		itemPageService.genItemHtml(goodsId);
//	}

	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbGoods> findAll(){
		return goodsService.findAll();
	}
	
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult findPage(int page, int rows){
		return goodsService.findPage(page, rows);
	}
	
	/**
	 * 增加
	 * @param goods
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody Goods goods){
		try {
			goodsService.add(goods);
			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}
	
	/**
	 * 修改
	 * @param goods
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody Goods goods){
		try {
			goodsService.update(goods);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}	
	
	/**
	 * 获取实体
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOne")
	public Goods findOne(Long id){
		return goodsService.findOne(id);		
	}
	
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(final Long [] ids){

		try {
			//在数据库中逻辑删除
			goodsService.delete(ids);

			//删除商品的申请
			jmsTemplate.send(queueSolrDeleteDestination, new MessageCreator() {
				public Message createMessage(Session session) throws JMSException {
					return session.createObjectMessage(ids);
				}
			});

			//删除生成的静态页面
			jmsTemplate.send(topicPageDeleteDestination, new MessageCreator() {
				public Message createMessage(Session session) throws JMSException {
					return session.createObjectMessage(ids);
				}
			});


			//删除索引中的数据
//			itemSearchService.deleteByGoodsIds(Arrays.asList(ids));
			return new Result(true, "删除成功"); 
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}
	
		/**
	 * 查询+分页
	 * @param goods
	 * @param page
	 * @param rows
	 * @return
	 */
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbGoods goods, int page, int rows  ){
		return goodsService.findPage(goods, page, rows);		
	}


	/**
	 * 更新状态
	 * @param ids
	 * @param status
	 * @return
	 */
	@RequestMapping("/updateAuditStatus")
	public Result updateAuditStatus(Long[] ids, String status){
		try{
			goodsService.updateAuditStatus(ids,status);
			//按照spu ID查询sku列表（状态为1）
			if (status.equals("1")){//审核通过
				System.out.println("updateAuditStatus 审核通过");
				List<TbItem> itemList = goodsService.findItemListByGoodsIdandStatus(ids, status);
				//调用搜索接口实现数据批量导入
				if(itemList.size()>0){
					//将itemList转成JSON字符串
					final String JSONSTRING = JSON.toJSONString(itemList);
					jmsTemplate.send(queueTextDestination, new MessageCreator() {
						public Message createMessage(Session session) throws JMSException {
							return session.createTextMessage(JSONSTRING);
						}
					});

					//导入索引库
//					itemSearchService.importList(itemList);
				}else{
					System.out.println("没有明细数据sku");
				}
			}
			//商品审核后直接生成静态页面
			for ( final Long id : ids) {
				//发送消息生成商品详情页
				jmsTemplate.send(topicPageDestination, new MessageCreator() {
					public Message createMessage(Session session) throws JMSException {
						return session.createTextMessage(id+"");
					}
				});



//				itemPageService.genItemHtml(id);
			}

			return  new Result(true,"审核成功");
		}catch (Exception e){
			e.printStackTrace();
			return  new Result(true,"审核失败");
		}
	}

}

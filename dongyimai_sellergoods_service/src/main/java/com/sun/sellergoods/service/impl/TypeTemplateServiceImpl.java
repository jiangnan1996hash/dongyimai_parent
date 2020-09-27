package com.sun.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sun.entity.PageResult;
import com.sun.mapper.TbSpecificationMapper;
import com.sun.mapper.TbSpecificationOptionMapper;
import com.sun.mapper.TbTypeTemplateMapper;
import com.sun.pojo.TbSpecificationOption;
import com.sun.pojo.TbSpecificationOptionExample;
import com.sun.pojo.TbTypeTemplate;
import com.sun.pojo.TbTypeTemplateExample;
import com.sun.pojo.TbTypeTemplateExample.Criteria;
import com.sun.sellergoods.service.TypeTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.Map;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class TypeTemplateServiceImpl implements TypeTemplateService {

	@Autowired
	private TbTypeTemplateMapper typeTemplateMapper;

	@Autowired
	private TbSpecificationOptionMapper specificationOptionMapper;

	@Autowired
	private RedisTemplate redisTemplate;
	/**
	 * 查询全部
	 */
	@Override
	public List<TbTypeTemplate> findAll() {
		return typeTemplateMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbTypeTemplate> page=   (Page<TbTypeTemplate>) typeTemplateMapper.selectByExample(null);


		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbTypeTemplate typeTemplate) {
		typeTemplateMapper.insert(typeTemplate);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbTypeTemplate typeTemplate){
		typeTemplateMapper.updateByPrimaryKey(typeTemplate);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbTypeTemplate findOne(Long id){
		return typeTemplateMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			typeTemplateMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
	@Override
	public PageResult findPage(TbTypeTemplate typeTemplate, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbTypeTemplateExample example=new TbTypeTemplateExample();
		Criteria criteria = example.createCriteria();
		
		if(typeTemplate!=null){			
						if(typeTemplate.getName()!=null && typeTemplate.getName().length()>0){
				criteria.andNameLike("%"+typeTemplate.getName()+"%");
			}			if(typeTemplate.getSpecIds()!=null && typeTemplate.getSpecIds().length()>0){
				criteria.andSpecIdsLike("%"+typeTemplate.getSpecIds()+"%");
			}			if(typeTemplate.getBrandIds()!=null && typeTemplate.getBrandIds().length()>0){
				criteria.andBrandIdsLike("%"+typeTemplate.getBrandIds()+"%");
			}			if(typeTemplate.getCustomAttributeItems()!=null && typeTemplate.getCustomAttributeItems().length()>0){
				criteria.andCustomAttributeItemsLike("%"+typeTemplate.getCustomAttributeItems()+"%");
			}	
		}

		Page<TbTypeTemplate> page= (Page<TbTypeTemplate>)typeTemplateMapper.selectByExample(example);
		saveToRedis();//存入数据到缓存
	    System.out.println("TypeTemplateServiceImpl-findPage");
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 获取选项列表
	 * @return
	 */
	@Override
	public List<Map> selectOptionList() {
		return typeTemplateMapper.selectOptionList();
	}

	/**
	 * 根据模板Id，获取完整规格列表
	 * @param id
	 * @return
	 */
	@Override
	public List<Map> findSpecList(Long id) {
		//根据模板Id 获取对应的模板对象
		TbTypeTemplate tbTypeTemplate = typeTemplateMapper.selectByPrimaryKey(id);

		if(tbTypeTemplate.getSpecIds()!=null){
			//从模板对象获取规格属性 转换成集合
			List<Map> list = JSON.parseArray(tbTypeTemplate.getSpecIds(),Map.class);
			//遍历规格集合
			if(list != null && list.size()>0) {
				for (Map map : list) {
					//从map中取出Id
					Long specid = new Long((Integer) map.get("id"));
					//根据规格id获取规格选项
					TbSpecificationOptionExample example = new TbSpecificationOptionExample();
					TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
					criteria.andSpecIdEqualTo(specid);
					List<TbSpecificationOption> specificationOptionList = specificationOptionMapper.selectByExample(example);
					map.put("options", specificationOptionList);
				}
			}
			return list;
		}else {
			return null;
		}
	}


	/**
	 * 将数据存入缓存
	 */
	private void saveToRedis(){
		//获取模板数据
		List<TbTypeTemplate> typeTemplateList = findAll();

		//循环模板
		for(TbTypeTemplate typeTemplate : typeTemplateList){
			//存储品牌列表
			List<Map> brandList = JSON.parseArray(typeTemplate.getBrandIds(),Map.class);

			redisTemplate.boundHashOps("brandList").put(typeTemplate.getId(), brandList);

			//存储规格列表
			List<Map> specList = findSpecList(typeTemplate.getId());//根据模板Id查询出规格列表和规格选项

			redisTemplate.boundHashOps("specList").put(typeTemplate.getId(), specList);
		}
		System.out.println("TypeTemplateServiceImpl--saveToRedis--数据存入缓冲");
	}




}

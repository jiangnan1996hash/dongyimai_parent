package com.sun.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sun.entity.PageResult;
import com.sun.group.Specification;
import com.sun.mapper.TbSpecificationMapper;
import com.sun.mapper.TbSpecificationOptionMapper;
import com.sun.pojo.TbSpecification;
import com.sun.pojo.TbSpecificationExample;
import com.sun.pojo.TbSpecificationExample.Criteria;
import com.sun.pojo.TbSpecificationOption;
import com.sun.pojo.TbSpecificationOptionExample;
import com.sun.sellergoods.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class SpecificationServiceImpl implements SpecificationService {

	@Autowired
	private TbSpecificationMapper specificationMapper;
	@Autowired
	private TbSpecificationOptionMapper specificationOptionMapper;

	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbSpecification> findAll() {
		return specificationMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbSpecification> page=   (Page<TbSpecification>) specificationMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbSpecification specification) {
		specificationMapper.insert(specification);		
	}

	
	/**
	 * 修改 规格及规格列表
	 *
	 */
	@Override
	public void update(Specification specification){
		//根据ID修改规格
		specificationMapper.updateByPrimaryKey(specification.getSpecification());
		//根据规格ID，删除所有的规格选项
		TbSpecificationOptionExample example = new TbSpecificationOptionExample();
		TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
		criteria.andSpecIdEqualTo(specification.getSpecification().getId());

		specificationOptionMapper.deleteByExample(example);

		//删除规格选项后 再添加规格
		for(TbSpecificationOption tbSpecificationOption :specification.getSpecificationOptionList() ){
			//传入ID
			tbSpecificationOption.setSpecId(specification.getSpecification().getId());
			//插入数据
			specificationOptionMapper.insert(tbSpecificationOption);
		}
	}
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Specification findOne(Long id){
		//查询规格
		TbSpecification tbSpecification = specificationMapper.selectByPrimaryKey(id);
		//查询规格选项列表
		TbSpecificationOptionExample example=new TbSpecificationOptionExample();
		//创建查询对象
		TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
		//根据id查询
		criteria.andSpecIdEqualTo(id);//根据规格ID查询

		//查出的所有规格的选项列表
		List<TbSpecificationOption> optionList = specificationOptionMapper.selectByExample(example);
		//构建组合实体类返回结果
		Specification specification=new Specification();
		specification.setSpecification(tbSpecification);
		specification.setSpecificationOptionList(optionList);
		return specification;
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			//查询规格选项列表
			TbSpecificationOptionExample example=new TbSpecificationOptionExample();
			//创建查询对象
			TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
			criteria.andSpecIdEqualTo(id);
			//删除规格列表
			specificationOptionMapper.deleteByExample(example);

			//删除规格
			specificationMapper.deleteByPrimaryKey(id);


		}		
	}
	
	
		@Override
	public PageResult findPage(TbSpecification specification, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSpecificationExample example=new TbSpecificationExample();
		Criteria criteria = example.createCriteria();
		
		if(specification!=null){			
						if(specification.getSpecName()!=null && specification.getSpecName().length()>0){
				criteria.andSpecNameLike("%"+specification.getSpecName()+"%");
			}	
		}
		
		Page<TbSpecification> page= (Page<TbSpecification>)specificationMapper.selectByExample(example);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加一个规格和多个组件
	 * @param specification
	 */
	@Override
	public void add(Specification specification) {
		specificationMapper.insert(specification.getSpecification());//插入规格

		//循环插入规格中的组件
		for(TbSpecificationOption specificationOption:specification.getSpecificationOptionList()){
			//设置规格ID		specificationOptionMapper.insert(specificationOption);
			specificationOption.setSpecId(specification.getSpecification().getId());
			specificationOptionMapper.insert(specificationOption);
		}
	}


	/**
	 *	品牌下拉列表数据
	 */
	@Override
	public List<Map> selectOptionList() {
		return specificationMapper.selectOptionList();
	}



}

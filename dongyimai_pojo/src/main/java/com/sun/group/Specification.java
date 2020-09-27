package com.sun.group;

import com.sun.pojo.TbSpecification;
import com.sun.pojo.TbSpecificationOption;

import java.io.Serializable;
import java.util.List;
/**
 * 规格组合实体类
 * @author Administrator
 *
 */
public class Specification implements Serializable {
    //规格
    private TbSpecification specification;
    //一个规格有多个组件
    private List<TbSpecificationOption> specificationOptionList;

    public TbSpecification getSpecification() {
        return specification;
    }
    public void setSpecification(TbSpecification specification) {
        this.specification = specification;
    }
    public List<TbSpecificationOption> getSpecificationOptionList() {
        return specificationOptionList;
    }
    public void setSpecificationOptionList(List<TbSpecificationOption> specificationOptionList) {
        this.specificationOptionList = specificationOptionList;
    }
}

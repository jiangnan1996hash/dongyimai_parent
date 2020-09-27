package com.sun.portal.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.sun.content.service.ContentService;
import com.sun.pojo.TbContent;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/content")
public class ContentController {

    @Reference
    private ContentService contentService;

    /**
     * 根据广告分类ID查询广告列表
     * @param categoryId
     * @return
     */
    @RequestMapping("findByCategoryId")
    public List<TbContent> findByCategoryId(Long categoryId){

        return contentService.findByCategoryId(categoryId);
    }


}

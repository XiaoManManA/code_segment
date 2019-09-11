package com.hanyuan.ebay.modules.sys.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hanyuan.ebay.common.utils.*;
import com.hanyuan.ebay.modules.job.service.EbayItemsTaskService;
import com.hanyuan.ebay.modules.sys.dao.EbayItemDao;
import com.hanyuan.ebay.modules.sys.entity.EbayItemEntity;
import com.hanyuan.ebay.modules.sys.entity.SysConfigEntity;
import com.hanyuan.ebay.modules.sys.service.EbayItemService;
import com.hanyuan.ebay.modules.sys.service.SysConfigService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service("ebayItemService")
public class EbayItemServiceImpl extends ServiceImpl<EbayItemDao, EbayItemEntity> implements EbayItemService {

    @Autowired
    private SysConfigService sysConfigService;

    private static Logger logger = LoggerFactory.getLogger(EbayItemServiceImpl.class);

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        EntityWrapper e = new EntityWrapper<EbayItemEntity>();

        // 删除标识为未删除
        e.eq("delete_flag", 1);

        // 查询请求来源
        Object source = params.get("source");

        /////////////////////////////////////// 产品库查询 begin ///////////////////////////////////////

        if(source != null && "item".equals(source)){

            //关键词
            Object key = params.get("key");
            if(key != null && StringUtils.isNotEmpty(key.toString())){
                e.like("name", key.toString());
            }

            //处理时间
            Object handlingTime = params.get("handlingTime");
            if(handlingTime != null && StringUtils.isNotEmpty(handlingTime.toString())){
                e.eq("handling_time", handlingTime.toString());
            }

            //是否多属性
            Object isMultiVariationListing = params.get("isMultiVariationListing");
            if(isMultiVariationListing != null && StringUtils.isNotEmpty(isMultiVariationListing.toString())){
                e.le("is_multi_variation_listing", isMultiVariationListing.toString());
            }

            //收录天数
            Object enteredDayType = params.get("enteredDayType");
            if(enteredDayType != null && StringUtils.isNotEmpty(enteredDayType.toString())){
                if("1".equals(enteredDayType)){
                    e.eq("entered_day", 1);
                } else if("2".equals(enteredDayType)){
                    e.gt("entered_day", 1);
                }
            }

            //货运方式
            Object nameAndDeliveryTime = params.get("nameAndDeliveryTime");
            if(nameAndDeliveryTime != null && StringUtils.isNotEmpty(nameAndDeliveryTime.toString())){
                e.like("name_and_delivery_time", nameAndDeliveryTime.toString());
            }

            // 商品类型[1-产品库，5-屏蔽库]
            e.eq("type", 1);

            // 通用查询参数
            commonAbstractWrapper(params, e);

            // 商品分类
            categoryAbstractWrapper(params, e);
        }

        /////////////////////////////////////// 产品库查询 end ///////////////////////////////////////

        /////////////////////////////////////// 推荐产品查询 begin ///////////////////////////////////////

        if(source != null && "recommend".equals(source)){

            //获取用户推荐产品设置规则
            String str = sysConfigService.getValue(SysConfigEntity.RECOMMEND_SETTING_KEY + params.get("userId"));

            //根据传递过来的新品 爆款类型去带入条件
            if (StringUtils.isNotEmpty(str)) {

                // 格式化规则查询条件
                Map<String, Object> setting = JSON.parseObject(str);

                switch (params.get("recommendType").toString()){
                    case "new":
                        params.putAll((Map<String, Object>) setting.get("newMap"));
                        break;
                    case "hot":
                        params.putAll((Map<String, Object>) setting.get("hotMap"));
                        break;
                }

            }

            // 通用查询参数
            commonAbstractWrapper(params, e);

            // 放入用户收藏分类数据
            params.put("categoryList", params.get("categoryList"));

            // 商品分类
            categoryAbstractWrapper(params, e);

        }

        /////////////////////////////////////// 推荐产品查询 end ///////////////////////////////////////

        /////////////////////////////////////// 屏蔽库查询 begin ///////////////////////////////////////

        if(source != null && "shield".equals(source)){

            // 商品类型[1-产品库，5-屏蔽库]
            e.eq("type", 5);

            // 屏蔽库类型[1-毫无用处，2-看它成长]
            Object shieldType = params.get("shieldType");
            if(shieldType != null && StringUtils.isNotEmpty(shieldType.toString())){
                e.eq("shield_type", shieldType);
            }

            commonAbstractWrapper(params, e);

        }

        /////////////////////////////////////// 屏蔽库查询 end ///////////////////////////////////////

        /////////////////////////////////////// 标题撰写 begin ///////////////////////////////////////

        if(source != null && "titleWriting".equals(source)){

            List<String> excludeIds = Arrays.asList(params.get("excludeIds").toString().split(","));
            if(excludeIds != null && excludeIds.size() > 0){
                // 屏蔽ID，对应页面移除操作
                e.notIn("id", excludeIds);
            }
        }

        /////////////////////////////////////// 标题撰写 end ///////////////////////////////////////

        Page<EbayItemEntity> page = this.selectPage(
                new Query<EbayItemEntity>(params).getPage(),
                e
        );
        return new PageUtils(page);
    }

    /**
     * 一些奇奇怪怪的查询条件，封装一下子
     */
    private void commonAbstractWrapper(Map<String, Object> params, EntityWrapper<EbayItemEntity> e){

        //商品售价起始
        Object currentPriceBegin = params.get("currentPriceBegin");
        if(currentPriceBegin != null && StringUtils.isNotEmpty(currentPriceBegin.toString())){
            e.ge("current_price", currentPriceBegin.toString());
        }

        //商品售价截止
        Object currentPriceEnd = params.get("currentPriceEnd");
        if(currentPriceEnd != null && StringUtils.isNotEmpty(currentPriceEnd.toString())){
            e.le("current_price", currentPriceEnd.toString());
        }

        //上架时间截止
        Object startTimeEnd = params.get("startTimeEnd");
        if(startTimeEnd != null && StringUtils.isNotEmpty(startTimeEnd.toString())){
            e.le("start_time", startTimeEnd.toString());
        }

        //上架时间起始
        Object startTimeBegin = params.get("startTimeBegin");
        if(startTimeBegin != null && StringUtils.isNotEmpty(startTimeBegin.toString())){
            e.ge("start_time", startTimeBegin.toString());
        }

        //物品所在地
        Object location = params.get("location");
        if(location != null && StringUtils.isNotEmpty(location.toString())){
            e.eq("location", location.toString());
        }

        //七日销量起始
        Object sevenSoldQuantityBegin = params.get("sevenSoldQuantityBegin");
        if(sevenSoldQuantityBegin != null && StringUtils.isNotEmpty(sevenSoldQuantityBegin.toString())){
            e.ge("seven_sold_quantity", sevenSoldQuantityBegin.toString());
        }

        //七日销量截止
        Object sevenSoldQuantityEnd = params.get("sevenSoldQuantityEnd");
        if(sevenSoldQuantityEnd != null && StringUtils.isNotEmpty(sevenSoldQuantityEnd.toString())){
            e.le("seven_sold_quantity", sevenSoldQuantityEnd.toString());
        }

        //站点ID
        Object siteId = params.get("siteId");
        if(siteId != null && StringUtils.isNotEmpty(siteId.toString())){
            e.eq("site_id", siteId.toString());
        }

        // 排序
        EbayTaskUtils.orderByAbstractWrapper(params, e);

    }

    /**
     * 分类查询
     */
    private void categoryAbstractWrapper(Map<String, Object> params, EntityWrapper<EbayItemEntity> e) {

        // 取出用户收藏分类信息
        List<Map<String, Object>> categoryList = (List<Map<String, Object>>) params.get("categoryList");
        if(categoryList == null || categoryList.size() == 0){
            e.eq("user_category_id", -1);
            return;
        }

        // 取出指定分类数据
        Object categoryId = params.get("categoryId");
        if(categoryId != null && StringUtils.isNotEmpty(categoryId.toString())){
            e.eq("user_category_id", categoryId);
            return;
        }

        // 取出指定站点数据
        Object siteId = params.get("siteId");
        if(siteId != null && StringUtils.isNotEmpty(siteId.toString())){
            List<Object> categoryIdList = getCategoryIdList(categoryList, siteId);
            if(categoryIdList.size() > 0){
                e.in("user_category_id", categoryIdList);
                return;
            }
        }

        // 以上条件都不符合，直接查询用户收藏分类全部商品数据
        List<Object> categoryIds = categoryList.stream().map(this::getCategoryId).collect(Collectors.toList());
        e.in("user_category_id", categoryIds);
    }

    private Object getCategoryId(Map<String, Object> map){
        return map.get("categoryId");
    }

    private List<Object> getCategoryIdList(List<Map<String, Object>> categoryList, Object siteId) {
        List<Object> categoryIdList = Lists.newArrayList();
        int categoryListSize = categoryList.size();
        for (int i = 0; i < categoryListSize; i++) {
            Object mapSiteId = categoryList.get(i).get("siteId");
            if (siteId.equals(mapSiteId)) {
                categoryIdList.add(categoryList.get(i).get("categoryId"));
            }
        }
        return categoryIdList;
    }


    /**
     * 保存商品
     * @param itemEntityList
     */
    @Async
    @Override
    public void saveItem(List<EbayItemEntity> itemEntityList) {

        long startTime = System.currentTimeMillis();   //获取开始时间

        if (itemEntityList.size() > 0) {
            try {
                baseMapper.inertItemBatch(itemEntityList);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        long endTime = System.currentTimeMillis(); //获取结束时间

        logger.error("新增"+ itemEntityList.size() +"条商品数据到数据库 : " + (Double.valueOf(endTime - startTime) / 1000) + "秒");

    }

    /**
     * 更新销量
     * @param itemEntityList
     */
    @Async
    @Override
    public void updateItem(List<EbayItemEntity> itemEntityList) {

        long startTime = System.currentTimeMillis();   //获取开始时间

        try {
            // logger.error(JSON.toJSONString(itemEntityList));
            this.updateBatchById(itemEntityList);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        long endTime = System.currentTimeMillis(); //获取结束时间

        logger.error("更新销量到数据库 : " + (Double.valueOf(endTime - startTime) / 1000) + "秒");
    }

    @Override
    public List<Long> selectIdListPage(Map<String, Object> params) {
        Page<Long> page = new Query<Long>(params).getPage();
        return baseMapper.selectIdListPage(page);
    }

    @Override
    public void ebayItemsClear() {
        baseMapper.ebayItemsClear();
    }

    @Override
    public void HomeStatistics() {

    }

    @Override
    public void itemOutStore(List<Long> longs) {
        baseMapper.itemOutStore(longs);
    }

    @Override
    public void updateEbayItem(Integer type, Integer shieldType, Long id) {
        baseMapper.updateEbayItem(type, shieldType, id);
    }

    @Async
    @Override
    public void truncateEbayItemBestMatchRank() {
        baseMapper.truncateEbayItemBestMatchRank();
    }

    @Async
    @Override
    public void inertItemBestMatchRankBatch(List<Map<String, Object>> bestMatchList) {
        baseMapper.inertItemBestMatchRankBatch(bestMatchList);
    }

    @Override
    public void updateBestMatchRank() {
        baseMapper.updateBestMatchRank();
    }

    @Override
    public List<Map<String, Object>> selectTotalMap() {
        return baseMapper.selectTotalMap();
    }

    @Override
    public List<String> selectExistedItemIds(List<String> queryExistedItemIds, String siteId) {
        return baseMapper.queryExistedItemIds(queryExistedItemIds, siteId);
    }

    @Override
    public void truncateTable() {
        baseMapper.ebayItemsTruncate();
    }

}

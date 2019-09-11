package com.hanyuan.ebay.modules.sys.service;

import com.baomidou.mybatisplus.service.IService;
import com.hanyuan.ebay.common.utils.PageUtils;
import com.hanyuan.ebay.modules.sys.entity.EbayItemEntity;

import java.util.List;
import java.util.Map;

/**
 * eBay商品
 *
 * @author sing
 * @email h_j_xiao@foxmail.com
 * @date 2019-01-08 19:20:55
 */
public interface EbayItemService extends IService<EbayItemEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveItem(List<EbayItemEntity> itemEntityList);

    void updateItem(List<EbayItemEntity> itemEntityList);

    List<Long> selectIdListPage(Map<String, Object> params);

    /////////////////////////////////////////////  业务方法 /////////////////////////////////////////////

    /**
     * 清除产品库中删除标识为真的eBay商品
     */
    void ebayItemsClear();

    /**
     * 首页数据统计，统计最新的各项数据值，写入数据库
     */
    void HomeStatistics();

    /**
     * 商品出库
     * @param longs
     */
    void itemOutStore(List<Long> longs);

    /**
     * 更新商品属性
     * @param type
     * @param shieldType
     */
    void updateEbayItem(Integer type, Integer shieldType, Long id);

    /**
     * 清空商品最佳匹配排序
     */
    void truncateEbayItemBestMatchRank();

    /**
     * 插入商品最佳匹配排序
     * @param bestMatchList
     * @return
     */
    void inertItemBestMatchRankBatch(List<Map<String, Object>> bestMatchList);

    /**
     * 更新商品最佳匹配排序
     */
    void updateBestMatchRank();

    /////////////////////////////////////////////  业务方法 /////////////////////////////////////////////

    List<Map<String, Object>> selectTotalMap();

    List<String> selectExistedItemIds(List<String> queryExistedItemIds, String siteId);

    void truncateTable();

}


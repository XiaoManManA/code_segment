package com.hanyuan.ebay.modules.sys.service;

import com.hanyuan.ebay.modules.sys.entity.EbayCategoryEntity;
import com.hanyuan.ebay.sdk.trading.soap.eBLBaseComponents.TransactionType;

import java.util.List;
import java.util.Map;

public interface EbayApiService {

    /**
     * 检索指定eBay站点的最新类别层次结构
     * @param appId
     * @param token
     * @param siteId
     * @return
     */
    List<EbayCategoryEntity> getCategories(String appId, String token, String siteId);

    /**
     * 返回指定类别中的eBay商品
     * @param appId
     * @param siteId
     * @param pageNumber 当前页数
     * @return
     */
    List<Map<String, Object>> findItemsByCategory(String appId, String siteId, String categoryId, int pageNumber);

    /**
     * 返回指定关键字查询的eBay商品
     * @param appId
     * @param siteId
     * @param keywords
     * @param pageNumber
     * @return
     */
    List<Map<String, Object>> findItemsByKeywords(String appId, String siteId, String keywords, int pageNumber);

    /**
     * 返回eBay店铺中的所有商品
     * @param appId
     * @param siteId
     * @param storeName
     * @param pageNumber
     * @return
     */
    List<Map<String, Object>> findItemsIneBayStores(String appId, String siteId, String storeName, int pageNumber);

    /**
     * 检索指定商品的的公开数据
     * @param appId
     * @param siteId
     * @param itemIds
     */
    List<Map<String, Object>> getMultipleItems(String appId, String siteId, String itemIds);

    /**
     * 检索指定商品的销售历史数据
     * @param token
     * @return
     */
    Map<String, Object> getItemTransactions(String token, String itemId);

    /**
     * 检索经过身份验证的用户发布的项目列表，包括相关项目数据。
     * @param token
     * @return
     */
    List<Map<String, Object>> getSellerList(String token, int num);

    /**
     * 检索经过身份验证的用户作为参与者的订单，作为买方或卖方。
     * @param token
     * @return
     */
    List<Map<String, Object>> getOrders(String token, int num);

    /**
     * 获取商品的运费。
     * @param appId
     * @param itemId
     * @param siteId
     * @return
     */
    Map<String, String> getShippingCosts(String appId, String itemId, String siteId);

    TransactionType[] getItemTransactionType(String token, String itemId);

}

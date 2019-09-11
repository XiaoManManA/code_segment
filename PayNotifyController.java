package cn.szu.xiaoshu.modules.app.controller;

import cn.szu.xiaoshu.modules.app.service.CanteenOrderService;
import cn.szu.xiaoshu.wxpay.sdk.WXPayConstants;
import cn.szu.xiaoshu.wxpay.sdk.WXPayUtil;
import com.alibaba.fastjson.JSON;
import com.alipay.api.internal.util.AlipaySignature;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.Map;

@RestController
@RequestMapping("/trade")
public class PayNotifyController {

    private Logger logger = LoggerFactory.getLogger(getClass());
    @Value("${xiaoshu.pay.wxKey}")
    private String wxKey;
    @Value("${xiaoshu.pay.aliPublicKey}")
    private String aliPublicKey;
    @Autowired
    private CanteenOrderService orderService;

    @RequestMapping("/wxPayNotify")
    public String wxPayNotify(HttpServletRequest request) {
        try {
            StringBuilder xmlStr = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line = null;
            while ((line = reader.readLine()) != null) {
                xmlStr.append(line);
            }
            Map<String, String> wxMap = WXPayUtil.xmlToMap(xmlStr.toString());
            boolean isSignatureValid = WXPayUtil.isSignatureValid(wxMap, wxKey, WXPayConstants.SignType.HMACSHA256);
            logger.info("-----------------------------------------wxPayNotify---------------------------------------------------");
            logger.info("xmlStr:" + xmlStr);
            logger.info("wxMap:" + JSON.toJSONString(wxMap));
            logger.info("isSignatureValid:" + isSignatureValid);
            logger.info("-----------------------------------------wxPayNotify---------------------------------------------------");
            if (isSignatureValid) {
                Map<String, String> params = Maps.newHashMap();
                params.put("totalAmount", BigDecimal.valueOf(Double.valueOf(wxMap.get("total_fee"))).divide(BigDecimal.valueOf(100)).doubleValue()+"");
                params.put("orderCode", wxMap.get("out_trade_no"));
                params.put("transactionId", wxMap.get("transaction_id"));
                params.put("passBackParams", wxMap.get("attach"));
                orderService.payCallback(params);
                return returnXML(wxMap.get("return_code"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnXML("FAIL");
    }

    private String returnXML(String return_code) {
        return "<xml><return_code><![CDATA["
                + return_code
                + "]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";
    }

    @RequestMapping("/aliPayNotify")
    public String aliPayNotify(@RequestParam Map<String, String> response) {
        try {
            logger.info("--------------------------------------------aliPayNotify------------------------------------------------");
            logger.info("aliMap:" + JSON.toJSONString(response));
            boolean isSignatureValid = AlipaySignature.rsaCheckV1(response, aliPublicKey, "UTF-8", "RSA2");
            logger.info("isSignatureValid:" + isSignatureValid);
            logger.info("--------------------------------------------aliPayNotify------------------------------------------------");
            if (isSignatureValid) {
                Map<String, String> params = Maps.newHashMap();
                params.put("totalAmount", response.get("total_amount"));
                params.put("orderCode", response.get("out_trade_no"));
                params.put("transactionId", response.get("trade_no"));
                params.put("passBackParams", URLDecoder.decode(response.get("passback_params"), "UTF-8"));
                orderService.payCallback(params);
                return "success";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "false";
    }


}

package cn.jiuzhuang.modules.app.controller;

import cn.jiuzhuang.common.utils.R;
import cn.jiuzhuang.common.validator.ValidatorUtils;
import cn.jiuzhuang.modules.app.annotation.Login;
import cn.jiuzhuang.modules.app.annotation.LoginUser;
import cn.jiuzhuang.modules.app.entity.UserEntity;
import cn.jiuzhuang.modules.app.form.ConvertIntegralItemForm;
import cn.jiuzhuang.modules.sys.service.IntegralItemService;
import cn.jiuzhuang.modules.sys.service.UserIntegralConvertService;
import cn.jiuzhuang.modules.sys.service.UserIntegralService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/app/integral")
@Api(tags = "积分商城")
public class AppIntegralController {

    @Autowired
    private IntegralItemService integralItemService;
    @Autowired
    private UserIntegralService userIntegralService;
    @Autowired
    private UserIntegralConvertService integralConvertService;

    @PostMapping("items")
    @ApiOperation("积分商品")
    public R items() {

        // 列出所有积分商品，不分页
        return R.ok().put("data", integralItemService.getIntegralItems());
    }

    @Login
    @PostMapping("record")
    @ApiOperation("积分明细")
    public R integralRecord(@LoginUser UserEntity user) {

        // 列出会员的积分记录
        return R.ok().put("data", userIntegralService.getUserIntegralList(user.getUserId()));
    }

    @Login
    @PostMapping("convert")
    @ApiOperation("兑换商品")
    public R items(@LoginUser UserEntity user, @RequestBody ConvertIntegralItemForm form) {

        ValidatorUtils.validateEntity(form);
        form.setUserId(user.getUserId());
        form.setUserName(user.getNickName());

        // 提交一个兑换商品申请，在这里要扣减库存
        integralConvertService.convertItem(form);
        return R.ok();
    }

    @Login
    @PostMapping("convert/record")
    @ApiOperation("兑换记录")
    public R convertRecord(@LoginUser UserEntity user) {

        // 列出会员的申请兑换记录
        return R.ok().put("data", integralConvertService.getUserIntegralConvertList(user.getUserId()));
    }

}

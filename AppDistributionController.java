package cn.jiuzhuang.modules.app.controller;

import cn.jiuzhuang.common.utils.R;
import cn.jiuzhuang.common.validator.ValidatorUtils;
import cn.jiuzhuang.modules.app.annotation.Login;
import cn.jiuzhuang.modules.app.annotation.LoginUser;
import cn.jiuzhuang.modules.app.entity.UserEntity;
import cn.jiuzhuang.modules.app.form.QueryTeamForm;
import cn.jiuzhuang.modules.app.service.DistributionService;
import cn.jiuzhuang.modules.sys.entity.UserDistributorApplicationEntity;
import cn.jiuzhuang.modules.sys.entity.UserWithdrawalEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * APP分销中心
 *
 * @author Sing h_j_xiao@foxmail.com
 */
@RestController
@RequestMapping("/app/distribution")
@Api(tags = "分销中心")
public class AppDistributionController {

    @Autowired
    private DistributionService distributionService;

    @Login
    @PostMapping("account")
    @ApiOperation("会员账户")
    public R account(@LoginUser UserEntity user) {

        return R.ok().put("data", distributionService.getUserAccount(user.getUserId()));
    }

    @Login
    @PostMapping("apply")
    @ApiOperation("申请成为分销商")
    public R apply(@LoginUser UserEntity user, @RequestBody UserDistributorApplicationEntity form) {

        // 设置用户ID
        form.setUserId(user.getUserId());
        ValidatorUtils.validateEntity(form);

        distributionService.applyForDistributor(form);

        return R.ok();
    }

    @Login
    @PostMapping("team")
    @ApiOperation("我的团队")
    public R team(@LoginUser UserEntity user, @RequestBody QueryTeamForm form) {

        ValidatorUtils.validateEntity(form);

        return R.ok().put("data", distributionService.getUserRelation(form.getLevel(), user.getUserId()));
    }

    @Login
    @PostMapping("commission")
    @ApiOperation("返点记录")
    public R commission(@LoginUser UserEntity user) {

        return R.ok().put("data", distributionService.getUserCommission(user.getUserId()));
    }

    @Login
    @PostMapping("withdrawal")
    @ApiOperation("")
    public R withdrawal(@LoginUser UserEntity user, @RequestBody UserWithdrawalEntity form) {

        ValidatorUtils.validateEntity(form);

        form.setUserId(user.getUserId());
        distributionService.applyForWithdrawal(form);

        return R.ok();
    }

    @Login
    @PostMapping("withdrawalRecord")
    @ApiOperation("提现记录")
    public R withdrawalRecord(@LoginUser UserEntity user) {

        return R.ok().put("data", distributionService.getUserWithdrawal(user.getUserId()));
    }

}

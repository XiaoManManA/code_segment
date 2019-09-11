package cn.szu.xiaoshu.modules.app.controller;

import cn.szu.xiaoshu.common.exception.XSException;
import cn.szu.xiaoshu.common.utils.PayUtils;
import cn.szu.xiaoshu.common.utils.R;
import cn.szu.xiaoshu.common.validator.ValidatorUtils;
import cn.szu.xiaoshu.modules.app.annotation.Login;
import cn.szu.xiaoshu.modules.app.annotation.LoginUser;
import cn.szu.xiaoshu.modules.app.entity.TutorJobEntity;
import cn.szu.xiaoshu.modules.app.entity.UserEntity;
import cn.szu.xiaoshu.modules.app.form.ParentSubmitForm;
import cn.szu.xiaoshu.modules.app.form.PassBackParamsForm;
import cn.szu.xiaoshu.modules.app.form.PaySubmitForm;
import cn.szu.xiaoshu.modules.app.service.*;
import cn.szu.xiaoshu.modules.app.trade.TradeService;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Map;

@RestController
@RequestMapping("/app/tutor/parent")
@Api(tags = "APP家教家长接口")
public class AppTutorParentController {

    @Autowired
    TutorSubjectService subjectService;
    @Autowired
    TradeService tradeService;
    @Autowired
    TutorJobService jobService;
    @Autowired
    TutorResumeService resumeService;
    @Autowired
    TutorDeliveryService deliveryService;
    @Autowired
    UserService userService;

    /**
     * 个人中心
     */
    @Login
    @PostMapping("index")
    @ApiOperation("家长个人中心")
    public R index(@ApiIgnore @LoginUser UserEntity user) {
        //返回APP用户的头像与名称
        return R.ok().put("content", user.resultMap());
    }

    /**
     * 已发布的老师信息
     */
    @Login
    @PostMapping("jobs")
    @ApiOperation("已发布的老师信息")
    public R jobs(@ApiIgnore @RequestAttribute("userId") Integer userId) {
        //返回用户自己发布的 tb_tutor_job 记录
        return R.ok().put("content", jobService.selectMyTutorJobs(Long.valueOf(userId)));
    }

    /**
     * 收到的老师信息
     */
    @Login
    @PostMapping("resumes")
    @ApiOperation("收到的老师信息")
    public R resumes(@ApiIgnore @RequestAttribute("userId") Integer userId) {
        //查询 tb_tutor_delivery 投递关系表，返回 tutor_job_user_id（本用户） 所属的tb_tutor_resume记录
        return R.ok().put("content", deliveryService.selectTutorResumes(Long.valueOf(userId)));
    }

    /**
     * 家教老师列表
     */
    @PostMapping("teachers")
    @ApiOperation("家教老师列表")
    public R teachers() {
        //返回家教老师列表
        return R.ok().put("content", resumeService.selectTutorResumes());
    }

    /**
     * 家长信息填写
     */
    @Login
    @PostMapping("submit")
    @ApiOperation("家长信息填写")
    public R submit(@ApiIgnore @LoginUser UserEntity user, @RequestBody ParentSubmitForm form) {
        if(user.getIsJobVip() == 1){
            throw new XSException("请先缴纳招聘费用，再发布招聘信息！", 403);
        }
        //表单校验
        ValidatorUtils.validateEntity(form);
        form.setUser(user);
        //生成家长信息审核记录
        TutorJobEntity job = new TutorJobEntity(form);
        jobService.insert(job);
        return R.ok();
    }

    /**
     * 家长信息填写-科目
     */
    @PostMapping("subjects")
    @ApiOperation("家长信息填写-选择科目")
    public R subject() {
        return R.ok().put("content", subjectService.selectSubjects());
    }

    /**
     * 提示弹窗
     */
    @Login
    @PostMapping("tipPopup")
    @ApiOperation("提示弹窗")
    public R tipPopup(@ApiIgnore @LoginUser UserEntity user) {
        //未缴纳过平台招聘费用但是存在招聘费订单商户号，则去第三方平台查询该订单号支付结果
        if(user.getIsJobVip() == 1 && StringUtils.isNotEmpty(user.getVipTradeNo()) && tradeService.paymentSuccessful(user)){
            user.setIsJobVip(2);
            userService.updateById(user);
        }
        return R.ok().put("content", user.getIsJobVip());
    }

    /**
     * 支付100元
     */
    @Login
    @PostMapping("payOneHundredYuan")
    @ApiOperation("支付一百元")
    public R payOneHundredYuan(@ApiIgnore @LoginUser UserEntity user, @RequestBody PaySubmitForm form) {
        if(user.getIsJobVip() == 2){
            return R.ok("该用户已缴纳过招聘费用！");
        }
        ValidatorUtils.validateEntity(form);
        String outTradeNo = PayUtils.generateOutTradeNo(user.getUserId());
        Map<String, String> params = Maps.newHashMap();
        params.put("payType", form.getPay_type()+"");
        params.put("body", "深大小树APP招聘费");
        params.put("totalAmount", "0.01");
        params.put("outTradeNo", outTradeNo);
        params.put("passBackParams", JSON.toJSONString(new PassBackParamsForm("vipFee", user.getUserId())));

        //实时更新用户招聘费订单数据
        user.setVipPayType(form.getPay_type());
        user.setVipTradeNo(outTradeNo);
        userService.updateById(user);

        //调用APP支付，返回一百元支付资料
        return R.ok().put("content", tradeService.tradeAppPay(params));
    }

}

package cn.szu.xiaoshu.modules.app.controller;

import cn.szu.xiaoshu.common.exception.XSException;
import cn.szu.xiaoshu.common.utils.R;
import cn.szu.xiaoshu.common.validator.ValidatorUtils;
import cn.szu.xiaoshu.modules.app.annotation.Login;
import cn.szu.xiaoshu.modules.app.annotation.LoginUser;
import cn.szu.xiaoshu.modules.app.entity.TutorResumeEntity;
import cn.szu.xiaoshu.modules.app.entity.UserEntity;
import cn.szu.xiaoshu.modules.app.form.*;
import cn.szu.xiaoshu.modules.app.service.TutorDeliveryService;
import cn.szu.xiaoshu.modules.app.service.TutorJobService;
import cn.szu.xiaoshu.modules.app.service.TutorResumeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Map;

@RestController
@RequestMapping("/app/tutor/teacher")
@Api(tags = "APP家教老师接口")
public class AppTutorTeacherController {

    @Autowired
    TutorDeliveryService deliveryService;
    @Autowired
    TutorResumeService resumeService;
    @Autowired
    TutorJobService jobService;

    /**
     * 个人中心
     */
    @Login
    @PostMapping("index")
    @ApiOperation("老师个人中心")
    public R index(@ApiIgnore @LoginUser UserEntity user){
        //返回APP用户的头像与名称
        return R.ok().put("content", user.resultMap());
    }

    /**
     * 已投递的招聘信息
     */
    @Login
    @PostMapping("deliveryRecord")
    @ApiOperation("已投递的招聘信息")
    public R deliveryRecord(@ApiIgnore @RequestAttribute("userId") Integer userId){
        return R.ok().put("content", deliveryService.selectTutorJobs(Long.valueOf(userId)));
    }

    /**
     * 老师个人信息
     */
    @Login
    @PostMapping("personalInfo")
    @ApiOperation("老师个人信息")
    public R personalInfo(@ApiIgnore @RequestAttribute("userId") Integer userId){
        //返回老师简历资料信息，暂时返回全部 tb_tutor_resume
        Map<String, Object> map = resumeService.selectMapById(Long.valueOf(userId));
        if(map.isEmpty()){
            throw new XSException("请先填写简历信息！", 401);
        }
        return R.ok().put("content", map);
    }

    /**
     * 老师详情页
     */
    @Login
    @PostMapping("detailsPage")
    @ApiOperation("老师详情页")
    public R detailsPage(@ApiIgnore @LoginUser UserEntity user, @RequestBody ResumeDetailsQueryForm form){
        //判断查阅详情页的用户是否缴纳过平台招聘费用
        if(user.getIsJobVip() == 1){
            return R.error("请先缴纳招聘费，才能查阅老师详情页！");
        }
        ValidatorUtils.validateEntity(form);
        //返回老师详情页 这里没有身份证 学生证等敏感信息
        return R.ok().put("content", resumeService.selectDetails(form.getResume_id()));
    }

    /**
     * 招聘信息
     */
    @Login
    @PostMapping("jobs")
    @ApiOperation("招聘信息")
    public R jobs(@ApiIgnore @RequestAttribute("userId") Integer userId){
        //返回家教招聘信息列表，需要给用户返回 is_delivery 字段(是否投递过)
        return R.ok().put("content", jobService.selectAllTutorJobs(Long.valueOf(userId)));
    }

    /**
     * 招聘信息详情
     */
    @Login
    @PostMapping("jobDetails")
    @ApiOperation("招聘信息详情")
    public R jobDetails(@ApiIgnore @RequestAttribute("userId") Integer userId, @RequestBody JobDetailsQueryForm form){
        //表单校验
        ValidatorUtils.validateEntity(form);
        form.setUser_id(Long.valueOf(userId));
        //返回家教招聘信息详情
        return R.ok().put("content", jobService.selectDetails(form));
    }

    /**
     * 老师信息填写
     */
    @Login
    @PostMapping("submit")
    @ApiOperation("老师信息填写")
    public R submit(@ApiIgnore @LoginUser UserEntity user, @RequestBody ResumeSubmitForm form){
        //表单校验
        ValidatorUtils.validateEntity(form);
        //生成老师信息
        TutorResumeEntity resume = new TutorResumeEntity(form);
        resume.setUserId(user.getUserId());
        resume.setUserName(user.getUsername());
        resumeService.insert(resume);
        return R.ok();
    }

    /**
     * 我要编辑
     */
    @Login
    @PostMapping("edit")
    @ApiOperation("我要编辑")
    public R edit(@ApiIgnore @RequestAttribute("userId") Integer userId, @RequestBody ResumeEditForm form){
        //表单校验
        ValidatorUtils.validateEntity(form);
        TutorResumeEntity resume = resumeService.selectById(form.getResume_id());
        if(!Long.valueOf(userId).equals(resume.getUserId())){
            return R.error("当前操作用户与简历所属用户不匹配！");
        }
        //修改老师信息
        resume.edit(form);
        resumeService.updateById(resume);
        return R.ok();
    }

    /**
     * 投递简历
     */
    @Login
    @PostMapping("deliveryResume")
    @ApiOperation("投递简历")
    public R deliveryResume(@ApiIgnore @RequestAttribute("userId") Integer userId, @RequestBody DeliveryResumeSubmitForm form){
        //表单校验
        ValidatorUtils.validateEntity(form);
        form.setResume_user_id(Long.valueOf(userId));
        deliveryService.deliveryResume(form);
        return R.ok();
    }

}

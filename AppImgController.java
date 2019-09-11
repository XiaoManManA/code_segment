package cn.jiuzhuang.modules.app.controller;

import cn.jiuzhuang.common.exception.JzException;
import cn.jiuzhuang.common.utils.R;
import cn.jiuzhuang.modules.app.config.UploadConfig;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/app")
@Api(tags = "图片服务器")
public class AppImgController {

    @Autowired
    private UploadConfig uploadConfig;

    /**
     * 批量上传
     */
    @PostMapping("/multipleFilesUpload")
    @ApiOperation("批量上传图片")
    public R multipleFilesUpload(HttpServletRequest request) {
        //获取上传的文件数组
        List<MultipartFile> files = ((MultipartHttpServletRequest) request).getFiles("file");
        //遍历处理文件
        List<Map<String, String>> fileList = Lists.newArrayList();
        for (MultipartFile file:files) {
            if (file.isEmpty()) {
                throw new JzException("上传文件不能为空");
            }
            //上传文件后缀名
            String suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            log.info(suffix);
            log.info(uploadConfig.getLicitFileType());
            log.info(JSON.toJSONString(Arrays.<String> asList(uploadConfig.getLicitFileType().split(","))));
            if ( !Arrays.<String> asList(uploadConfig.getLicitFileType().split(",")).contains(suffix)){
                throw new JzException("存在非法格式的文件，请检查文件后缀名并重新上传！");
            }
            //文件名称
            String fileName = System.currentTimeMillis() + suffix;
            try {
                FileCopyUtils.copy(file.getBytes(), new File(uploadConfig.getFilePath() + fileName));
                //写入响应数据列表
                Map<String, String> fileResult = Maps.newHashMap();
                fileResult.put("name", fileName);
                fileResult.put("url", uploadConfig.getImageUrlPrefix() + fileName);
                fileList.add(fileResult);
            } catch (IOException e) {
                throw new JzException("文件写入磁盘错误，请检查配置文件图片存放路径！");
            }
        }
        return R.ok().put("data", fileList);
    }

}

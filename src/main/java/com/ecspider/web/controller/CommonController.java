package com.ecspider.web.controller;

import com.ecspider.web.SpiderAdvanceCache;
import com.ecspider.web.model.Result;
import com.ecspider.web.model.SpiderAdvance;
import com.ecspider.web.service.CommonService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author lyifee
 * on 2020/12/27
 */
@Controller
public class CommonController {
    @Resource
    private CommonService commonService;

    @RequestMapping("/advance")
    @ResponseBody
    public Result getAdvance(@RequestParam(name = "keyword") String keyword) {
        SpiderAdvance advance = commonService.getAdvance(keyword);
        return Result.success(advance);
    }

    @RequestMapping("/index")
    public String index(HttpServletRequest request) {
        List<String> keywords = SpiderAdvanceCache.getGrabList();
        List<SpiderAdvance> advances = SpiderAdvanceCache.getAdvanceList();
        request.setAttribute("keywords", keywords);
        request.setAttribute("advances", advances);
        return "index";
    }
}

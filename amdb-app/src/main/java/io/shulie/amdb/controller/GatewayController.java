package io.shulie.amdb.controller;

import io.shulie.amdb.entity.ScriptDO;
import io.shulie.amdb.service.ScriptManager;
import io.shulie.amdb.service.ScriptManagerService;
import io.shulie.amdb.service.ScriptService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author xiaobin.zfb|xiaobin@shulie.io
 * @since 2023/6/7 15:16
 */
@RestController
@RequestMapping("/amdb/gateway")
public class GatewayController {
    @Resource
    private ScriptService scriptService;

    @Resource
    private ScriptManagerService scriptManagerService;

    @RequestMapping("data")
    public ModelAndView data(HttpServletRequest request, @RequestBody Object data) {
        String targetUrl = request.getParameter("targetUrl");
        String scriptCode = request.getParameter("scriptCode");
        ScriptDO scriptDO = scriptService.getScriptByCode(scriptCode);
        if (scriptDO == null) {
            throw new RuntimeException("script is not exists:" + scriptCode);
        }
        ScriptManager scriptManager = scriptManagerService.getScriptManager(scriptDO.getScriptType());
        if (scriptManager == null) {
            throw new RuntimeException("ScriptManager is not exists:" + scriptDO.getScriptType());
        }

        Object ret = scriptManager.executeScript(scriptDO, data);
        ModelAndView modelAndView = new ModelAndView("forward:" + targetUrl);
        modelAndView.addObject(ret);
        return modelAndView;
    }
}

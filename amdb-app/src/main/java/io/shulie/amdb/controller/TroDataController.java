package io.shulie.amdb.controller;

import io.shulie.amdb.common.Response;
import io.shulie.amdb.common.request.trodata.TrodataQueryParam;
import io.shulie.amdb.exception.AmdbExceptionEnums;
import io.shulie.amdb.service.TroDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author sunshiyu
 * @description 控制台数据查询controller
 * @datetime 2021-09-27 7:34 下午
 */
@Slf4j
@RestController
@RequestMapping("amdb/db/api/troData")
public class TroDataController {

    @Autowired
    private TroDataService troDataService;

    /**
     * 应用自定义探针配置查询
     *
     * @param param
     * @return
     */
    @RequestMapping(value = "/queryTroData", method = RequestMethod.GET)
    public Response<String> queryTroData(TrodataQueryParam param) {
        try {
            if (param.getAppName() == null || param.getConfigKey() == null) {
                return Response.fail(AmdbExceptionEnums.COMMON_EMPTY_PARAM);
            }
            return Response.success(troDataService.queryTroData(param));
        } catch (Exception e) {
            log.error("查询应用自定义探针配置异常{},异常堆栈:{}", e, e.getStackTrace());
            return Response.fail(AmdbExceptionEnums.TRODATA_QUERY);
        }
    }
}

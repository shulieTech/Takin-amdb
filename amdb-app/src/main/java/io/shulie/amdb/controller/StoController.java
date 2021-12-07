package io.shulie.amdb.controller;

import io.shulie.amdb.common.Response;
import io.shulie.amdb.common.dto.sto.StoQueryDTO;
import io.shulie.amdb.common.request.sto.StoQueryRequest;
import io.shulie.amdb.exception.AmdbExceptionEnums;
import io.shulie.amdb.service.StoService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Api("申通指标查询")
@RestController
@RequestMapping("/sto")
@Slf4j
public class StoController {
    @Autowired
    StoService stoService;

    @PostMapping("/getServiceMetrics")
    public Response<StoQueryDTO> getServiceMetrics(@RequestHeader("userAppKey") String userAppKey, @RequestBody StoQueryRequest requestParam) {
        //系统鉴权,如果是申通调用,则满足调用条件
        if (requestParam.getUserAppKey().equals(userAppKey)) {
            //参数校验
            if (checkParam(requestParam)) {
                //开始执行查询
                return stoService.getServiceMetrics(requestParam);
            } else {
                return Response.fail(AmdbExceptionEnums.STO_QUERY_ILLEGAL_PARAM);
            }
        } else {
            return Response.fail(AmdbExceptionEnums.STO_QUERY_NOT_AUTH);
        }
    }

    private boolean checkParam(StoQueryRequest request) {
        //必传参数校验
        if (StringUtils.isBlank(request.getAppName()) || StringUtils.isBlank(request.getStartTime()) || StringUtils.isBlank(request.getEndTime())) {
            return false;
        }
        //数据格式校验
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date startTime = format.parse(request.getStartTime());
            Date endTime = format.parse(request.getEndTime());
            request.setInterval((endTime.getTime() - startTime.getTime()) / 1000);


            //如果当前时间 - 开始时间 > 3天,参数校验不满足 || 结束时间>当前时间
            if (System.currentTimeMillis() - startTime.getTime() > 3 * 24 * 60 * 60 * 1000 || endTime.getTime() > System.currentTimeMillis()) {
                return false;
            }
        } catch (ParseException e) {
            return false;
        }
        return true;
    }

}

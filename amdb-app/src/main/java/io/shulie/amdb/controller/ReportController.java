package io.shulie.amdb.controller;

import java.util.function.Supplier;

import javax.annotation.Resource;

import io.shulie.amdb.common.Response;
import io.shulie.amdb.exception.AmdbExceptionEnums;
import io.shulie.amdb.request.query.ReportInterfaceQueryRequest;
import io.shulie.amdb.service.ReportActivityInterfaceService;
import io.shulie.amdb.service.ReportActivityService;
import io.shulie.amdb.service.ReportInterfaceMetricsService;
import io.swagger.annotations.Api;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(description = "压测报告")
@RestController
@RequestMapping("amdb/report")
public class ReportController {

    @Resource
    private ReportActivityService activityService;

    @Resource
    private ReportActivityInterfaceService interfaceService;

    @Resource
    private ReportInterfaceMetricsService metricsService;

    @PostMapping("activity/list")
    public Response<Object> queryReportActivity(@RequestBody ReportInterfaceQueryRequest request) {
        return exec(request, () -> activityService.queryReportActivity(request));
    }

    @PostMapping("activity/interface/list")
    public Response<Object> queryReportActivityInterface(@RequestBody ReportInterfaceQueryRequest request) {
        return exec(request, () -> interfaceService.queryReportActivityInterface(request));
    }

    @PostMapping("activity/interface/metrics")
    public Response<Object> queryReportInterfaceMetrics(@RequestBody ReportInterfaceQueryRequest request) {
        return exec(request, () -> metricsService.queryReportInterfaceMetrics(request));
    }

    @GetMapping("create/table")
    private Response<Object> createReportTable(ReportInterfaceQueryRequest request) {
        return exec(request, () -> activityService.createReportTable(request.getReportId()));
    }

    private Response<Object> exec(ReportInterfaceQueryRequest request, Supplier<Object> supplier) {
        if (inValidateRequiredParam(request)) {
            return Response.fail(AmdbExceptionEnums.COMMON_EMPTY_PARAM);
        }
        try {
            return Response.success(supplier.get());
        } catch (Exception e) {
            return Response.fail(e.getMessage());
        }
    }

    private boolean inValidateRequiredParam(ReportInterfaceQueryRequest request) {
        return StringUtils.isBlank(request.getReportId());
    }
}

package io.shulie.amdb.controller;

import io.shulie.amdb.common.Response;
import io.shulie.amdb.common.dto.waterline.TendencyChart;
import io.shulie.amdb.common.dto.waterline.WaterlineMetrics;
import io.shulie.amdb.service.WaterlineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("amdb/db/api/waterline")
public class WaterlineController {

    @Autowired
    private WaterlineService waterlineService;

    @GetMapping("/getAllApplicationWithMetrics")
    public Response<List<WaterlineMetrics>> getAllApplicationWithMetrics(
            @RequestParam(name = "names") List<String> names,
            @RequestParam(name = "startTime") String startTime,
            @RequestParam(name = "tenantAppKey")String tenantAppKey,
            @RequestParam(name = "tenantAppKey")String envCode
    ) {
        return Response.success(waterlineService.getAllApplicationWithMetrics(names,startTime,tenantAppKey,envCode));
    }

    @GetMapping("/getTendencyChart")
    public Response<List<TendencyChart>> getTendencyChart(
            @RequestParam(name = "applicationName") String applicationName,
            @RequestParam(name = "startTime") String startTime,
            @RequestParam(name = "endTime") String endTime,
            @RequestParam(name = "nodes") List<String> nodes,
            @RequestParam(name = "tenantAppKey")String tenantAppKey,
            @RequestParam(name = "tenantAppKey")String envCode
    ) {
        return Response.success(waterlineService.getTendencyChart(applicationName,startTime,endTime,nodes,tenantAppKey,envCode));
    }

}

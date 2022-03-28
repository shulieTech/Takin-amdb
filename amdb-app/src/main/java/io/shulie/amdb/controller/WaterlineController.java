package io.shulie.amdb.controller;

import io.shulie.amdb.common.Response;
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
    public Response<List<WaterlineMetrics>> getAllApplicationWithMetrics(@RequestParam(name = "names") List<String> names,@RequestParam(name = "startTime") String startTime) {
        return Response.success(waterlineService.getAllApplicationWithMetrics(names,startTime));
    }

}

package io.shulie.amdb.controller;

import javax.annotation.Resource;

import io.shulie.amdb.common.Response;
import io.shulie.amdb.service.ReportTaskService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api("压测报告")
@RequestMapping(value = "/amdb/report/task")
public class ReportTaskController {

    @Resource
    private ReportTaskService reportTaskService;

    /**
     * 启动数据处理任务
     *
     * @return true-启动成功
     */
    @GetMapping(value = "/start")
    public Response<Boolean> startAnalyzeTask(String taskId) {
        try {
            reportTaskService.startTask(taskId);
            return Response.emptySuccess();
        } catch (Exception e) {
            return Response.fail(e.getMessage());
        }
    }
}

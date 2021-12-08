/*
 * Copyright 2021 Shulie Technology, Co.Ltd
 * Email: shulie@shulie.io
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.shulie.amdb.request.query;

import io.shulie.amdb.common.request.AbstractAmdbBaseRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import java.util.ArrayList;
import java.util.List;

@Data
public class MetricsDetailQueryRequest extends AbstractAmdbBaseRequest {

    String appName;

    String startTime;

    String endTime;

    int clusterTest;             //-1,混合  0,业务  1,压测

    String serviceName;         //服务列表

    String activityName;        //业务活动

    List<String> attentionList;       //关注的活动

    String orderBy;             //排序字段

    @ApiModelProperty("分页大小")
    private Integer pageSize = 10;

    @ApiModelProperty("请求页")
    private Integer currentPage = 0;

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public int getClusterTest() {
        return clusterTest;
    }

    public void setClusterTest(int clusterTest) {
        this.clusterTest = clusterTest;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public List<String> getAttentionList() {
        return attentionList == null ? new ArrayList<>(0) : attentionList;
    }

    public void setAttentionList(List<String> attentionList) {
        this.attentionList = attentionList;
    }

    public String getOrderBy() {
        return StringUtils.isBlank(orderBy) ? "QPS desc" : orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }
}

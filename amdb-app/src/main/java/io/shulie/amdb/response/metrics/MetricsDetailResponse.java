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

package io.shulie.amdb.response.metrics;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
public class MetricsDetailResponse implements Serializable {

    String appName;
    String service;
    String method;
    String serviceAndMethod;
    String middlewareName;

    //指标汇总
    float requestCount;
    float tps;
    float responseConsuming;        //相应耗时
    float successRatio;             //成功率
    int rpcType;
    String startTime;
    String endTime;
    int timeGap;


    //关联业务活动
    List<String> activeList;

    Set<String> allActiveList;

    public Set<String> getAllActiveList() {
        return allActiveList;
    }

    public void setAllActiveList(Set<String> allActiveList) {
        this.allActiveList = allActiveList;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getServiceAndMethod() {
        return serviceAndMethod;
    }

    public void setServiceAndMethod(String serviceAndMethod) {
        this.serviceAndMethod = serviceAndMethod;
    }

    public float getTps() {
        return tps;
    }

    public void setTps(float tps) {
        this.tps = tps;
    }

    public float getResponseConsuming() {
        return responseConsuming;
    }

    public void setResponseConsuming(float responseConsuming) {
        this.responseConsuming = responseConsuming;
    }

    public String getMiddlewareName() {
        return middlewareName;
    }

    public void setMiddlewareName(String middlewareName) {
        this.middlewareName = middlewareName;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public List<String> getActiveList() {
        return activeList;
    }

    public void setActiveList(List<String> activeList) {
        this.activeList = activeList;
    }

    public int getRpcType() {
        return rpcType;
    }

    public void setRpcType(int rpcType) {
        this.rpcType = rpcType;
    }

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

    public int getTimeGap() {
        return timeGap;
    }

    public void setTimeGap(int timeGap) {
        this.timeGap = timeGap;
    }

    public boolean equals(Object obj){
        if(obj instanceof MetricsDetailResponse){
            MetricsDetailResponse temp = (MetricsDetailResponse) obj;
            //仅限控制台应用tab页场景使用
            if(this.service.equals(temp.getService())&&this.method.equals(temp.getMethod())){
                return true;
            }
        }
        return false;
    }

    public int hashCode(){
        int result = 17;
        result = 31 * result + (appName == null ? 0 : appName.hashCode());
        result = 31 * result + (service == null ? 0 : service.hashCode());
        result = 31 * result + (method == null ? 0 : method.hashCode());
        return result;
    }

}

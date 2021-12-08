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
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Data
public class MetricsFromInfluxdbQueryRequest extends AbstractAmdbBaseRequest {
    String startTime;
    String endTime;
    int timeGap;

    String inAppName;          //入口应用
    String inService;          //入口接口
    String inMethod;           //入口方法

    String entranceStr;         //入口集合
    String fromAppName;         //上游应用
    String middlewareName;      //容器,界面区分多变
    String appName;             //应用
    String service;             //接口
    String method;              //方法

    int clusterTest;            //-1,混合  0,业务  1,压测

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public String getEndTime() {
        if(this.timeGap==0){    //非点击列表进入,自动刷新或手动刷新情况进入
            //不做复杂考虑验证,直接前推1.5分钟
            try {
                Date date =df.parse(this.endTime);
                date.setTime(date.getTime() - (90*1000));
                this.endTime = df.format(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public int getClusterTest() {
        return clusterTest;
    }

    public void setClusterTest(int clusterTest) {
        this.clusterTest = clusterTest;
    }

    public String getInAppName() {
        return inAppName;
    }

    public void setInAppName(String inAppName) {
        this.inAppName = inAppName;
    }

    public String getInService() {
        return inService;
    }

    public void setInService(String inService) {
        this.inService = inService;
    }

    public String getInMethod() {
        return inMethod;
    }

    public void setInMethod(String inMethod) {
        this.inMethod = inMethod;
    }

    public String getFromAppName() {
        return fromAppName;
    }

    public void setFromAppName(String fromAppName) {
        this.fromAppName = fromAppName;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
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

    public String getEntranceStr() {
        return entranceStr;
    }

    public void setEntranceStr(String entranceStr) {
        this.entranceStr = entranceStr;
    }

    public int getTimeGap() {
        if(this.timeGap==0){
            this.timeGap = 210; //300s-90s=210s 前推1分30s
        }
        return timeGap;
    }

    public void setTimeGap(int timeGap) {
        this.timeGap = timeGap;
    }

    public String getMiddlewareName() {
        return middlewareName;
    }

    public void setMiddlewareName(String middlewareName) {
        this.middlewareName = middlewareName;
    }

    public boolean isNotEmptyForEntrance(){
        if(StringUtils.isBlank(this.startTime)||
                StringUtils.isBlank(this.endTime)||
                StringUtils.isBlank(this.inAppName)||
                StringUtils.isBlank(this.inService)||
                StringUtils.isBlank(this.inMethod)
        ){
           return false;
        }
        return true;
    }

    public boolean isNotEmptyForMetric(){
        if(StringUtils.isBlank(this.startTime)||
                StringUtils.isBlank(this.endTime)||
                StringUtils.isBlank(this.entranceStr)||
                //StringUtils.isBlank(this.fromAppName)||
                StringUtils.isBlank(this.appName)||
                StringUtils.isBlank(this.service)||
                StringUtils.isBlank(this.method)
        ){
            return false;
        }
        return true;
    }
}

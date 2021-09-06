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

package io.shulie.amdb.common;

import com.github.pagehelper.PageInfo;
import io.shulie.amdb.exception.AmdbException;
import io.shulie.amdb.exception.AmdbExceptionEnums;
import org.assertj.core.util.Lists;

import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.util.List;

public class Response<T> implements Serializable {
    private static final long serialVersionUID = 45387487319877474L;
    private ErrorInfo error;
    private T data;
    private long total;
    private Boolean success;
    private String emptyMsg;
    public static final String PAGE_TOTAL_HEADER = "x-total-count";
    public static final boolean DEFAULT_SUCCESS = true;

    public Response(T data) {
        this(null, data, true);
    }

    public Response(ErrorInfo error, boolean success) {
        this(error, null, success);
    }

    public Response(ErrorInfo error, T data, boolean success) {
        this.error = error;
        this.data = data;
        this.success = success;
        if (data != null) {
            this.total = 1L;
        }
    }

    public Response setTotal(Long total) {
        HttpServletResponse response = ServletContextHolder.getResponse();
        response.setHeader("Access-Control-Expose-Headers", "x-total-count");
        response.setHeader("x-total-count", total + "");
        this.total = total;
        return this;
    }

    public static <T> Response<T> emptySuccess(String emptyMsg) {
        Response response = new Response((Object) null);
        response.emptyMsg = emptyMsg;
        response.success = true;
        return response;
    }

    public static <T> Response<T> emptySuccess() {
        Response response = new Response((Object) null);
        response.success = true;
        return response;
    }


    public static <T> Response<T> fail() {
        return fail(AmdbExceptionEnums.COMMON_UNDEFINE);
    }

    public static <T> Response<T> fail(String message) {
        return fail(AmdbExceptionEnums.COMMON_UNDEFINE_STRING_DESC.getCode(),
                AmdbExceptionEnums.COMMON_UNDEFINE_STRING_DESC.getMsg(),
                message);
    }

    public static <T> Response<T> fail(AmdbException exception, Object... args) {
        return fail(exception.getCode(), exception.getMessage(), args);
    }

    public static <T> Response<T> fail(AmdbExceptionEnums exception, Object... args) {
        return fail(exception.getCode(), exception.getMsg(), args);
    }

    public static <T> Response<T> success(T data) {
        return data instanceof PageInfo ? success(data) : new Response(data);
    }

    public static <T> Response<PageInfo<T>> successPageInfo(PageInfo<T> data) {
        return new Response(data);
    }

    public static <T> Response<List<T>> success(PageInfo<T> data) {
        List<T> list = Lists.newArrayList();
        long total = 0L;
        if (null != data) {
            list = data.getList();
            total = data.getTotal();
        }

        HttpServletResponse response = ServletContextHolder.getResponse();
        response.setHeader("Access-Control-Expose-Headers", "x-total-count");
        response.setHeader("x-total-count", total + "");
        Response result = new Response(list);
        result.setTotal(total);
        return result;
    }

    public static <T> Response<List<T>> success(List<T> data, long total) {
        HttpServletResponse response = ServletContextHolder.getResponse();
        response.setHeader("Access-Control-Expose-Headers", "x-total-count");
        response.setHeader("x-total-count", total + "");
        Response result = new Response(data);
        result.setTotal(total);
        return result;
    }

    public static <T> Response<T> fail(String code, String msgTemplate, Object... args) {
        ErrorInfo errorInfo = ErrorInfo.build(code, msgTemplate, args);
        return new Response(errorInfo, false);
    }

    public static <T> Response<T> fail(ErrorInfo errorInfo) {
        return new Response(errorInfo, false);
    }

    public ErrorInfo getError() {
        return this.error;
    }

    public T getData() {
        return this.data;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public Boolean isSuccess() {
        return this.success;
    }
}

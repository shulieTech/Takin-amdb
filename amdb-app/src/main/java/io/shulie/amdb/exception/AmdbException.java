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

package io.shulie.amdb.exception;

import lombok.Getter;

/**
 * @Author: xingchen
 * @ClassName: AmdbExcetion
 * @Package: io.shulie.amdb.exception
 * @Date: 2020/10/1914:26
 * @Description:
 */
@Getter
public class AmdbException extends RuntimeException {

    /**
     * 异常编码
     */
    private String code;

    public AmdbException(AmdbExceptionEnums exceptionEnums) {
        super(exceptionEnums.getMsg());
        this.code = exceptionEnums.getCode();
    }
    public AmdbException(AmdbExceptionEnums exceptionEnums, Throwable cause) {
        super(exceptionEnums.getMsg(), cause);
        this.code = exceptionEnums.getCode();
    }

    public AmdbException(Throwable cause) {
        super(cause);
    }

    public AmdbException(AmdbExceptionEnums exceptionEnums, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(exceptionEnums.getMsg(), cause, enableSuppression, writableStackTrace);
        this.code = exceptionEnums.getCode();
    }
}

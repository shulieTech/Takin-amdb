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

package io.shulie.amdb;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;

/**
 * @description: 启动类
 * @author: CaoYanFei@ShuLie.io
 * @create: 2020-07-15 22:26
 **/
@SpringBootApplication
@EnableScheduling
@EnableAsync
@Slf4j
public class AMDBAPIBootstrap {

    public static void main(String[] args) throws IOException {
        SpringApplication.run(AMDBAPIBootstrap.class, args);
    }

}

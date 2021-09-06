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

package io.shulie.amdb.utils;

import okhttp3.OkHttpClient;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.ssl.SSLContexts;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.impl.InfluxDBResultMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import javax.net.ssl.*;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Author: xingchen
 * @ClassName: InfluxDBManager
 * @Package: io.shulie.tro.report.service
 * @Date: 2020/7/2717:03
 * @Description:
 */
@Service
public class InfluxDBManager implements AutoCloseable {
    private static Logger logger = LoggerFactory.getLogger(InfluxDBManager.class);

    private static final ThreadLocal<InfluxDB> CACHE = new ThreadLocal<>();
    private static final InfluxDBResultMapper RESULT_MAPPER = new InfluxDBResultMapper();

    @Value("${influx.openurl}")
    private String influxdbUrl;
    @Value("${influx.username}")
    private String userName;
    @Value("${influx.password}")
    private String password;
    @Value("${influx.database}")
    private String database;

    private InfluxDB createInfluxDb() {
        InfluxDB influxDb;
        try {
            if (StringUtils.isBlank(influxdbUrl)) {
                throw new RuntimeException("url is null");
            }
            if (influxdbUrl.startsWith("https")) {
                OkHttpClient.Builder client = new OkHttpClient.Builder()
                        .connectTimeout(60, TimeUnit.SECONDS)
                        .writeTimeout(60, TimeUnit.SECONDS)
                        .readTimeout(60, TimeUnit.SECONDS)
                        .retryOnConnectionFailure(true);
                client.sslSocketFactory(defaultSslSocketFactory(), defaultTrustManager());
                client.hostnameVerifier(noopHostnameVerifier());
                influxDb = InfluxDBFactory.connect(influxdbUrl, userName, password, client);
            } else {
                influxDb = InfluxDBFactory.connect(influxdbUrl, userName, password);
            }
            return influxDb;
        } catch (Throwable e) {
            logger.error("influxdb init fail ", e);
        }
        return null;
    }

    private static SSLSocketFactory defaultSslSocketFactory() {
        try {
            SSLContext sslContext = SSLContexts.createDefault();
            sslContext.init(null, new TrustManager[]{
                    defaultTrustManager()
            }, new SecureRandom());
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private static HostnameVerifier noopHostnameVerifier() {
        return (s, sslSession) -> {
            return true;//true 表示使用ssl方式，但是不校验ssl证书，建议使用这种方式
        };
    }

    private static X509TrustManager defaultTrustManager() {
        return new X509TrustManager() {
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }

            @Override
            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        };
    }

    private InfluxDB getInfluxDb() {
        InfluxDB influxDb = CACHE.get();
        if (influxDb == null) {
            influxDb = createInfluxDb();
            CACHE.set(influxDb);
        }
        return influxDb;
    }

    @PreDestroy
    public void destory() {
        InfluxDB influxDb = CACHE.get();
        if (influxDb != null) {
            influxDb.close();
        }
        CACHE.remove();
    }

    public <T> Collection<T> query(Class<T> clazz, String command) {
        return query(clazz, new Query(command, database));
    }

    public <T> Collection<T> query(Class<T> clazz, String command, String database) {
        return query(clazz, new Query(command, database));
    }

    public List<QueryResult.Result> query(String command) {
        return parseRecords(command);
    }

    public <T> Collection<T> query(Class<T> clazz, Query query) {
        InfluxDB influxDb = getInfluxDb();
        if (influxDb != null) {
            QueryResult queryResult = influxDb.query(query);
            if (queryResult != null && CollectionUtils.isNotEmpty(queryResult.getResults())) {
                return RESULT_MAPPER.toPOJO(queryResult, clazz);
            }
        }
        return null;
    }

    public <T> Collection<T> query(Class<T> clazz, Query query, String measurementName) {
        InfluxDB influxDb = getInfluxDb();
        if (influxDb != null) {
            QueryResult queryResult = influxDb.query(query);
            if (queryResult != null && CollectionUtils.isNotEmpty(queryResult.getResults())) {
                return RESULT_MAPPER.toPOJO(queryResult, clazz, measurementName);
            }
        }
        return null;
    }

    @Override
    public void close() {
        InfluxDB influxDb = CACHE.get();
        if (influxDb != null) {
            influxDb.close();
        }
    }

    private List<QueryResult.Result> parseRecords(String command) {
        QueryResult queryResult = getInfluxDb().query(new Query(command, database));
        List<QueryResult.Result> results = queryResult.getResults();
        if (results == null || results.size() == 0) {
            return null;
        }
        return results;
    }
}

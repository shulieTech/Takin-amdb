package io.shulie.amdb.service.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import io.shulie.amdb.entity.ReportActivityDO;
import io.shulie.amdb.mapper.ReportActivityMapper;
import io.shulie.amdb.request.query.ReportInterfaceQueryRequest;
import io.shulie.amdb.service.ReportActivityService;
import io.shulie.surge.data.sink.clickhouse.ClickHouseShardSupport;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.yandex.clickhouse.BalancedClickhouseDataSource;
import ru.yandex.clickhouse.ClickHouseConnection;

@Service
@Slf4j
public class ReportActivityServiceImpl implements ReportActivityService {

    @Resource
    private ReportActivityMapper mapper;

    @Value("${config.clickhouse.url}")
    private String clickhouseUrl;

    @Value("${config.clickhouse.userName}")
    private String clickhouseUserName;

    @Value("${config.clickhouse.password}")
    private String clickhousePassword;

    private static final String DEFAULT_SEARCH_LOCATIONS = "file:./config/,classpath:/";

    private static final String DISTRIBUTE_SQL = "CREATE TABLE {distribute_table_name} on cluster ck_cluster "
        + " as {table_name} ENGINE = Distributed(ck_cluster, default, {table_name}, sipHash64(traceId))";

    @Override
    public List<ReportActivityDO> queryReportActivity(ReportInterfaceQueryRequest request) {
        return mapper.selectByRequest(request);
    }

    @Override
    public boolean createReportTable(String reportId) {
        ClickHouseShardSupport shardSupport = new ClickHouseShardSupport();
        List<String> urls = shardSupport.splitUrl(clickhouseUrl);
        boolean cluster = isCluster(urls);
        try {
            this.createLocalTable(reportId, urls, cluster);
            this.createDistributeTable(reportId, urls, cluster);
            return true;
        } catch (Exception e) {
            log.error("压测报告建表异常", e);
            return false;
        }
    }

    /**
     * 创建本地表，此处脚本是on cluster模式，只需要选取一个节点执行就行了,失败不重试
     *
     * @param reportId  报告Id
     * @param urls      集群url
     * @param isCluster 是否集群
     * @throws Exception 文件读取/建表异常
     */
    private void createLocalTable(String reportId, List<String> urls, boolean isCluster) throws Exception {
        String createTableScript = findCreateTableSql(reportId, isCluster);
        for (String url : urls) {
            BalancedClickhouseDataSource dataSource = buildDataSource(url);
            try (ClickHouseConnection connection = dataSource.getConnection(clickhouseUserName, clickhousePassword)) {
                connection.createStatement().execute(createTableScript);
            }
        }
    }

    /**
     * 创建分布式表,失败不重试
     *
     * @param reportId 报告Id
     * @param urls     集群url
     * @throws Exception 文件读取/建表异常
     */
    private void createDistributeTable(String reportId, List<String> urls, boolean isCluster) throws Exception {
        if (isCluster) {
            String createTableScript = findCreateTableSql(reportId, true);
            BalancedClickhouseDataSource dataSource = buildDataSource(urls.get(0));
            try (ClickHouseConnection connection = dataSource.getConnection(clickhouseUserName, clickhousePassword)) {
                connection.createStatement().execute(createTableScript);
                // 分布式表：
                connection.createStatement().execute(buildDistributeSql(reportId));
            }
        }
    }

    /**
     * 通过url构造dataSource
     *
     * @param url 数据源url,会自动判断是否需要添加前缀
     * @return 数据源 {@link BalancedClickhouseDataSource}
     */
    private BalancedClickhouseDataSource buildDataSource(String url) {
        return new BalancedClickhouseDataSource(url);
    }

    private static final String SCRIPT_FILE_NAME = "t_pressure_report.sql";
    private static final String TABLE_NAME_PLACEHOLDER = "{table_name}";
    private static final String DISTRIBUTE_TABLE_NAME_PLACEHOLDER = "{distribute_table_name}";
    private static final String CLUSTER_NAME_PLACEHOLDER = "{cluster_name}";

    /**
     * 返回表结构，并替换其中的 {table_name}
     *
     * @param isCluster 是否集群
     * @return 建表语句
     * @throws IOException 脚本文件内容读取发生IO异常
     */
    private String findCreateTableSql(String reportId, boolean isCluster) throws IOException {
        String scriptFile = findScriptFile();
        if (scriptFile != null) {
            // 获取脚本文件内容
            return scriptFile.replace(TABLE_NAME_PLACEHOLDER, tableName(reportId, isCluster))
                .replace(CLUSTER_NAME_PLACEHOLDER, isCluster ? " on cluster ck_cluster " : "");
        }
        throw new IllegalStateException("report script file [" + DEFAULT_SEARCH_LOCATIONS + "] content is empty");
    }

    private String findScriptFile() throws IOException {
        DefaultResourceLoader loader = new DefaultResourceLoader();
        String[] locations = StringUtils.trimArrayElements(StringUtils.commaDelimitedListToStringArray(DEFAULT_SEARCH_LOCATIONS));
        for (String location : locations) {
            org.springframework.core.io.Resource resource = loader.getResource(location + SCRIPT_FILE_NAME);
            if (resource.exists()) {
                String content = IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);
                if (StringUtils.hasText(content)) {
                    return content;
                }
            }
        }
        return null;
    }

    private String tableName(String reportId, boolean isCluster) {
        return (isCluster ? "t_pressure_" : "t_trace_pressure_") + reportId;
    }

    private String buildDistributeSql(String reportId) {
        return DISTRIBUTE_SQL.replace(DISTRIBUTE_TABLE_NAME_PLACEHOLDER, tableName(reportId, false))
            .replace(TABLE_NAME_PLACEHOLDER, tableName(reportId, true));
    }

    private boolean isCluster(List<String> urls) {
        Set<String> urlSet = formatUrl(urls);
        Set<String> finalUrls = urlSet.stream().map(var -> {
            try {
                return InetAddress.getByName(var).getHostAddress();
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return "";
        }).collect(Collectors.toSet());
        return finalUrls.size() > 1;
    }

    private Set<String> formatUrl(List<String> urls) {
        if (CollectionUtils.isEmpty(urls)) {
            throw new RuntimeException("clickhouse url is null");
        }
        return urls.stream().map(url -> url.substring(url.indexOf("//") + 2, url.lastIndexOf(":"))).collect(Collectors.toSet());
    }
}

package io.shulie.amdb.config;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.NamedThreadLocal;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

@Configuration
public class CustomizedWebMcvConfigurer implements WebMvcConfigurer {

    @Value("${config.metrics.cost.enable:false}")
    private boolean costEnable;

    @Value("#{'${config.metrics.cost.patterns:/amdb/**}'.split(\",\")}")
    private String[] costPatterns;

    @Resource(name = "costMetricsInterceptor")
    private HandlerInterceptor costMetricsInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (costEnable) {
            registry.addInterceptor(costMetricsInterceptor).addPathPatterns(costPatterns);
        }
    }

    @Bean("costMetricsInterceptor")
    public HandlerInterceptor costMetricsInterceptor() {
        return new CostMetricsInterceptor();
    }

    @Slf4j
    static final class CostMetricsInterceptor extends HandlerInterceptorAdapter {

        private final NamedThreadLocal<Long> startTimeThreadLocal = new NamedThreadLocal<>("interface-cost-metrics");

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            startTimeThreadLocal.set(System.currentTimeMillis());
            return true;
        }

        @Override
        public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
            if (log.isInfoEnabled()) {
                long endTime = System.currentTimeMillis();
                Long startTime = startTimeThreadLocal.get();
                log.info("[ interface cost metrics ]： url=[{}] startTime=[{}], endTime=[{}], cost=[{} ms]",
                    request.getRequestURI(), startTime, endTime, endTime - startTime);
            }
            startTimeThreadLocal.remove();
        }
    }
}

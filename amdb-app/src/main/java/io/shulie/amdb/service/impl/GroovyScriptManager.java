package io.shulie.amdb.service.impl;

import io.shulie.amdb.entity.ScriptDO;
import io.shulie.amdb.service.ScriptManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.scripting.groovy.GroovyScriptEvaluator;
import org.springframework.scripting.support.StaticScriptSource;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author xiaobin.zfb|xiaobin@shulie.io
 * @since 2023/6/7 16:12
 */
@Component("groovy")
@Slf4j
public class GroovyScriptManager implements ScriptManager {
    private GroovyScriptEvaluator groovyScriptEvaluator = new GroovyScriptEvaluator();

    @Override
    public <S, T> T executeScript(ScriptDO scriptDO, S source) {
        StaticScriptSource scriptSource = new StaticScriptSource(scriptDO.getScript());
        if (source instanceof Map) {
            return (T) groovyScriptEvaluator.evaluate(scriptSource, (Map) source);
        } else {
            Map<String, Object> map = toMap(source);
            return (T) groovyScriptEvaluator.evaluate(scriptSource, map);
        }
    }

    private static Map<String, Object> toMap(Object source) {
        Map<String, Object> map = new HashMap<>();
        BeanMap beanMap = BeanMap.create(source);
        for (Object object : beanMap.entrySet()) {
            if (object instanceof Map.Entry) {
                Map.Entry<String, Object> entry = (Map.Entry<String, Object>) object;
                String key = entry.getKey();
                Object value = entry.getValue();
                map.put(key, value);
            }
        }
        return map;
    }
}

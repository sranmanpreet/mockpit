package com.ms.utils.mockpit.service;

import com.ms.utils.mockpit.dto.MockResponse;
import org.graalvm.polyglot.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

@Service
public class JavaScriptExecutionService {

    private final Logger LOGGER = LoggerFactory.getLogger(JavaScriptExecutionService.class);

    @Autowired
    private Environment env;

    public Object execute(Object snippet, Map<String, String[]> queryParams, Map<String,String> pathVariables) {
        try (Context context = Context.newBuilder("js").build()) {
            String queryParamPrefix = env.getProperty("dynamic-mocks.query-parameter-prefix");
            if(Objects.isNull(queryParamPrefix) || queryParamPrefix.isEmpty()){
                queryParamPrefix = "queryParameter___";
            }
            String finalQueryParamPrefix = queryParamPrefix;

            String pathVariablePrefix = env.getProperty("dynamic-mocks.path-variable-prefix");
            if(Objects.isNull(pathVariablePrefix) || pathVariablePrefix.isEmpty()){
                pathVariablePrefix = "pathVariable___";
            }
            String finalPathVariablePrefix = pathVariablePrefix;
            queryParams.forEach((k, v)->{

                String key = finalQueryParamPrefix + k;
                context.getBindings("js").putMember(key, v[0]);
            });
            pathVariables.forEach((k,v)->{
                String key = finalPathVariablePrefix + k;
                context.getBindings("js").putMember(key, v);
            });
            String jsCode = "(() => { " + snippet + " })();";
            return context.eval("js", jsCode).toString();
        } catch (Exception e){
            LOGGER.info("JavaScript execution failed.", e.getMessage());
            return new MockResponse(e.getMessage(), "");
        }
    }
}

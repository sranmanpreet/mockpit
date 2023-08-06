package com.ms.utils.mockpit.service;

import com.ms.utils.mockpit.dto.MockResponse;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class JavaScriptExecutionService {

    private final Logger LOGGER = LoggerFactory.getLogger(JavaScriptExecutionService.class);

    public Object execute(Object snippet, Map<String, String[]> queryParams, Map<String,String> pathVariables) {
        try (Context context = Context.newBuilder("js").build()) {
            queryParams.forEach((k,v)->{
                String key = "queryParameter___" + k;
                context.getBindings("js").putMember(key, v[0]);
            });
            pathVariables.forEach((k,v)->{
                String key = "pathVariable___" + k;
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

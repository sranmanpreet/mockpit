package com.ms.utils.moock.service;

import com.ms.utils.moock.dto.MockResponse;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class JavaScriptExecutionService {

    private final Logger LOGGER = LoggerFactory.getLogger(JavaScriptExecutionService.class);

    public Object execute(Object snippet) {
        try (Context context = Context.newBuilder("js").build()) {
            String jsCode = "(() => { " + snippet + " })();";
            return context.eval("js", jsCode).toString();
        } catch (Exception e){
            LOGGER.info("JavaScript execution failed.", e.getMessage());
            return new MockResponse(e.getMessage(), null);
        }
    }
}

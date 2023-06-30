package com.ms.utils.moock.service;

import org.graalvm.polyglot.Context;
import org.springframework.stereotype.Service;

@Service
public class JavaScriptExecutionService {

    public String execute(Object snippet) {
        try (Context context = Context.newBuilder("js").build()) {
            String jsCode = "(() => { " + snippet + " })();";
            return context.eval("js", jsCode).toString();
        }
    }
}

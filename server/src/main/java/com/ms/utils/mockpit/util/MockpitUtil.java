package com.ms.utils.mockpit.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MockpitUtil {

    public static boolean doUrlsMatch(String incomingUrl, String configuredUrl) {

        String regex = configuredUrl.replaceAll(":(\\w+)", "(\\\\w+)");

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(removeQueryParameters(incomingUrl));

        return matcher.matches();
    }

    private static String removeQueryParameters(String url) {
        String regex = "\\?.*";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);

        return matcher.replaceAll("");
    }

    public static String patternMatcher(String url) {
        String pattern = "/:(\\w+)";
        Pattern regexPattern = Pattern.compile(pattern);
        Matcher matcher = regexPattern.matcher(url);

        while (matcher.find()) {
            String pathVariable = matcher.group(1);
            System.out.println("Path Variable: " + pathVariable);
        }
        return "";
    }
}

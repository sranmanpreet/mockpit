package com.ms.utils.mockpit.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MockpitUtil {

    public static boolean isMatch(String incomingUrl, String configuredUrl) {

        String regex = removeTrailingSlashes(removeQueryParameters(configuredUrl)).replaceAll(":([\\w-]+)", "([\\\\w-]+)");
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(removeTrailingSlashes(removeQueryParameters(incomingUrl)));

        return matcher.matches();
    }

    private static String removeQueryParameters(String url) {
        String regex = "\\?.*";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);

        return matcher.replaceAll("");
    }

    public static Map<String, String> getPathVariableMap(String incomingUrl, String configuredUrl) {
        incomingUrl = removeTrailingSlashes(removeQueryParameters(incomingUrl));
        configuredUrl = removeTrailingSlashes(removeQueryParameters(configuredUrl));
        Map<String, String> pathVariablesMap = new HashMap<>();

        int i=0,j=0, x=0, y=0, n=configuredUrl.length()-1, m=incomingUrl.length()-1;

        while(i<=n && j<=n && i<=j){
            if(configuredUrl.charAt(j) == incomingUrl.charAt(y)){
                j++;
                y++;
                continue;
            }
            if(configuredUrl.charAt(j)==':'){
                j++;
                i = j;
                while(j<=n && configuredUrl.charAt(j)!='/'){
                    j++;
                }
                x=y;
                while(y<=m && incomingUrl.charAt(y)!='/'){
                    y++;
                }
                pathVariablesMap.put(configuredUrl.substring(i,j), incomingUrl.substring(x,y));
            } else {
                j++;
                y++;
            }
        }
        return pathVariablesMap;
    }

    private static String removeTrailingSlashes(String url) {
        String regex = "/+$";
        return url.replaceAll(regex, "");
    }
}

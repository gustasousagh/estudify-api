package com.gustasousagh.studify.estudify.utils;

import org.apache.commons.text.StringEscapeUtils;
import org.springframework.stereotype.Component;

@Component
public class ContentCleaner {

    public String cleanContent(String content) {
        if (content == null) return "";
        if (content.contains("\\n") || content.contains("\\t") || content.matches(".*\\\\u[0-9a-fA-F]{4}.*")) {
            content = StringEscapeUtils.unescapeJson(content);
        }
        content = content.replace("\uFEFF", "");
        content = content.replace("\r\n", "\n").replace("\r", "\n");
        content = content.replaceAll("[ \\t]+\n", "\n");
        content = content.replaceAll("(?m)^(#{1,6} .+)$\n(\\S)", "$1\n\n$2");
        content = content.replaceAll("(?m)^\\*\\s*$", "");
        content = content.replaceAll("(?m)^\\d+\\s*$", "");
        content = content.replaceAll("\n{3,}", "\n\n").trim();
        return content;
    }

    public static String stripCodeFence(String s) {
        String t = s == null ? "" : s.trim();
        if (t.startsWith("```")) {
            int i = t.indexOf('\n');
            if (i >= 0) t = t.substring(i + 1);
            if (t.endsWith("```")) t = t.substring(0, t.length() - 3);
        }
        return t;
    }
}
package ru.itmo.security;

import org.owasp.encoder.Encode;

public final class OutputEncoder {
    private OutputEncoder() {
    }

    public static String html(String value) {
        return value == null ? "" : Encode.forHtmlContent(value);
    }
}

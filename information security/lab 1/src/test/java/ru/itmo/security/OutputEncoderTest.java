package ru.itmo.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OutputEncoderTest {
    @Test
    void escapesUserControlledHtmlBeforeReturningIt() {
        assertEquals("&lt;script&gt;alert('x')&lt;/script&gt;",
                OutputEncoder.html("<script>alert('x')</script>"));
    }
}

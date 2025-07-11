package org.example.berichbe.global.api.status;

public interface ErrorCode {

    String getCode();
    String getMessage();

    default String formatMessage(final Object... args) {
        return String.format(getMessage(), args);
    }
}

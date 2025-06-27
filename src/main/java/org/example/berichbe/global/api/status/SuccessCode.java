package org.example.berichbe.global.api.status;

import org.springframework.http.HttpStatus;

public interface SuccessCode {

    HttpStatus getStatus();
    String getCode();
    String getMessage();
}

package com.github.conanchen.gedit.store.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Setter
@Getter
public class RException extends RuntimeException{
    private String code;
    private String details;

    public RException(String message, String code, String details) {
        super(String.format("code:[%s],details:[%s]",code,details));
        this.code = code;
        this.details = details;
    }
}

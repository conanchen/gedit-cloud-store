package com.github.conanchen.gedit.store.exception;

import lombok.Getter;
import lombok.Setter;
import io.grpc.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

/**
 * response result exception
 */
@Setter
@Getter
@Slf4j
public class RRException extends RuntimeException {
    private String code;
    private String details;

    public RRException(Status status) {
        super(String.format("code:[%s],details:[%s]", status.getCode(), status.getClass().getName().toLowerCase()));
        this.code = String.valueOf(status.getCode());
        this.details = status.getClass().getName().toLowerCase();
        log.info("code:{},details:{}",code,details);
    }

    public RRException(Status status,String message) {
        super(String.format("code:[%s],details:[%s]", status.getCode(), message));
        this.code = String.valueOf(status.getCode());
        this.details = message;
        log.info("code:{},details:{}",code,message);
    }
}
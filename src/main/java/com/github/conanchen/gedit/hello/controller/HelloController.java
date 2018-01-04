package com.github.conanchen.gedit.hello.controller;

import com.google.gson.Gson;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.DateFormat;
import java.util.Date;

@RestController
@EnableAutoConfiguration
public class HelloController {
    private final static Gson gson = new Gson();
    //
//    @Autowired
//    private WordRepository wordRepository;
    @Value("${gedit.docker.enabled}")
    Boolean insideDocker = false;

    @RequestMapping(value = "/hello")
    public String hello() {
        return
                String.format("hello@%s , HelloController Spring Boot insideDocker=%b",
                        DateFormat.getInstance().format(new Date()), insideDocker);
    }
}
package com.github.conanchen.gedit.store.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.text.DateFormat;
import java.util.List;
@Configuration
@ComponentScan("com.github.conanchen.gedit")
@EnableWebMvc
@Slf4j
public class JSONConfig extends WebMvcConfigurerAdapter  {
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        GsonHttpMessageConverter msgConverter = new GsonHttpMessageConverter();
        // use millisecond as a json Serializer
        Gson gson = new GsonBuilder().setDateFormat(DateFormat.MILLISECOND_FIELD).create();
        msgConverter.setGson(gson);
        converters.add(msgConverter);
        log.info("json config initial success");
    }
}

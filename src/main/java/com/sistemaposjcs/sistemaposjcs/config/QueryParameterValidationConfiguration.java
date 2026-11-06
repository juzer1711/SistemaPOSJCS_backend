package com.sistemaposjcs.sistemaposjcs.config;

import com.sistemaposjcs.sistemaposjcs.modules.autorizacion.interceptor.AutorizacionInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class QueryParameterValidationConfiguration implements WebMvcConfigurer {

    private final AutorizacionInterceptor autorizacionInterceptor;

    public QueryParameterValidationConfiguration(AutorizacionInterceptor autorizacionInterceptor) {
        this.autorizacionInterceptor = autorizacionInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new QueryParameterValidationInterceptor());
        registry.addInterceptor(autorizacionInterceptor);
    }
}

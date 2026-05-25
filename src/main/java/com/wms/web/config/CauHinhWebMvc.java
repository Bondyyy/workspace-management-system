package com.wms.web.config;

import com.wms.web.interceptor.ChanXacThuc;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CauHinhWebMvc implements WebMvcConfigurer {

    private final ChanXacThuc ChanXacThuc;

    public CauHinhWebMvc(ChanXacThuc ChanXacThuc) {
        this.ChanXacThuc = ChanXacThuc;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(ChanXacThuc)
                .addPathPatterns("/portal", "/portal/**", "/staff", "/staff/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/");
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/");
        registry.addResourceHandler("/icons/**")
                .addResourceLocations("classpath:/static/icons/");
        registry.addResourceHandler("/favicon.ico", "/favicon-16x16.png", "/favicon-32x32.png", "/apple-touch-icon.png")
                .addResourceLocations("classpath:/static/");
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/images/");
    }
}

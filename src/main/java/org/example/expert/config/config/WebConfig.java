package org.example.expert.config.config;

import lombok.RequiredArgsConstructor;
import org.example.expert.config.AuthUserArgumentResolver;
import org.example.expert.config.interceptor.AdminInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final AdminInterceptor adminInterceptor;

    // ArgumentResolver 등록
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new AuthUserArgumentResolver());
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry){
        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/admin/comments/*","/admin/users/*");
    }
}

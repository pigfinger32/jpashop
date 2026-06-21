package jpabook.jpashop;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // 업로드된 공문서를 /govdocs/** URL로 접근 가능하게 설정
        registry.addResourceHandler("/govdocs/**")
                .addResourceLocations("file:/uploads/");
    }
}

package org.tinycloud.mmwiki.config;

import java.nio.file.Path;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.tinycloud.mmwiki.web.AuthInterceptor;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;
    private final MmwikiProperties mmwikiProperties;

    public WebMvcConfig(AuthInterceptor authInterceptor, MmwikiProperties mmwikiProperties) {
        this.authInterceptor = authInterceptor;
        this.mmwikiProperties = mmwikiProperties;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
            .addPathPatterns("/**")
            .excludePathPatterns(
                "/author/**",
                "/install/**",
                "/static/**",
                "/images/**",
                "/error",
                "/favicon.ico",
                "/page/display"
            );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")
            .addResourceLocations("classpath:/static/");
        String imagesLocation = Path.of(mmwikiProperties.getDocumentRootDir()).toAbsolutePath().resolve("images").toUri().toString();
        registry.addResourceHandler("/images/**")
            .addResourceLocations(imagesLocation);
    }
}

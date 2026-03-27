package com.housekeeping.config;

import com.housekeeping.auth.support.AuthInterceptor;
import com.housekeeping.upload.config.FileStorageProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;
    private final FileStorageProperties fileStorageProperties;

    @Value("${app.cors.allowed-origins:http://localhost:5173}")
    private String[] allowedOrigins;

    public WebConfig(AuthInterceptor authInterceptor, FileStorageProperties fileStorageProperties) {
        this.authInterceptor = authInterceptor;
        this.fileStorageProperties = fileStorageProperties;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor).addPathPatterns("/api/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String publicUrlPrefix = trimTrailingSlash(fileStorageProperties.getPublicUrlPrefix());
        String fileLocation = Paths.get(fileStorageProperties.getUploadDir())
                .toAbsolutePath()
                .normalize()
                .toUri()
                .toString();
        registry.addResourceHandler(publicUrlPrefix + "/**")
                .addResourceLocations(fileLocation.endsWith("/") ? fileLocation : fileLocation + "/");
    }

    private String trimTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "/uploads";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}

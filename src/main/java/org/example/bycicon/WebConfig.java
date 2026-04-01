package org.example.bycicon;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/products/**")
                .addResourceLocations("file:///C:/Users/JAYBAD/Bicycon/Products/");
        registry.addResourceHandler("/profile/**")
                .addResourceLocations("file:///C:/Users/JAYBAD/Bicycon/Profile/");
    }

}
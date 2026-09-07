package org.example.omart;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final String userHome = System.getProperty("user.home");

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/products/**")
                .addResourceLocations("file:///" + userHome + "/OMart/Products/");
        registry.addResourceHandler("/profile/**")
                .addResourceLocations("file:///" + userHome + "/OMart/Profile/");
    }
}
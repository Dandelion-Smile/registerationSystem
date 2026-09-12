package org.iflytek.framework.config;

import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;
import org.iflytek.common.config.RuoYiConfig;
import org.iflytek.common.constant.Constants;
import org.iflytek.framework.interceptor.RepeatSubmitInterceptor;

/**
 * 通用配置
 * 
 * @author ruoyi
 */
@Configuration
public class ResourcesConfig implements WebMvcConfigurer
{
    @Autowired
    private RepeatSubmitInterceptor repeatSubmitInterceptor;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry)
    {
        registerLocalResourceHandler(registry, Constants.RESOURCE_PREFIX + "/**", resourcePath ->
                resourcePath != null && resourcePath.startsWith("avatar/"));
        registerLocalResourceHandler(registry, "/avatar/**", resourcePath -> true);

        /** swagger配置 */
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/springfox-swagger-ui/")
                .setCacheControl(CacheControl.maxAge(5, TimeUnit.HOURS).cachePublic());
    }

    /**
     * 自定义拦截规则
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry)
    {
        registry.addInterceptor(repeatSubmitInterceptor).addPathPatterns("/**");
    }

    private void registerLocalResourceHandler(ResourceHandlerRegistry registry, String pattern,
            Predicate<String> fallbackPredicate)
    {
        registry.addResourceHandler(pattern)
                .addResourceLocations("file:" + RuoYiConfig.getProfile() + "/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver()
                {
                    private final Resource defaultAvatar = new ClassPathResource("static/default-avatar.svg");

                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws java.io.IOException
                    {
                        Resource requested = location.createRelative(resourcePath);
                        if (requested.exists() && requested.isReadable())
                        {
                            return requested;
                        }
                        if (fallbackPredicate != null && fallbackPredicate.test(resourcePath) && defaultAvatar.exists())
                        {
                            return defaultAvatar;
                        }
                        return null;
                    }
                });
    }

    /**
     * 跨域配置
     */
    @Bean
    public CorsFilter corsFilter()
    {
        CorsConfiguration config = new CorsConfiguration();
        // 设置访问源地址
        config.addAllowedOriginPattern("*");
        // 设置访问源请求头
        config.addAllowedHeader("*");
        // 设置访问源请求方法
        config.addAllowedMethod("*");
        // 有效期 1800秒
        config.setMaxAge(1800L);
        // 添加映射路径，拦截一切请求
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        // 返回新的CorsFilter
        return new CorsFilter(source);
    }
}

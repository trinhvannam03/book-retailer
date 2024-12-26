package com.project.bookseller.configuration;


import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@Configuration
@EnableRedisHttpSession
@EnableCaching
public class SessionConfiguration {

//    @Bean
//    public CookieSerializer cookieSerializer() {
//        DefaultCookieSerializer cookieSerializer = new DefaultCookieSerializer();
//        cookieSerializer.setCookieName("SESSIONID"); // Set your session cookie name here
//        cookieSerializer.setCookiePath("/");
//        return cookieSerializer;
//    }

//    @Bean
//    public SessionRepositoryFilter<?> springSessionRepositoryFilter(RedisConnectionFactory connectionFactory) {
//        return new SessionRepositoryFilter<>(redisConnectionFactory());
//    }

}

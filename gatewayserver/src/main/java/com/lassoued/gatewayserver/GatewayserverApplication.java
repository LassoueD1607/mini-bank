package com.lassoued.gatewayserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;

import java.time.Duration;
import java.time.LocalDateTime;

@SpringBootApplication
public class GatewayserverApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayserverApplication.class, args);
    }
    //Redirect request of path /lassoued/accounts/** to /** for example
    //Now, we can access the same API from 2 API /lassoued/accounts/api/create === /accounts/api/create
    //We have to disable the default route from the application.yml (http://localhost:8072/actuator/gateway/routes)
    @Bean
    public RouteLocator lassouedBankRouterConfig(RouteLocatorBuilder routeLocatorBuilder){
        return routeLocatorBuilder.routes()
                .route(p->p
                        .path("/lassoued/accounts/**")
                        .filters(f-> f.rewritePath("/lassoued/accounts/(?<segment>.*)","/${segment}")
                                .addRequestHeader("X-Response-Time", LocalDateTime.now().toString())
                                .circuitBreaker(config -> config.setName("accountsCircuitBreaker")
                                        .setFallbackUri("forward:/contactSupport"))
                        )
                        .uri("lb://ACCOUNTS"))
                .route(p->p
                        .path("/lassoued/cards/**")
                        .filters(f-> f.rewritePath("/lassoued/cards/(?<segment>.*)","/${segment}")
                                .retry(retryConfig -> retryConfig.setRetries(3)
                                        .setMethods(HttpMethod.GET)
                                        .setBackoff(Duration.ofMillis(100),Duration.ofMillis(1000),2,true)
                                )
                        )
                        .uri("lb://CARDS"))
                .route(p->p
                        .path("/lassoued/loans/**")
                        .filters(f-> f.rewritePath("/lassoued/loans/(?<segment>.*)","/${segment}"))
                        .uri("lb://LOANS")

                ).build();
    }
}

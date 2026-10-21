package com.reservo.api.common.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI reservationOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Reservation API")
                        .description("RESTful API for booking management, establishments, and reservations.")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Reservo Team")
                                .email("support@reservo.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}

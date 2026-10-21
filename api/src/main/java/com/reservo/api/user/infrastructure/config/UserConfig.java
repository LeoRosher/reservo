package com.reservo.api.user.infrastructure.config;

import com.reservo.api.user.domain.port.PasswordEncoder;
import com.reservo.api.user.domain.port.TokenService;
import com.reservo.api.user.domain.port.UserRepository;
import com.reservo.api.user.usecase.GetUserByEmailUseCase;
import com.reservo.api.user.usecase.GetUserByIdUseCase;
import com.reservo.api.user.usecase.LoginUserUseCase;
import com.reservo.api.user.usecase.RegisterUserUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserConfig {

    @Bean
    public RegisterUserUseCase registerUserUseCase(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        return new RegisterUserUseCase(userRepository, passwordEncoder);
    }

    @Bean
    public LoginUserUseCase loginUserUseCase(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            TokenService tokenService
    ) {
        return new LoginUserUseCase(userRepository, passwordEncoder, tokenService);
    }

    @Bean
    public GetUserByIdUseCase getUserByIdUseCase(UserRepository userRepository) {
        return new GetUserByIdUseCase(userRepository);
    }

    @Bean
    public GetUserByEmailUseCase getUserByEmailUseCase(UserRepository userRepository) {
        return new GetUserByEmailUseCase(userRepository);
    }
}

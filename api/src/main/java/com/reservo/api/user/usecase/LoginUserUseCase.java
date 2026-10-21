package com.reservo.api.user.usecase;

import com.reservo.api.user.domain.exception.InvalidCredentialsException;
import com.reservo.api.user.domain.model.User;
import com.reservo.api.user.domain.port.PasswordEncoder;
import com.reservo.api.user.domain.port.TokenService;
import com.reservo.api.user.domain.port.UserRepository;

public class LoginUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public LoginUserUseCase(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            TokenService tokenService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    public record Command(
            String email,
            String password
    ) {}

    public record Result(
            String token,
            User user
    ) {}

    public Result execute(Command command) {
        User user = userRepository.findByEmail(command.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(command.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String token = tokenService.generateToken(user);
        return new Result(token, user);
    }
}

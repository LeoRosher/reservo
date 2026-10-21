package com.reservo.api.user.usecase;

import com.reservo.api.user.domain.exception.UserAlreadyExistsException;
import com.reservo.api.user.domain.model.User;
import com.reservo.api.user.domain.model.UserRole;
import com.reservo.api.user.domain.port.PasswordEncoder;
import com.reservo.api.user.domain.port.UserRepository;

public class RegisterUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterUserUseCase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public record Command(
            String name,
            String email,
            String password,
            String phone,
            UserRole role
    ) {}

    public User execute(Command command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new UserAlreadyExistsException(command.email());
        }

        String passwordHash = passwordEncoder.encode(command.password());

        User newUser = User.createNew(
                command.name(),
                command.email(),
                passwordHash,
                command.phone(),
                command.role()
        );

        return userRepository.save(newUser);
    }
}

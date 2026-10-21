package com.reservo.api.establishment.infrastructure.config;

import com.reservo.api.establishment.domain.port.EstablishmentRepository;
import com.reservo.api.establishment.usecase.CreateEstablishmentUseCase;
import com.reservo.api.establishment.usecase.DeleteEstablishmentUseCase;
import com.reservo.api.establishment.usecase.GetEstablishmentByIdUseCase;
import com.reservo.api.establishment.usecase.ListEstablishmentsUseCase;
import com.reservo.api.establishment.usecase.UpdateEstablishmentUseCase;
import com.reservo.api.user.domain.port.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EstablishmentConfig {

    @Bean
    public CreateEstablishmentUseCase createEstablishmentUseCase(
            EstablishmentRepository establishmentRepository,
            UserRepository userRepository
    ) {
        return new CreateEstablishmentUseCase(establishmentRepository, userRepository);
    }

    @Bean
    public GetEstablishmentByIdUseCase getEstablishmentByIdUseCase(EstablishmentRepository establishmentRepository) {
        return new GetEstablishmentByIdUseCase(establishmentRepository);
    }

    @Bean
    public ListEstablishmentsUseCase listEstablishmentsUseCase(EstablishmentRepository establishmentRepository) {
        return new ListEstablishmentsUseCase(establishmentRepository);
    }

    @Bean
    public UpdateEstablishmentUseCase updateEstablishmentUseCase(
            EstablishmentRepository establishmentRepository,
            UserRepository userRepository
    ) {
        return new UpdateEstablishmentUseCase(establishmentRepository, userRepository);
    }

    @Bean
    public DeleteEstablishmentUseCase deleteEstablishmentUseCase(
            EstablishmentRepository establishmentRepository,
            UserRepository userRepository
    ) {
        return new DeleteEstablishmentUseCase(establishmentRepository, userRepository);
    }
}

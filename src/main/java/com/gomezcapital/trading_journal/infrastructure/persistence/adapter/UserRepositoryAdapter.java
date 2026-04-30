package com.gomezcapital.trading_journal.infrastructure.persistence.adapter;

import com.gomezcapital.trading_journal.domain.model.User;
import com.gomezcapital.trading_journal.domain.ports.UserRepositoryPort;
import com.gomezcapital.trading_journal.infrastructure.persistence.entity.UserEntity;
import com.gomezcapital.trading_journal.infrastructure.persistence.repository.JpaUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID; // <-- ¡ESTE ES EL IMPORT QUE FALTABA!

@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final JpaUserRepository jpaUserRepository;

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaUserRepository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpaUserRepository.findById(id).map(this::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaUserRepository.existsByEmail(email);
    }

    @Override
    public User save(User user) {
        UserEntity entity = UserEntity.builder()
                .id(user.id())
                .email(user.email())
                .passwordHash(user.passwordHash())
                .alias(user.alias())
                .role(user.role() != null ? user.role() : "ROLE_FREE") // <-- ASIGNAMOS EL ROL
                .build();
        return toDomain(jpaUserRepository.save(entity));
    }

    private User toDomain(UserEntity entity) {
        return new User(entity.getId(), entity.getEmail(), entity.getPasswordHash(), entity.getAlias(), entity.getRole()); // <-- RECUPERAMOS EL ROL
    }
}
package com.gomezcapital.trading_journal.infrastructure.persistence.adapter;

import com.gomezcapital.trading_journal.domain.model.User;
import com.gomezcapital.trading_journal.domain.ports.UserRepositoryPort;
import com.gomezcapital.trading_journal.infrastructure.persistence.entity.UserEntity;
import com.gomezcapital.trading_journal.infrastructure.persistence.repository.JpaUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final JpaUserRepository jpaUserRepository;

    @Override
    public User save(User user) {
        UserEntity entity = new UserEntity(
                user.id(),
                user.email(),
                user.passwordHash(),
                user.alias()
        );
        UserEntity savedEntity = jpaUserRepository.save(entity);
        return toDomain(savedEntity);
 
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaUserRepository.findByEmail(email)
                .map(this::toDomain);
    
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaUserRepository.existsByEmail(email);
    
    }

    // Metodo para comvertir de entidad a modelo de dominio

    private User toDomain(UserEntity entity) {
        return new User(
            entity.getId(),
            entity.getEmail(),
            entity.getPasswordHash(),
            entity.getAlias()
        );
    }
    
}

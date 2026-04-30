package com.gomezcapital.trading_journal.domain.ports;

import com.gomezcapital.trading_journal.domain.model.User;
import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryPort {
    Optional<User> findByEmail(String email);
    Optional<User> findById(UUID id); // <-- NUEVA FUNCIÓN
    boolean existsByEmail(String email);
    User save(User user);
}
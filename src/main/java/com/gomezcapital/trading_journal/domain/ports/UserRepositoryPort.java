package com.gomezcapital.trading_journal.domain.ports;

import com.gomezcapital.trading_journal.domain.model.User;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface UserRepositoryPort {
    Optional<User> findByEmail(String email);
    Optional<User> findById(UUID id);
    boolean existsByEmail(String email);
    User save(User user);
    
    // <-- NUEVO MÉTODO AÑADIDO -->
    List<User> findAll();
}
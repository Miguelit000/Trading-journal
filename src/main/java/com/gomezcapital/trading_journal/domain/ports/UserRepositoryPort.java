package com.gomezcapital.trading_journal.domain.ports;

import com.gomezcapital.trading_journal.domain.model.User;
import java.util.Optional;

public interface UserRepositoryPort {
    
    User save(User user);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}


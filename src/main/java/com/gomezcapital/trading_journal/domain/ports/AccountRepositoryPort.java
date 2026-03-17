package com.gomezcapital.trading_journal.domain.ports;

import com.gomezcapital.trading_journal.domain.model.Account; 
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepositoryPort {

    Account save(Account account);

    Optional<Account> findById(UUID id);

    List<Account> findByUserId(UUID userId);

    void deleteById(UUID id);

}
package com.gomezcapital.trading_journal.infrastructure.persistence.adapter;

import com.gomezcapital.trading_journal.domain.model.Account;
import com.gomezcapital.trading_journal.domain.ports.AccountRepositoryPort;
import com.gomezcapital.trading_journal.infrastructure.persistence.entity.AccountEntity;
import com.gomezcapital.trading_journal.infrastructure.persistence.repository.AccountJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component 
@RequiredArgsConstructor
public class AccountRepositoryAdapter implements AccountRepositoryPort {

    private final AccountJpaRepository accountJpaRepository;

    @Override
    public Account save(Account account) {
        AccountEntity entity = toEntity(account);
        AccountEntity savedEntity = accountJpaRepository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public Optional<Account> findById(UUID id) {
        return accountJpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Account> findByUserId(UUID userId) {
        return accountJpaRepository.findByUserId(userId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void deleteById(UUID id) {
        accountJpaRepository.deleteById(id);
    }

    // ==========================================
    // MÉTODOS PRIVADOS DE TRADUCCIÓN (MAPPERS)
    // ==========================================

    private AccountEntity toEntity(Account account) {
        if (account == null) return null;
        
        return AccountEntity.builder()
                .id(account.id())
                .userId(account.userId())
                .name(account.name())
                .initialBalance(account.initialBalance())
                .currentBalance(account.currentBalance())
                .currency(account.currency())
                .createdAt(account.createdAt())
                .build();
    }

    private Account toDomain(AccountEntity entity) {
        if (entity == null) return null;

        return new Account(
                entity.getId(),
                entity.getUserId(),
                entity.getName(),
                entity.getInitialBalance(),
                entity.getCurrentBalance(),
                entity.getCurrency(),
                entity.getCreatedAt()
        );
    }
}
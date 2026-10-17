package com.muller_tomas.reading_groups.repository;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.muller_tomas.reading_groups.model.RefreshSession;

public interface RefreshSessionRepository extends JpaRepository<RefreshSession, Long> {

    Optional<RefreshSession> findByTokenHash(String tokenHash);

    @Transactional
    long deleteByExpiresAtBefore(Instant cutoff);
}

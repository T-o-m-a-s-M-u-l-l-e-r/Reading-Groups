package com.muller_tomas.reading_groups.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.muller_tomas.reading_groups.model.EmailVerification;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Integer> {
	@Query("select v.userId from EmailVerification v where v.tokenHash = :tokenHash")
	public Optional<Integer> findUserIdByTokenHash(@Param("tokenHash") String tokenHash);
}

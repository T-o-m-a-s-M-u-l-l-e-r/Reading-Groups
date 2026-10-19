package com.muller_tomas.reading_groups.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import com.muller_tomas.reading_groups.model.User;

public interface UserRepository extends JpaRepository<User, Integer> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select u from User u where u.id = :userId")
	public Optional<User> findByIdForUpdate(@Param("userId") int userId);

	Optional<User> findByUsername(String username);
	
	Optional<User> findByEmail(String email);
	
}

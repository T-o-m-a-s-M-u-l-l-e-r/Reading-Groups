package com.muller_tomas.reading_groups.repository;

import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import com.muller_tomas.reading_groups.model.Group;

public interface GroupRepository extends JpaRepository<Group, Integer> {
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select g from Group g where g.id = :groupId")
	public Optional<Group> findByIdForUpdate(@Param("groupId") int groupId);

	
	Set<Group> findByUsersId(int userId);
	
}
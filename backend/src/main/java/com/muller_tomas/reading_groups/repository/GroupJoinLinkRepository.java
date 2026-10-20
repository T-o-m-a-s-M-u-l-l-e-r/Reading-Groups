package com.muller_tomas.reading_groups.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.muller_tomas.reading_groups.model.GroupJoinLink;

public interface GroupJoinLinkRepository extends JpaRepository<GroupJoinLink, Integer> {
	@Query("select link.groupId from GroupJoinLink link where link.tokenHash = :tokenHash")
	public Optional<Integer> findGroupIdByTokenHash(@Param("tokenHash") String tokenHash);
}

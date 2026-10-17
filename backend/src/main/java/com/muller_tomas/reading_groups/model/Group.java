package com.muller_tomas.reading_groups.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "Groups")
public class Group {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "group_id")
	private int id;

	@Column(name = "reading_text_path", columnDefinition = "TEXT", nullable = false)
	private String readingTextPath;

	@Column(name = "group_name", length = 100, nullable = false)
	private String groupName;
	
	@Column(name = "created_at", length = 100, nullable = false)
	@CreationTimestamp
	private LocalDateTime createdAt;
	
	@ManyToOne
	@JoinColumn(name = "administrator_user_id", referencedColumnName = "user_id", nullable = false, updatable = false)
	private User administratorUser;
	
	@ManyToMany
    @JoinTable(
        name = "Users_Groups",
        joinColumns = @JoinColumn(name = "group_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
	private Set<User> users = new HashSet<>();
	
	public Group(String readingTextPath, User administratorUser, String groupName, HashSet<User> users) {
		super();
		this.readingTextPath = readingTextPath;
		this.administratorUser = administratorUser;
		this.groupName = groupName;
		this.users = users;
	}
	
	public Group() {
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getReadingTextPath() {
		return readingTextPath;
	}

	public void setReadingTextPath(String readingTextPath) {
		this.readingTextPath = readingTextPath;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public User getAdministratorUser() {
		return administratorUser;
	}

	public void setAdministratorUser(User administratorUser) {
		this.administratorUser = administratorUser;
	}

	public Set<User> getUsers() {
		return users;
	}

	public void setUsers(Set<User> users) {
		this.users = users;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

}

package com.prabhakar.auth.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "permissions")
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;   // e.g., USER_READ, USER_WRITE

	public Permission(Long id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	public Permission() {
		super();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
    
    
}

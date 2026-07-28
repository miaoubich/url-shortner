package org.miaoubich.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "short_urls", 
	   indexes = { 
			   @Index(
					   name = "idx_short_code", 
					   columnList = "shortCode", 
					   unique = true
					   ) 
			   }
)
public class ShortUrl {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 12)
	private String shortCode;

	@Column(nullable = false, length = 2048)
	private String longUrl;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	private Instant expiresAt;

	@Column(nullable = false)
	private boolean active;

	protected ShortUrl() {
		// JPA requires a no-arg constructor
	}

	public ShortUrl(String shortCode, String longUrl) {
		this.shortCode = shortCode;
		this.longUrl = longUrl;
	}

	@PrePersist
	void prePersist() {
		this.createdAt = Instant.now();
		this.active = true;
	}

	public Long getId() {
		return id;
	}

	public String getShortCode() {
		return shortCode;
	}

	public String getLongUrl() {
		return longUrl;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}

	public void setExpiresAt(Instant expiresAt) {
		this.expiresAt = expiresAt;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
}
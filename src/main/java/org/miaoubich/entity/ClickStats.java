package org.miaoubich.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Table(name = "click_stats")
@Entity
public class ClickStats {

	@Id
	@Column(length = 12)
	private String shortCode;
	
	@Column(nullable = false)
	private long totalClicks;
	
	private Instant lastClickedAt;
	
	protected ClickStats() {
		// required by JPA
	}

	public ClickStats(String shortCode, long totalClicks, Instant lastClickAt) {
		super();
		this.shortCode = shortCode;
		this.totalClicks = totalClicks;
		this.lastClickedAt = lastClickAt;
	}

	public String getShortCode() {
		return shortCode;
	}

	public long getTotalClicks() {
		return totalClicks;
	}

	public void setTotalClicks(long totalClicks) {
		this.totalClicks = totalClicks;
	}

	public Instant getLastClickedAt() {
		return lastClickedAt;
	}

	public void setLastClickAt(Instant lastClickedAt) {
		this.lastClickedAt = lastClickedAt;
	}
	
}

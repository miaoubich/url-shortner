package org.miaoubich.repository;

import java.time.Instant;

import org.miaoubich.entity.ClickStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ClickStatsRepository extends JpaRepository<ClickStats, String> {

	@Modifying
	@Transactional
	@Query(value = """
			insert into click_stats (short_code, total_clicks, last_clicked_at)
            values (:shortCode, 1, :clickedAt)
            on conflict (short_code)
            do update set total_clicks = click_stats.total_clicks + 1,
                          last_clicked_at = :clickedAt
		    """, nativeQuery = true)
	void incrementClicks(@Param("shortCode") String shortCode, @Param("clickedAt") Instant clickedAt);
}

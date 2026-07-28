package org.miaoubich.repository;

import java.util.List;
import java.util.Optional;

import org.miaoubich.entity.ShortUrl;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {

	Optional<ShortUrl> findByShortCodeAndActiveTrue(String shortCode);
	
	boolean existByShortCode(String shortCode);
	
	/**
     * Keyset ("seek") pagination instead of OFFSET/LIMIT.
     * Pass the last-seen id as the cursor; on the first page pass Long.MAX_VALUE.
     * This stays O(page size) regardless of how deep you page, unlike OFFSET
     * which forces the DB to scan and discard every prior row.
     */
	@Query("select u from ShortUrl u where u.id < :cursor order by u.id desc")
	List<ShortUrl> findPageByCursor(@Param("cursor") Long cursor, Limit limit);
}

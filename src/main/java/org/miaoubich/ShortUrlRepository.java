package org.miaoubich;

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
	
	@Query("select u form ShortUrl u where u.id < :cursor order by u.id desc")
	List<ShortUrl> findPageByCursor(@Param("cursor") Long cursor, Limit limit);
}

package org.miaoubich.dto;

import java.time.Instant;

public record ClickEvent (

		String shortCode,
		Instant clickedAt
		) {

}

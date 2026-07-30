package org.miaoubich.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateUrlStatusRequest (
		@NotNull
		Boolean active
		) {

}

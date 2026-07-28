package org.miaoubich.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateUrlRequest (
		
		@NotBlank
        @Pattern(regexp = "^https?://.+", message = "must be a valid http(s) URL")
        String longUrl,

        String customCode) {

}

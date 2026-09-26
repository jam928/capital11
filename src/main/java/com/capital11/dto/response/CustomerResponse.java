package com.capital11.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record CustomerResponse(int cid, String name, String email, String username,
                               @Schema(description = "M/D/YYYY") String birthday, String gender) {
}

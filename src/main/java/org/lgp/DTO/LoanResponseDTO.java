package org.lgp.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public record LoanResponseDTO(
        String id,
        String userId,
        String userEmail,
        String inventoryItemId,
        String boardgameId,
        String boardgameTitle,
        String boardgameImageUrl,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        Date borrowedAt,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        Date dueAt,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        Date returnedAt,

        Boolean active
) {
}

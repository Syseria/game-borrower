package org.lgp.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.URL;

public record BoardgameRequestDTO(
        @NotBlank(message = "Title is required")
        String title,

        @NotBlank(message = "Publisher is required")
        String publisher,

        @NotNull(message = "Min players is required")
        @Min(value = 1, message = "Min players must be at least 1")
        Integer minPlayers,

        @NotNull(message = "Max players is required")
        @Min(value = 1, message = "Max players must be at least 1")
        Integer maxPlayers,

        @NotNull(message = "Min age is required")
        @Min(value = 0, message = "Min age cannot be negative")
        Integer minAge,

        @NotNull(message = "Min time is required")
        @Min(value = 1, message = "Min time must be at least 1 minute")
        Integer minTime,

        @NotNull(message = "Max time is required")
        Integer maxTime,

        @NotBlank(message = "Description is required")
        String description,

        @URL(message = "Image URL must be a valid URL")
        String imageUrl,

        @URL(message = "Video URL must be a valid URL")
        String videoUrl
) {
}

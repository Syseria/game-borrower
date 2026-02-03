package org.lgp.DTO;

public record BoardgameResponseDTO(
        String id,
        String title,
        String publisher,
        Integer minPlayers,
        Integer maxPlayers,
        Integer minAge,
        Integer minTime,
        Integer maxTime,
        String description,
        String imageUrl,
        String videoUrl
) {
}

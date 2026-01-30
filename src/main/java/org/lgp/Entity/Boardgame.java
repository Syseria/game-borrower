package org.lgp.Entity;

import com.google.cloud.firestore.annotation.DocumentId;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.URL;

@RegisterForReflection
public class Boardgame {

    @DocumentId
    private String id;
    private String title;
    private String publisher;
    private Integer minPlayers;
    private Integer maxPlayers;
    private Integer minAge;
    private Integer minTime;
    private Integer maxTime;
    private String imageUrl;
    private String description;

    // YouTube video explaining the rules/game
    private String videoUrl;

    // =========================================================================
    // CONSTRUCTORS
    // =========================================================================

    public Boardgame() {
    }

    // =========================================================================
    // STANDARD GETTERS/SETTERS
    // =========================================================================

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }

    public Integer getMinPlayers() { return minPlayers; }
    public void setMinPlayers(Integer minPlayers) { this.minPlayers = minPlayers; }

    public Integer getMaxPlayers() { return maxPlayers; }
    public void setMaxPlayers(Integer maxPlayers) { this.maxPlayers = maxPlayers; }

    public Integer getMinAge() { return minAge; }
    public void setMinAge(Integer minAge) { this.minAge = minAge; }

    public Integer getMinTime() { return minTime; }
    public void setMinTime(Integer minTime) { this.minTime = minTime; }

    public Integer getMaxTime() { return maxTime; }
    public void setMaxTime(Integer maxTime) { this.maxTime = maxTime; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    // =========================================================================
    // DTOs
    // =========================================================================

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
    ) {}

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
    ) {}

    // =========================================================================
    // OVERRIDES
    // =========================================================================

    @Override
    public String toString() {
        return "Game{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", publisher='" + publisher + '\'' +
                ", minPlayers=" + minPlayers +
                ", maxPlayers=" + maxPlayers +
                ", minAge=" + minAge +
                ", minTime=" + minTime +
                ", maxTime=" + maxTime +
                ", imageUrl='" + imageUrl + '\'' +
                ", videoUrl='" + videoUrl + '\'' +
                '}';
    }
}
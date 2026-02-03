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

    public record BoardgameSearchCriteria(
            String id,
            String title,
            String publisher,
            Integer minPlayers,
            Integer maxPlayers,
            Integer minAge,
            Integer minTime,
            Boolean hasVideo,
            // Pagination Fields
            String sortField,
            String sortDir,
            Integer pageSize,
            String firstId,
            String lastId,
            boolean isPrevious
    ) {
        // Static builder to avoid long null lists in code
        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private String id;
            private String title;
            private String publisher;
            private Integer minPlayers;
            private Integer maxPlayers;
            private Integer minAge;
            private Integer minTime;
            private Boolean hasVideo;

            // Pagination Fields
            private String sortField = "title";
            private String sortDir = "asc";
            private Integer pageSize = 20;
            private String firstId;
            private String lastId;
            private boolean isPrevious = false;

            public Builder id(String id) { this.id = id; return this; }
            public Builder title(String title) { this.title = title; return this; }
            public Builder publisher(String publisher) { this.publisher = publisher; return this; }
            public Builder minPlayers(Integer min) { this.minPlayers = min; return this; }
            public Builder maxPlayers(Integer max) { this.maxPlayers = max; return this; }
            public Builder minAge(Integer age) { this.minAge = age; return this; }
            public Builder minTime(Integer time) { this.minTime = time; return this; }
            public Builder hasVideo(Boolean has) { this.hasVideo = has; return this; }

            // Pagination
            public Builder sortField(String field) { this.sortField = field; return this; }
            public Builder sortDir(String direction) { this.sortDir = direction; return this; }
            public Builder pageSize(Integer limit) { this.pageSize = limit; return this; }
            public Builder firstId(String first) { this.firstId = first; return this; }
            public Builder lastId(String last) { this.lastId = last; return this; }
            public Builder isPrevious(Boolean prev) { this.isPrevious = prev; return this; }

            public BoardgameSearchCriteria build() {
                return new BoardgameSearchCriteria(id, title, publisher, minPlayers, maxPlayers, minAge, minTime, hasVideo, sortField, sortDir, pageSize, firstId, lastId, isPrevious);
            }
        }
    }

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
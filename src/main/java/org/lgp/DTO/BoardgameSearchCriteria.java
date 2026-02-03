package org.lgp.DTO;

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
    public static Builder builder() {
        return new Builder();
    }

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

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder publisher(String publisher) {
            this.publisher = publisher;
            return this;
        }

        public Builder minPlayers(Integer min) {
            this.minPlayers = min;
            return this;
        }

        public Builder maxPlayers(Integer max) {
            this.maxPlayers = max;
            return this;
        }

        public Builder minAge(Integer age) {
            this.minAge = age;
            return this;
        }

        public Builder minTime(Integer time) {
            this.minTime = time;
            return this;
        }

        public Builder hasVideo(Boolean has) {
            this.hasVideo = has;
            return this;
        }

        // Pagination
        public Builder sortField(String field) {
            this.sortField = field;
            return this;
        }

        public Builder sortDir(String direction) {
            this.sortDir = direction;
            return this;
        }

        public Builder pageSize(Integer limit) {
            this.pageSize = limit;
            return this;
        }

        public Builder firstId(String first) {
            this.firstId = first;
            return this;
        }

        public Builder lastId(String last) {
            this.lastId = last;
            return this;
        }

        public Builder isPrevious(Boolean prev) {
            this.isPrevious = prev;
            return this;
        }

        public BoardgameSearchCriteria build() {
            return new BoardgameSearchCriteria(id, title, publisher, minPlayers, maxPlayers, minAge, minTime, hasVideo, sortField, sortDir, pageSize, firstId, lastId, isPrevious);
        }
    }
}

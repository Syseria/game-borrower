package org.lgp.DTO;

import org.lgp.Entity.InventoryItem;

public record InventorySearchCriteria(
        String id,
        String gameId,
        InventoryItem.Status status,
        InventoryItem.Condition condition,

        // Pagination Fields
        String sortField,
        String sortDir,
        Integer pageSize,
        String firstId,
        String lastId,
        boolean isPrevious
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String gameId;
        private InventoryItem.Status status;
        private InventoryItem.Condition condition;

        // Pagination Fields
        private String sortField = "status";
        private String sortDir = "asc";
        private Integer pageSize = 20;
        private String firstId;
        private String lastId;
        private boolean isPrevious = false;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder gameId(String gameId) {
            this.gameId = gameId;
            return this;
        }

        public Builder status(InventoryItem.Status status) {
            this.status = status;
            return this;
        }

        public Builder condition(InventoryItem.Condition condition) {
            this.condition = condition;
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

        public InventorySearchCriteria build() {
            return new InventorySearchCriteria(id, gameId, status, condition, sortField, sortDir, pageSize, firstId, lastId, isPrevious);
        }
    }
}

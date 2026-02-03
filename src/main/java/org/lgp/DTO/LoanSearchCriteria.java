package org.lgp.DTO;

import org.lgp.Entity.Loan;

import java.util.Date;

public record LoanSearchCriteria(
        String userId,
        String gameId,
        String inventoryItemId,
        String title,
        Boolean activeOnly,
        Date borrowedAt,
        Date dueAt,
        Date returnedAt,

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
        private String userId;
        private String gameId;
        private String inventoryItemId;
        private String title;
        private Boolean activeOnly;
        private Date borrowedAt;
        private Date dueAt;
        private Date returnedAt;

        // Pagination Fields
        private String sortField = "dueAt";
        private String sortDir = "asc";
        private Integer pageSize = 20;
        private String firstId;
        private String lastId;
        private boolean isPrevious = false;

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder gameId(String gameId) {
            this.gameId = gameId;
            return this;
        }

        public Builder inventoryItemId(String id) {
            this.inventoryItemId = id;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder activeOnly(Boolean active) {
            this.activeOnly = active;
            return this;
        }

        public Builder borrowedAt(Date date) {
            this.borrowedAt = date;
            return this;
        }

        public Builder dueAt(Date date) {
            this.dueAt = date;
            return this;
        }

        public Builder returnedAt(Date date) {
            this.returnedAt = date;
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

        public LoanSearchCriteria build() {
            return new LoanSearchCriteria(userId, gameId, inventoryItemId, title, activeOnly, borrowedAt, dueAt, returnedAt, sortField, sortDir, pageSize, firstId, lastId, isPrevious);
        }
    }
}

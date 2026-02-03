package org.lgp.DTO;

import org.lgp.Entity.User;

public record UserSearchCriteria(
        String id,
        String name,
        String lname,
        String email,
        User.Role role,

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
        private String name;
        private String lname;
        private String email;
        private User.Role role;

        // Pagination Fields
        private String sortField = "lname";
        private String sortDir = "asc";
        private Integer pageSize = 20;
        private String firstId;
        private String lastId;
        private boolean isPrevious = false;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder lname(String lname) {
            this.lname = lname;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder role(User.Role role) {
            this.role = role;
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

        public UserSearchCriteria build() {
            return new UserSearchCriteria(id, name, lname, email, role, sortField, sortDir, pageSize, firstId, lastId, isPrevious);
        }
    }
}

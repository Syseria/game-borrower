package org.lgp.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.firestore.annotation.Exclude;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotNull;
import org.lgp.Validation.InventoryItemExists;
import org.lgp.Validation.LoanDuration;
import org.lgp.Validation.UserExists;

import java.util.Date;

@RegisterForReflection
public class Loan {

    @DocumentId
    private String id;
    private String userId;
    private String userEmail;

    @NotNull
    private String inventoryItemId;
    private String boardgameId;
    private String boardgameTitle;
    private String boardgameImageUrl;
    private Timestamp borrowedAt;
    private Timestamp dueAt;
    private Timestamp returnedAt;
    private Boolean active;

    // =========================================================================
    // CONSTRUCTORS
    // =========================================================================

    public Loan() {
        this.active = true;
        this.borrowedAt = Timestamp.now();
    }

    // =========================================================================
    // LOGIC & HELPERS
    // =========================================================================

    @Exclude
    @JsonIgnore
    public Date toDate(Timestamp ts) {
        return ts == null ? null : ts.toDate();
    }

    // =========================================================================
    // STANDARD GETTERS / SETTERS
    // =========================================================================

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public String getInventoryItemId() { return inventoryItemId; }
    public void setInventoryItemId(String inventoryItemId) { this.inventoryItemId = inventoryItemId; }
    public String getBoardgameId() { return boardgameId; }
    public void setBoardgameId(String boardgameId) { this.boardgameId = boardgameId; }
    public String getBoardgameTitle() { return boardgameTitle; }
    public void setBoardgameTitle(String boardgameTitle) { this.boardgameTitle = boardgameTitle; }
    public String getBoardgameImageUrl() { return boardgameImageUrl; }
    public void setBoardgameImageUrl(String boardgameImageUrl) { this.boardgameImageUrl = boardgameImageUrl; }
    public Timestamp getBorrowedAt() { return borrowedAt; }
    public void setBorrowedAt(Timestamp borrowedAt) { this.borrowedAt = borrowedAt; }
    public Timestamp getDueAt() { return dueAt; }
    public void setDueAt(Timestamp dueAt) { this.dueAt = dueAt; }
    public Timestamp getReturnedAt() { return returnedAt; }
    public void setReturnedAt(Timestamp returnedAt) { this.returnedAt = returnedAt; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    // =========================================================================
    // DTOs
    // =========================================================================

    @LoanDuration
    public record CreateLoanRequestDTO(
            @NotNull(message = "Inventory Item ID is required")
            @InventoryItemExists
            String inventoryItemId,

            @NotNull(message = "User ID is required")
            @UserExists
            String userId,

            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
            Date dueDate
    ) {}

    public record ReturnLoanRequestDTO(
            String condition
    ) {}

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
    ) {}

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
        public static Builder builder() { return new Builder(); }

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

            public Builder userId(String userId) { this.userId = userId; return this; }
            public Builder gameId(String gameId) { this.gameId = gameId; return this; }
            public Builder inventoryItemId(String id) { this.inventoryItemId = id; return this; }
            public Builder title(String title) { this.title = title; return this; }
            public Builder activeOnly(Boolean active) { this.activeOnly = active; return this; }
            public Builder borrowedAt(Date date) { this.borrowedAt = date; return this; }
            public Builder dueAt(Date date) { this.dueAt = date; return this; }
            public Builder returnedAt(Date date) { this.returnedAt = date; return this; }

            // Pagination
            public Builder sortField(String field) { this.sortField = field; return this; }
            public Builder sortDir(String direction) { this.sortDir = direction; return this; }
            public Builder pageSize(Integer limit) { this.pageSize = limit; return this; }
            public Builder firstId(String first) { this.firstId = first; return this; }
            public Builder lastId(String last) { this.lastId = last; return this; }
            public Builder isPrevious(Boolean prev) { this.isPrevious = prev; return this; }

            public LoanSearchCriteria build() {
                return new LoanSearchCriteria(userId, gameId, inventoryItemId, title, activeOnly, borrowedAt, dueAt, returnedAt, sortField, sortDir, pageSize, firstId, lastId, isPrevious);
            }
        }
    }
}
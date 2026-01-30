package org.lgp.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.firestore.annotation.Exclude;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotNull;
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

    public record CreateLoanRequestDTO(
            @NotNull(message = "Inventory Item ID is required")
            String inventoryItemId,

            @NotNull(message = "User ID is required")
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
}
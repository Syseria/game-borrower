package org.lgp.Entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.firestore.annotation.Exclude;
import com.google.cloud.firestore.annotation.PropertyName;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.lgp.Validation.BoardgameExists;
import org.lgp.Validation.ValidEnum;

@RegisterForReflection
public class InventoryItem {

    @DocumentId
    private String id;

    @Size(max = 1024, message = "Details can't exceed 1024 characters")
    private String details;
    private String currentLoanId;

    @NotBlank(message = "Boardgame ID is required")
    private String boardgameId;
    private String boardgameTitle;

    private Status status;
    private Condition condition;


    // =========================================================================
    // CONSTRUCTORS
    // =========================================================================

    public InventoryItem() {
        this.status = Status.AVAILABLE;
    }

    // =========================================================================
    // ENUMS
    // =========================================================================

    @RegisterForReflection
    public enum Status {
        AVAILABLE("available"),
        BORROWED("borrowed"),
        MAINTENANCE("maintenance"),
        RETURNED("returned"),
        LOST("lost");

        private final String value;
        Status(String value) { this.value = value; }

        @JsonValue
        public String getValue() { return value; }

        @JsonCreator
        public static Status fromString(String text) {
            for (Status s : Status.values()) {
                if (s.value.equalsIgnoreCase(text)) return s;
            }
            return null;
        }
    }

    @RegisterForReflection
    public enum Condition {
        NEW("new"),
        GOOD("good"),
        WORN("worn"),
        DAMAGED("damaged"),
        INCOMPLETE("incomplete");

        private final String value;
        Condition(String value) { this.value = value; }

        @JsonValue
        public String getValue() { return value; }

        @JsonCreator
        public static Condition fromString(String text) {
            for (Condition c : Condition.values()) {
                if (c.value.equalsIgnoreCase(text)) return c;
            }
            return null;
        }
    }

    // =========================================================================
    // LOGIC & FIRESTORE SHADOW ACCESSORS
    // =========================================================================

    @Exclude
    public Status getStatus() { return status; }
    @Exclude
    public void setStatus(Status status) { this.status = status; }

    @Exclude
    public Condition getCondition() { return condition; }
    @Exclude
    public void setCondition(Condition condition) { this.condition = condition; }

    @JsonIgnore
    @PropertyName("status")
    public String getStatusDb() {
        return status != null ? status.getValue() : null;
    }

    @JsonIgnore
    @PropertyName("status")
    public void setStatusDb(String value) {
        if (value != null) {
            Status s = Status.fromString(value);
            if (s != null) { this.status = s; }
        }
    }

    @JsonIgnore
    @PropertyName("condition")
    public String getConditionDb() {
        return condition != null ? condition.getValue() : null;
    }

    @JsonIgnore
    @PropertyName("condition")
    public void setConditionDb(String value) {
        if (value != null) {
            Condition c = Condition.fromString(value);
            if (c != null) { this.condition = c; }
        }
    }

    // =========================================================================
    // STANDARD GETTERS/SETTERS
    // =========================================================================

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getBoardgameId() { return boardgameId; }
    public void setBoardgameId(String boardgameId) { this.boardgameId = boardgameId; }
    public String getBoardgameTitle() { return boardgameTitle; }
    public void setBoardgameTitle(String boardgameTitle) { this.boardgameTitle = boardgameTitle; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public String getCurrentLoanId() { return currentLoanId; }
    public void setCurrentLoanId(String currentLoanId) { this.currentLoanId = currentLoanId; }
}
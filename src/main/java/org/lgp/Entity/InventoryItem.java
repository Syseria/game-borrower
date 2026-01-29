package org.lgp.Entity;

import com.google.cloud.firestore.annotation.DocumentId;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class InventoryItem {

    @DocumentId
    private String id;
    private String boardgameId;
    private String boardgameTitle;
    private Status status;
    private Condition condition;
    private String details;
    private String currentLoanId;

    @RegisterForReflection
    public enum Status {
        AVAILABLE("available"),
        BORROWED("borrowed"),
        MAINTENANCE("maintenance"),
        LOST("lost");

        private final String value;

        Status(String value) {
            this.value = value;
        }

        @com.fasterxml.jackson.annotation.JsonValue
        public String getValue() {
            return value;
        }

        public static Status fromString(String text) {
            for (Status s : Status.values()) {
                if (s.value.equalsIgnoreCase(text)) {
                    return s;
                }
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

        Condition(String value) {
            this.value = value;
        }

        @com.fasterxml.jackson.annotation.JsonValue
        public String getValue() {
            return value;
        }

        public static Condition fromString(String text) {
            for (Condition c : Condition.values()) {
                if (c.value.equalsIgnoreCase(text)) {
                    return c;
                }
            }
            return null;
        }
    }

    public InventoryItem() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBoardgameId() {
        return boardgameId;
    }

    public void setBoardgameId(String boardgameId) {
        this.boardgameId = boardgameId;
    }

    public String getBoardgameTitle() {
        return boardgameTitle;
    }

    public void setBoardgameTitle(String boardgameTitle) {
        this.boardgameTitle = boardgameTitle;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Condition getCondition() {
        return condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getCurrentLoanId() {
        return currentLoanId;
    }

    public void setCurrentLoanId(String currentLoanId) {
        this.currentLoanId = currentLoanId;
    }

    @Override
    public String toString() {
        return "InventoryItem{" +
                "id='" + id + '\'' +
                ", boardgameId='" + boardgameId + '\'' +
                ", boardgameTitle='" + boardgameTitle + '\'' +
                ", status=" + status +
                ", condition=" + condition +
                ", details='" + details + '\'' +
                ", currentLoanId='" + currentLoanId + '\'' +
                '}';
    }
}

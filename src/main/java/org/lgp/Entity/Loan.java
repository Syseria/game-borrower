package org.lgp.Entity;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.DocumentId;
import io.quarkus.runtime.annotations.RegisterForReflection;


@RegisterForReflection
public class Loan {

    @DocumentId
    private String id;

    // Who
    private String userId;
    private String userEmail;

    // What
    private String inventoryItemId;

    private String boardgameId;
    private String boardgameTitle;
    private String boardgameImageUrl;

    // When
    private Timestamp borrowedAt;
    private Timestamp dueAt;
    private Timestamp returnedAt;

    private Boolean active;

    public Loan() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getInventoryItemId() {
        return inventoryItemId;
    }

    public void setInventoryItemId(String inventoryItemId) {
        this.inventoryItemId = inventoryItemId;
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

    public String getBoardgameImageUrl() {
        return boardgameImageUrl;
    }

    public void setBoardgameImageUrl(String boardgameImageUrl) {
        this.boardgameImageUrl = boardgameImageUrl;
    }

    public Timestamp getBorrowedAt() {
        return borrowedAt;
    }

    public void setBorrowedAt(Timestamp borrowedAt) {
        this.borrowedAt = borrowedAt;
    }

    public Timestamp getDueAt() {
        return dueAt;
    }

    public void setDueAt(Timestamp dueAt) {
        this.dueAt = dueAt;
    }

    public Timestamp getReturnedAt() {
        return returnedAt;
    }

    public void setReturnedAt(Timestamp returnedAt) {
        this.returnedAt = returnedAt;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "Loan{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", userEmail='" + userEmail + '\'' +
                ", inventoryItemId='" + inventoryItemId + '\'' +
                ", boardgameId='" + boardgameId + '\'' +
                ", boardgameTitle='" + boardgameTitle + '\'' +
                ", boardgameImageUrl='" + boardgameImageUrl + '\'' +
                ", borrowedAt=" + borrowedAt +
                ", dueAt=" + dueAt +
                ", returnedAt=" + returnedAt +
                ", active=" + active +
                '}';
    }
}

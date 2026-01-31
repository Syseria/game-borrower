package org.lgp.Service;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.Query;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import org.lgp.Entity.Boardgame.BoardgameResponseDTO;
import org.lgp.Entity.InventoryItem;
import org.lgp.Entity.InventoryItem.Condition;
import org.lgp.Entity.InventoryItem.InventoryItemRequestDTO;
import org.lgp.Entity.InventoryItem.InventoryItemResponseDTO;
import org.lgp.Entity.InventoryItem.Status;
import org.lgp.Exception.ConflictException;
import org.lgp.Exception.ResourceNotFoundException;
import org.lgp.Exception.ServiceException;
import org.lgp.Exception.ValidationException;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@ApplicationScoped
public class InventoryService {

    private static final String COLLECTION = "inventory";

    @Inject
    Firestore firestore;

    @Inject
    BoardgameService boardgameService;

    @Inject
    Logger logger;

    // =========================================================================
    // PUBLIC METHODS
    // =========================================================================

    public String createItem(InventoryItemRequestDTO request) {
        try {
            InventoryItem item = new InventoryItem();
            item.setBoardgameId(request.boardgameId());
            item.setDetails(request.details());
            item.setCondition(Condition.valueOf(request.condition()));

            return firestore.collection(COLLECTION).add(item).get().getId();
        } catch (InterruptedException | ExecutionException e) {
            throw new ServiceException("Failed to create inventory item", e);
        }
    }

    public InventoryItemResponseDTO getItem(String id) {
        try {
            DocumentSnapshot doc = firestore.collection(COLLECTION).document(id).get().get();
            if (!doc.exists()) {
                throw new ResourceNotFoundException("Inventory item not found: " + id);
            }
            return mapEntityToResponse(doc);
        } catch (InterruptedException | ExecutionException e) {
            throw new ServiceException("Failed to fetch inventory item " + id, e);
        }
    }

    public List<InventoryItemResponseDTO> getItemsByBoardgameId(String boardgameId) {
        try {
            List<QueryDocumentSnapshot> docs = firestore.collection(COLLECTION)
                    .whereEqualTo("boardgameId", boardgameId)
                    .get().get().getDocuments();
            return docs.stream().map(this::mapEntityToResponse).collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new ServiceException("Failed to fetch items for game " + boardgameId, e);
        }
    }

    public List<InventoryItemResponseDTO> getAllAvailableItems() {
        try {
            List<QueryDocumentSnapshot> docs = firestore.collection(COLLECTION)
                    .whereEqualTo("status", Status.AVAILABLE.getValue())
                    .get().get().getDocuments();
            return docs.stream().map(this::mapEntityToResponse).collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new ServiceException("Failed to fetch available inventory", e);
        }
    }

    public void updateItem(String id, InventoryItemRequestDTO request) {
        try {
            DocumentSnapshot doc = firestore.collection(COLLECTION).document(id).get().get();
            if (!doc.exists()) throw new ResourceNotFoundException("Item not found: " + id);

            InventoryItem item = doc.toObject(InventoryItem.class);
            item.setDetails(request.details());

            Condition cond = Condition.fromString(request.condition());
            if (cond == null) throw new ValidationException("invalid-condition", "Invalid condition: " + request.condition());
            item.setCondition(cond);

            firestore.collection(COLLECTION).document(id).set(item).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new ServiceException("Failed to update item " + id, e);
        }
    }

    public void updateStatus(String id, Status newStatus) {
        try {
            DocumentSnapshot doc = firestore.collection(COLLECTION).document(id).get().get();
            if (!doc.exists()) throw new ResourceNotFoundException("Item not found: " + id);

            InventoryItem item = doc.toObject(InventoryItem.class);
            if (item.getStatus() == Status.BORROWED && newStatus == Status.AVAILABLE) {
                logger.warn("Manual status override on Borrowed item " + id);
            }

            item.setStatus(newStatus);
            firestore.collection(COLLECTION).document(id).set(item).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new ServiceException("Failed to update status for " + id, e);
        }
    }

    public void deleteItem(String id) {
        try {
            DocumentSnapshot doc = firestore.collection(COLLECTION).document(id).get().get();
            if (!doc.exists()) throw new ResourceNotFoundException("Item not found: " + id);

            InventoryItem item = doc.toObject(InventoryItem.class);
            if (item.getStatus() == Status.BORROWED) {
                throw new ConflictException("item-borrowed", "Cannot delete item while it is currently borrowed (Loan ID: " + item.getCurrentLoanId() + ")");
            }

            firestore.collection(COLLECTION).document(id).delete().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new ServiceException("Failed to delete item " + id, e);
        }
    }

    public List<InventoryItemResponseDTO> searchInventory(String gameId, Status status, Condition condition) {
        try {
            Query query = firestore.collection(COLLECTION);

            if (gameId != null && !gameId.isBlank()) {
                query = query.whereEqualTo("boardgameId", gameId);
            }
            if (status != null) {
                query = query.whereEqualTo("status", status.getValue());
            }
            if (condition != null) {
                query = query.whereEqualTo("condition", condition.getValue());
            }

            List<QueryDocumentSnapshot> docs = query.get().get().getDocuments();
            return docs.stream().map(this::mapEntityToResponse).collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new ServiceException("Failed to search inventory", e);
        }
    }

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================

    private InventoryItemResponseDTO mapEntityToResponse(DocumentSnapshot doc) {
        InventoryItem item = doc.toObject(InventoryItem.class);
        if (item == null) return null;

        return new InventoryItemResponseDTO(
                doc.getId(),
                item.getBoardgameId(),
                item.getBoardgameTitle(),
                item.getStatus(),
                item.getCondition(),
                item.getDetails(),
                item.getCurrentLoanId()
        );
    }
}
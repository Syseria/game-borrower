package org.lgp.Service;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldPath;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import org.lgp.Entity.InventoryItem;
import org.lgp.Entity.InventoryItem.InventorySearchCriteria;
import org.lgp.Entity.InventoryItem.Condition;
import org.lgp.Entity.InventoryItem.InventoryItemRequestDTO;
import org.lgp.Entity.InventoryItem.InventoryItemResponseDTO;
import org.lgp.Entity.InventoryItem.Status;
import org.lgp.Exception.ConflictException;
import org.lgp.Exception.ResourceNotFoundException;
import org.lgp.Exception.ServiceException;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@ApplicationScoped
public class InventoryService {

    private static final String COLLECTION = "inventory";

    @Inject
    Firestore firestore;

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
            item.setCondition(Condition.fromString(request.condition()));

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

    public void transitionItem(String id, Status nextStatus, Condition nextCondition, String nextDetails) {
        try {
            DocumentSnapshot doc = firestore.collection(COLLECTION).document(id).get().get();
            if (!doc.exists()) throw new ResourceNotFoundException("Item not found: " + id);

            InventoryItem item = doc.toObject(InventoryItem.class);

            // Security/Business Rule: Cannot move an item if it is currently BORROWED
            // unless moving it to LOST or RETURNED.
            if (item.getStatus() == Status.BORROWED &&
                    (nextStatus == Status.AVAILABLE || nextStatus == Status.MAINTENANCE)) {
                throw new ConflictException("item-active-loan",
                        "Cannot make item Available/Maintenance while it is still Borrowed.");
            }

            item.setStatus(nextStatus);
            if (nextCondition != null) item.setCondition(nextCondition);
            if (nextDetails != null) item.setDetails(nextDetails);

            firestore.collection(COLLECTION).document(id).set(item).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new ServiceException("Failed to transition item " + id, e);
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

    public List<InventoryItemResponseDTO> searchInventory(InventorySearchCriteria criteria) {
        try {
            Query query = firestore.collection(COLLECTION);

            if (criteria.id() != null) query = query.whereEqualTo(FieldPath.documentId(), criteria.id());
            if (criteria.gameId() != null && !criteria.gameId().isBlank()) query = query.whereEqualTo("boardgameId", criteria.gameId());
            if (criteria.status() != null) query = query.whereEqualTo("status", criteria.status().getValue());
            if (criteria.condition() != null) query = query.whereEqualTo("condition", criteria.condition().getValue());

            // Dynamic Sort
            String field = criteria.sortField() != null ? criteria.sortField() : "boardgameTitle";
            Query.Direction dir = "desc".equalsIgnoreCase(criteria.sortDir()) ?
                    Query.Direction.DESCENDING : Query.Direction.ASCENDING;

            query = query.orderBy(field, dir).orderBy(FieldPath.documentId(), dir);

            // Pagination
            int limit = criteria.pageSize() != null ? criteria.pageSize() : 20;

            if (criteria.isPrevious() && criteria.firstId() != null) {
                DocumentSnapshot cursor = firestore.collection(COLLECTION).document(criteria.firstId()).get().get();
                query = query.endBefore(cursor).limitToLast(limit);
            } else if (criteria.lastId() != null) {
                DocumentSnapshot cursor = firestore.collection(COLLECTION).document(criteria.lastId()).get().get();
                query = query.startAfter(cursor).limit(limit);
            } else {
                query = query.limit(limit);
            }

            return query.get().get().getDocuments().stream()
                    .map(this::mapEntityToResponse)
                    .collect(Collectors.toList());
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
package org.lgp.Service;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldPath;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.lgp.DTO.BoardgameSearchCriteria;
import org.lgp.Entity.Boardgame;
import org.lgp.DTO.BoardgameRequestDTO;
import org.lgp.DTO.BoardgameResponseDTO;
import org.lgp.DTO.InventorySearchCriteria;
import org.lgp.Exception.ServiceException;
import org.lgp.Exception.ResourceNotFoundException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@ApplicationScoped
public class BoardgameService {

    private static final String COLLECTION = "boardgames";

    @Inject
    InventoryService inventoryService;

    @Inject
    Firestore firestore;

    // =========================================================================
    // PUBLIC METHODS
    // =========================================================================

    public String createBoardgame(BoardgameRequestDTO request) {
        try {
            Boardgame boardgame = mapRequestToEntity(request);
            return firestore.collection(COLLECTION).add(boardgame).get().getId();
        } catch (InterruptedException | ExecutionException e) {
            throw new ServiceException("Failed to create boardgame", e);
        }
    }

    public List<BoardgameResponseDTO> searchBoardgames(BoardgameSearchCriteria criteria) {
        try {
            Query query = firestore.collection(COLLECTION);

            if (criteria.id() != null) query = query.whereEqualTo("id", criteria.id());
            if (criteria.title() != null) query = query.whereEqualTo("title", criteria.title());
            if (criteria.publisher() != null) query = query.whereEqualTo("publisher", criteria.publisher());
            if (criteria.minPlayers() != null) query = query.whereGreaterThanOrEqualTo("minPlayers", criteria.minPlayers());
            if (criteria.maxPlayers() != null) query = query.whereLessThanOrEqualTo("maxPlayers", criteria.maxPlayers());
            if (criteria.minAge() != null) query = query.whereGreaterThanOrEqualTo("minAge", criteria.minAge());
            if (criteria.minTime() != null) query = query.whereGreaterThanOrEqualTo("minTime", criteria.minTime());
            if (criteria.hasVideo() != null && criteria.hasVideo()) query = query.whereNotEqualTo("videoUrl", null);

            // Dynamic Sort
            String field = criteria.sortField() != null ? criteria.sortField() : "title";
            Query.Direction dir = "desc".equalsIgnoreCase(criteria.sortDir()) ?
                    Query.Direction.DESCENDING : Query.Direction.ASCENDING;
            // Always secondary sort by ID for cursor stability
            query = query.orderBy(field, dir).orderBy(FieldPath.documentId(), dir);


            // Pagination
            int limit = criteria.pageSize() != null ? criteria.pageSize() : 20;

            // Case 1: We are asking for a previous page
            if (criteria.isPrevious() && criteria.firstId() != null) {
                DocumentSnapshot cursor = firestore.collection(COLLECTION).document(criteria.firstId()).get().get();
                query = query.endBefore(cursor).limitToLast(limit);
            // Case 2: We are asking for a next page
            } else if (criteria.lastId() != null) {
                DocumentSnapshot cursor = firestore.collection(COLLECTION).document(criteria.lastId()).get().get();
                query = query.startAfter(cursor).limit(limit);
            // Case 3: First load / reset of the sorting
            } else {
                query = query.limit(limit);
            }

            return query.get().get().getDocuments().stream()
                    .map(this::mapEntityToResponse).collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new ServiceException("Boardgame search failed", e);
        }
    }

    public void updateBoardgame(String id, BoardgameRequestDTO request) {
        try {
            if (!firestore.collection(COLLECTION).document(id).get().get().exists()) {
                throw new ResourceNotFoundException("Cannot update, boardgame not found: " + id);
            }
            Boardgame entity = mapRequestToEntity(request);
            firestore.collection(COLLECTION).document(id).set(entity).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new ServiceException("Failed to update boardgame " + id, e);
        }
    }

    /**
     * Deletes a board game from the catalog.
     * Enforced Rule: Cannot delete a game if physical items exist in the inventory.
     * @param id The ID of the board game to delete.
     */
    public void deleteBoardgame(String id) {
        try {
            // Check if any inventory item is linked to this boardgameId
            InventorySearchCriteria criteria = InventorySearchCriteria.builder()
                    .gameId(id)
                    .build();

            var inventoryItems = inventoryService.searchInventory(criteria);

            if (!inventoryItems.isEmpty()) {
                throw new org.lgp.Exception.ConflictException("game-has-inventory",
                        "Cannot delete game: " + inventoryItems.size() + " items still exist in inventory.");
            }

            firestore.collection(COLLECTION).document(id).delete().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new ServiceException("Failed to delete boardgame " + id, e);
        }
    }

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================

    /**
     * Converts a Firestore reading to a Boardgame entity.
     * @param req DTO of the Firestore reading
     * @return Boardgame Object
     */
    private Boardgame mapRequestToEntity(BoardgameRequestDTO req) {
        Boardgame bg = new Boardgame();
        bg.setTitle(req.title());
        bg.setPublisher(req.publisher());
        bg.setMinPlayers(req.minPlayers());
        bg.setMaxPlayers(req.maxPlayers());
        bg.setMinAge(req.minAge());
        bg.setMinTime(req.minTime());
        bg.setMaxTime(req.maxTime());
        bg.setDescription(req.description());
        bg.setImageUrl(req.imageUrl());
        bg.setVideoUrl(req.videoUrl());
        return bg;
    }

    /**
     * Allows the mapping of an Entity to a DTO to easily save it to the Firestore DB.
     * @param doc Firestore snapshot
     * @return Boardgame.BoardgameResponseDTO
     */
    private BoardgameResponseDTO mapEntityToResponse(DocumentSnapshot doc) {
        Boardgame bg = doc.toObject(Boardgame.class);
        if (bg == null) return null;

        return new BoardgameResponseDTO(
                doc.getId(),
                bg.getTitle(),
                bg.getPublisher(),
                bg.getMinPlayers(),
                bg.getMaxPlayers(),
                bg.getMinAge(),
                bg.getMinTime(),
                bg.getMaxTime(),
                bg.getDescription(),
                bg.getImageUrl(),
                bg.getVideoUrl()
        );
    }
}
package org.lgp.Service;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.lgp.Entity.Boardgame;
import org.lgp.Entity.Boardgame.BoardgameRequestDTO;
import org.lgp.Entity.Boardgame.BoardgameResponseDTO;
import org.lgp.Exception.ServiceException;
import org.lgp.Exception.ResourceNotFoundException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@ApplicationScoped
public class BoardgameService {

    private static final String COLLECTION = "boardgames";

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

    public List<BoardgameResponseDTO> searchBoardgames(Boardgame.BoardgameSearchCriteria criteria) {
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

    public void deleteBoardgame(String id) {
        try {
            /// TODO: Check InventoryService before deleting
            // if (hasItems) throw new ConflictException("game-has-inventory", "Cannot delete game with active inventory");
            firestore.collection(COLLECTION).document(id).delete().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new ServiceException("Failed to delete boardgame " + id, e);
        }
    }

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================

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
package org.lgp.Service;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import org.lgp.Entity.Boardgame.BoardgameSearchCriteria;
import org.lgp.Entity.Boardgame.BoardgameResponseDTO;
import org.lgp.Entity.InventoryItem;
import org.lgp.Entity.Loan;
import org.lgp.Entity.Loan.LoanSearchCriteria;
import org.lgp.Entity.Loan.CreateLoanRequestDTO;
import org.lgp.Entity.Loan.LoanResponseDTO;
import org.lgp.Entity.User.UserSearchCriteria;
import org.lgp.Entity.User.UserProfileResponseDTO;
import org.lgp.Exception.ConflictException;
import org.lgp.Exception.ResourceNotFoundException;
import org.lgp.Exception.ServiceException;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@ApplicationScoped
public class LoanService {

    private static final String COLLECTION = "loans";
    private static final int DEFAULT_LOAN_DAYS = 14;

    @Inject Firestore firestore;
    @Inject InventoryService inventoryService;
    @Inject UserService userService;
    @Inject BoardgameService boardgameService;
    @Inject Logger logger;

    // =========================================================================
    // PUBLIC METHODS
    // =========================================================================

    public String createLoan(CreateLoanRequestDTO request) {
        try {
            return firestore.runTransaction(transaction -> {
                // --- STEP 1: READS ---

                UserSearchCriteria userCriteria = UserSearchCriteria.builder()
                        .id(request.userId())
                        .build();
                List<UserProfileResponseDTO> users = userService.searchUsers(userCriteria);
                if (users.isEmpty()) {
                    throw new ResourceNotFoundException("User not found: " + request.userId());
                }
                UserProfileResponseDTO user = users.getFirst();

                // Fetch Inventory Item directly via Reference for transaction tracking
                DocumentReference itemRef = firestore.collection("inventory").document(request.inventoryItemId());
                DocumentSnapshot itemSnap = transaction.get(itemRef).get();
                if (!itemSnap.exists()) {
                    throw new ResourceNotFoundException("Inventory item not found: " + request.inventoryItemId());
                }
                InventoryItem item = itemSnap.toObject(InventoryItem.class);

                // Business Logic: Check if available
                if (item.getStatus() != InventoryItem.Status.AVAILABLE) {
                    throw new ConflictException("item-unavailable", "Item is currently " + item.getStatus());
                }

                // Use the new Search Builder for Boardgame
                BoardgameSearchCriteria bgCriteria = BoardgameSearchCriteria.builder()
                        .id(item.getBoardgameId())
                        .build();
                List<BoardgameResponseDTO> games = boardgameService.searchBoardgames(bgCriteria);
                if (games.isEmpty()) {
                    throw new ResourceNotFoundException("Boardgame not found: " + item.getBoardgameId());
                }
                BoardgameResponseDTO game = games.getFirst();

                // --- STEP 2: WRITES ---

                // Create the Loan Entity
                DocumentReference loanRef = firestore.collection(COLLECTION).document();
                Loan loan = new Loan();
                loan.setUserId(user.uid());
                loan.setUserEmail(user.email());
                loan.setInventoryItemId(itemSnap.getId());
                loan.setBoardgameId(game.id());
                loan.setBoardgameTitle(game.title());
                loan.setBoardgameImageUrl(game.imageUrl());

                Date now = new Date();
                loan.setBorrowedAt(Timestamp.of(now));

                // Set due date logic
                if (request.dueDate() != null) {
                    loan.setDueAt(Timestamp.of(request.dueDate()));
                } else {
                    loan.setDueAt(Timestamp.of(calculateDueDate(now, DEFAULT_LOAN_DAYS)));
                }
                loan.setActive(true);

                transaction.set(loanRef, loan);

                transaction.update(itemRef, "status", InventoryItem.Status.BORROWED.getValue());

                return loanRef.getId();
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            if (e.getCause() instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new ServiceException("Checkout failed due to database error", e);
        }
    }

    public void returnLoan(String loanId) {
        try {
            firestore.runTransaction(transaction -> {
                DocumentReference loanRef = firestore.collection(COLLECTION).document(loanId);
                DocumentSnapshot loanSnap = transaction.get(loanRef).get();
                if (!loanSnap.exists()) throw new ResourceNotFoundException("Loan not found");
                Loan loan = loanSnap.toObject(Loan.class);

                if (!loan.getActive()) throw new ConflictException("loan-closed", "Loan already returned");

                transaction.update(loanRef, "active", false, "returnedAt", Timestamp.now());

                DocumentReference itemRef = firestore.collection("inventory").document(loan.getInventoryItemId());
                transaction.update(itemRef, "status", InventoryItem.Status.RETURNED.getValue());

                return null;
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new ServiceException("Transactional return failed", e);
        }
    }

    public LoanResponseDTO getLoan(String id) {
        try {
            DocumentSnapshot doc = firestore.collection(COLLECTION).document(id).get().get();
            if (!doc.exists()) throw new ResourceNotFoundException("Loan not found: " + id);
            return mapEntityToResponse((QueryDocumentSnapshot) doc);
        } catch (InterruptedException | ExecutionException e) {
            throw new ServiceException("Failed to fetch loan " + id, e);
        }
    }

    public List<LoanResponseDTO> searchLoans(LoanSearchCriteria criteria) {
        try {
            Query query = firestore.collection(COLLECTION);

            if (criteria.userId() != null && !criteria.userId().isBlank()) query = query.whereEqualTo("userId", criteria.userId());
            if (criteria.gameId() != null && !criteria.gameId().isBlank()) query = query.whereEqualTo("boardgameId", criteria.gameId());
            if (criteria.inventoryItemId() != null && !criteria.inventoryItemId().isBlank()) query = query.whereEqualTo("inventoryItemId", criteria.inventoryItemId());
            if (criteria.title() != null && !criteria.title().isBlank()) query = query.whereEqualTo("boardgameTitle", criteria.title());
            if (criteria.activeOnly() != null && criteria.activeOnly()) query = query.whereEqualTo("active", true);

            // Handle Date Range filters
            if (criteria.borrowedAt() != null) query = addDayRangeFilter(query, "borrowedAt", criteria.borrowedAt());
            else if (criteria.dueAt() != null) query = addDayRangeFilter(query, "dueAt", criteria.dueAt());
            else if (criteria.returnedAt() != null) query = addDayRangeFilter(query, "returnedAt", criteria.returnedAt());

            // Sorting
            String sortField = (criteria.sortBy() != null && !criteria.sortBy().isBlank()) ? criteria.sortBy() : "borrowedAt";
            query = query.orderBy(sortField, Query.Direction.DESCENDING);

            return query.get().get().getDocuments().stream()
                    .map(this::mapEntityToResponse)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new ServiceException("Search failed", e);
        }
    }

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================

    private Query addDayRangeFilter(Query query, String fieldName, Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date start = cal.getTime();

        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        Date end = cal.getTime();

        return query.whereGreaterThanOrEqualTo(fieldName, Timestamp.of(start))
                .whereLessThanOrEqualTo(fieldName, Timestamp.of(end));
    }

    private Date calculateDueDate(Date start, int days) {
        Calendar c = Calendar.getInstance();
        c.setTime(start);
        c.add(Calendar.DATE, days);
        return c.getTime();
    }

    private LoanResponseDTO mapEntityToResponse(QueryDocumentSnapshot doc) {
        Loan l = doc.toObject(Loan.class);
        if (l == null) return null;

        return new LoanResponseDTO(
                doc.getId(),
                l.getUserId(),
                l.getUserEmail(),
                l.getInventoryItemId(),
                l.getBoardgameId(),
                l.getBoardgameTitle(),
                l.getBoardgameImageUrl(),
                l.toDate(l.getBorrowedAt()),
                l.toDate(l.getDueAt()),
                l.toDate(l.getReturnedAt()),
                l.getActive()
        );
    }
}
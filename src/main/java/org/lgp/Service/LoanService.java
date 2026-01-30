package org.lgp.Service;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import org.lgp.Entity.Boardgame.BoardgameResponse;
import org.lgp.Entity.InventoryItem;
import org.lgp.Entity.Loan;
import org.lgp.Entity.Loan.CreateLoanRequestDTO;
import org.lgp.Entity.Loan.LoanResponseDTO;
import org.lgp.Entity.Loan.ReturnLoanRequestDTO;
import org.lgp.Entity.User.UserProfileResponseDTO;
import org.lgp.Exception.ResourceNotFoundException;
import org.lgp.Exception.ServiceException;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@ApplicationScoped
public class LoanService {

    @Inject Firestore firestore;
    @Inject InventoryService inventoryService;
    @Inject UserService userService;
    @Inject BoardgameService boardgameService;
    @Inject Logger logger;

    private static final String COLLECTION = "loans";
    private static final int DEFAULT_LOAN_DAYS = 14;

    // =========================================================================
    // 1. CHECKOUT
    // =========================================================================
    public String createLoan(CreateLoanRequestDTO request) {
        try {
            UserProfileResponseDTO user = userService.getUser(request.userId());

            var itemDTO = inventoryService.getItem(request.inventoryItemId());
            if (itemDTO.status() != InventoryItem.Status.AVAILABLE) {
                throw new IllegalArgumentException("Item not available: " + itemDTO.status());
            }

            BoardgameResponse game = boardgameService.getBoardgame(itemDTO.boardgameId());

            Loan loan = new Loan();
            loan.setUserId(user.uid());
            loan.setUserEmail(user.email());

            loan.setInventoryItemId(itemDTO.id());
            loan.setBoardgameId(game.id());
            loan.setBoardgameTitle(game.title());
            loan.setBoardgameImageUrl(game.imageUrl()); // Nice for UI!

            // Dates
            Date now = new Date();
            loan.setBorrowedAt(Timestamp.of(now));
            if (request.dueDate() != null) {
                if (request.dueDate().before(now)) {
                    throw new IllegalArgumentException("Due date cannot be in the past");
                }
                loan.setDueAt(Timestamp.of(request.dueDate()));
            } else {
                loan.setDueAt(Timestamp.of(calculateDueDate(now, DEFAULT_LOAN_DAYS)));
            }
            loan.setActive(true);

            String loanId = firestore.collection(COLLECTION).add(loan).get().getId();

            inventoryService.updateStatus(itemDTO.id(), InventoryItem.Status.BORROWED);

            return loanId;

        } catch (InterruptedException | ExecutionException e) {
            throw new ServiceException("Checkout failed", e);
        }
    }

    // =========================================================================
    // 2. CHECKIN
    // =========================================================================
    public void returnLoan(String loanId, ReturnLoanRequestDTO request) {
        try {
            DocumentReference loanDoc = firestore.collection(COLLECTION).document(loanId);
            Loan loan = loanDoc.get().get().toObject(Loan.class);

            if (loan == null) throw new ResourceNotFoundException("Loan not found: " + loanId);
            if (!loan.getActive()) throw new IllegalArgumentException("Loan already returned");

            loan.setActive(false);
            loan.setReturnedAt(Timestamp.now());
            loanDoc.set(loan);

            var item = inventoryService.getItem(loan.getInventoryItemId());

            inventoryService.updateStatus(item.id(), InventoryItem.Status.AVAILABLE);

            if (request != null && request.condition() != null && !request.condition().isBlank()) {
                InventoryItem.InventoryItemRequestDTO updateReq = new InventoryItem.InventoryItemRequestDTO(
                        item.boardgameId(), request.condition(), item.details()
                );
                inventoryService.updateItem(item.id(), updateReq);
            }

        } catch (InterruptedException | ExecutionException e) {
            throw new ServiceException("Check-in failed", e);
        }
    }

    // =========================================================================
    // 3. SEARCH
    // =========================================================================
    public List<LoanResponseDTO> searchLoans(
            String userId,
            String gameId,
            String boxId,
            String title,
            boolean activeOnly,
            Date borrowedAt,
            Date dueAt,
            Date returnedAt,
            String sortBy
    ) {
        try {
            Query query = firestore.collection(COLLECTION);

            // --- 1. STRING FILTERS ---
            if (userId != null && !userId.isBlank()) query = query.whereEqualTo("userId", userId);
            if (gameId != null && !gameId.isBlank()) query = query.whereEqualTo("boardgameId", gameId);
            if (boxId != null && !boxId.isBlank()) query = query.whereEqualTo("inventoryItemId", boxId);
            if (title != null && !title.isBlank()) query = query.whereEqualTo("boardgameTitle", title);

            // --- 2. STATUS FILTER ---
            if (activeOnly) query = query.whereEqualTo("active", true);

            // --- 3. DATE FILTERS (Range Logic) ---
            if (borrowedAt != null) {
                query = addDayRangeFilter(query, "borrowedAt", borrowedAt);
            } else if (dueAt != null) {
                query = addDayRangeFilter(query, "dueAt", dueAt);
            } else if (returnedAt != null) {
                query = addDayRangeFilter(query, "returnedAt", returnedAt);
            }

            // --- 4. SORTING ---
            String sortField = "borrowedAt";
            if (sortBy != null && !sortBy.isBlank()) {
                sortField = sortBy;
            } else if (dueAt != null) {
                sortField = "dueAt";
            }

            query = query.orderBy(sortField, Query.Direction.DESCENDING);

            return query.get().get().getDocuments().stream()
                    .map(this::mapEntityToResponse)
                    .collect(Collectors.toList());

        } catch (InterruptedException | ExecutionException e) {
            throw new ServiceException("Search failed", e);
        }
    }

    private Query addDayRangeFilter(Query query, String fieldName, Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        // Start of Day (00:00:00)
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date start = cal.getTime();

        // End of Day (23:59:59)
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        Date end = cal.getTime();

        return query.whereGreaterThanOrEqualTo(fieldName, Timestamp.of(start))
                .whereLessThanOrEqualTo(fieldName, Timestamp.of(end));
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

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
package org.lgp.Resource.api;

import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.lgp.DTO.CreateLoanRequestDTO;
import org.lgp.DTO.LoanSearchCriteria;
import org.lgp.DTO.LoanResponseDTO;
import org.lgp.DTO.PageResponse;
import org.lgp.Exception.ResourceNotFoundException;
import org.lgp.Exception.ValidationException;
import org.lgp.Service.LoanService;
import java.net.URI;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Date;

@Path("/api/loans")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LoanResource {

    @Inject LoanService loanService;
    @Inject SecurityIdentity identity;

    // =========================================================================
    // READ
    // =========================================================================

    /**
     * Searches and filters loans based on various criteria.
     * Enforces an ownership check: non-maintainers can only see their own loans.
     *
     * @param userIdParam    The user ID to filter by (Maintainer only).
     * @param gameId         Filter by Boardgame ID.
     * @param boxId          Filter by Inventory Item ID.
     * @param title          Filter by Boardgame title.
     * @param active         Filter for active loans only.
     * @param borrowedAtStr  Filter by borrowed date (yyyy-MM-dd).
     * @param dueAtStr       Filter by due date (yyyy-MM-dd).
     * @param returnedAtStr  Filter by returned date (yyyy-MM-dd).
     * @param sortBy         Field to sort the results by.
     * @return A list of matching LoanResponseDTOs.
     */
    @GET
    public PageResponse<LoanResponseDTO> search(
            // Filtering
            @QueryParam("userId") String userIdParam,
            @QueryParam("gameId") String gameId,
            @QueryParam("boxId") String boxId,
            @QueryParam("title") String title,
            @QueryParam("active") @DefaultValue("false") boolean active,
            @QueryParam("borrowedAt") String borrowedAtStr,
            @QueryParam("dueAt") String dueAtStr,
            @QueryParam("returnedAt") String returnedAtStr,

            // Sorting
            @QueryParam("sortBy") @DefaultValue("title") String sortBy,
            @QueryParam("sortDir") @DefaultValue("asc") String sortDir,
            @QueryParam("pageSize") @DefaultValue("20") Integer pageSize,
            @QueryParam("firstId") String firstId,
            @QueryParam("lastId") String lastId,
            @QueryParam("isPrevious") @DefaultValue("false") boolean isPrevious
    ) {
        // Security Lock: Normal users are restricted to their own UID
        String targetUserId = identity.hasRole("maintainer") ? userIdParam : identity.getPrincipal().getName();

        // Build criteria using the standardised Builder pattern
        LoanSearchCriteria criteria = LoanSearchCriteria.builder()
                .userId(targetUserId)
                .gameId(gameId)
                .inventoryItemId(boxId)
                .title(title)
                .activeOnly(active)
                .borrowedAt(parseDate(borrowedAtStr))
                .dueAt(parseDate(dueAtStr))
                .returnedAt(parseDate(returnedAtStr))
                .sortField(sortBy)
                .sortDir(sortDir)
                .pageSize(pageSize)
                .firstId(firstId)
                .lastId(lastId)
                .isPrevious(isPrevious)
                .build();

        return loanService.searchLoans(criteria);
    }

    @GET
    @Path("/{id}")
    public LoanResponseDTO get(@PathParam("id") String id) {
        LoanResponseDTO loan = loanService.getLoan(id);

        // Ownership Check: Internal Lock
        String currentUserId = identity.getPrincipal().getName();
        boolean isMaintainer = identity.hasRole("maintainer");

        if (!isMaintainer && !loan.userId().equals(currentUserId)) {
            // Forbidden if not the owner and not a maintainer
            // We return not found for security purposes
            throw new ResourceNotFoundException("Loan not found.");
        }

        return loan;
    }

    // =========================================================================
    // WRITE
    // =========================================================================

    @POST
    public Response checkout(@Valid CreateLoanRequestDTO request) {
        String id = loanService.createLoan(request);
        return Response.created(URI.create("/loans/" + id)).build();
    }

    @PUT
    @Path("/{id}/return")
    public Response checkin(@PathParam("id") String id) {
        loanService.returnLoan(id);
        return Response.ok().build();
    }

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================

    private Date parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        try {
            LocalDate localDate = LocalDate.parse(dateStr);
            return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        } catch (DateTimeParseException e) {
            throw new ValidationException("invalid-date-format", "Date '" + dateStr + "' is invalid. Use yyyy-MM-dd.");
        }
    }
}
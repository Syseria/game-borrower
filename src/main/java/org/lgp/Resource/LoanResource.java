package org.lgp.Resource;

import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.lgp.Entity.Loan.CreateLoanRequestDTO;
import org.lgp.Entity.Loan.LoanResponseDTO;
import org.lgp.Entity.Loan.ReturnLoanRequestDTO;
import org.lgp.Exception.ResourceNotFoundException;
import org.lgp.Exception.ValidationException;
import org.lgp.Service.LoanService;
import java.net.URI;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;

@Path("/loans")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LoanResource {

    @Inject LoanService loanService;
    @Inject SecurityIdentity identity;

    // =========================================================================
    // READ
    // =========================================================================

    @GET
    public List<LoanResponseDTO> list(
            @QueryParam("userId") String userIdParam,
            @QueryParam("gameId") String gameId,
            @QueryParam("boxId") String boxId,
            @QueryParam("title") String title,
            @QueryParam("borrowedAt") String borrowedAtStr,
            @QueryParam("dueAt") String dueAtStr,
            @QueryParam("returnedAt") String returnedAtStr,
            @QueryParam("sortBy") String sortBy,
            @QueryParam("active") @DefaultValue("false") boolean active
    ) {
        String targetUserId = identity.hasRole("maintainer") ? userIdParam : identity.getPrincipal().getName();

        Date borrowedAt = parseDate(borrowedAtStr);
        Date dueAt = parseDate(dueAtStr);
        Date returnedAt = parseDate(returnedAtStr);

        return loanService.searchLoans(
                targetUserId, gameId, boxId, title, active,
                borrowedAt, dueAt, returnedAt, sortBy
        );
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
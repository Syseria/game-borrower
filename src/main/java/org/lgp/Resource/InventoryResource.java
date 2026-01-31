package org.lgp.Resource;

import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.lgp.Entity.InventoryItem.InventoryStatusUpdateRequestDTO;
import org.lgp.Entity.InventoryItem.Condition;
import org.lgp.Entity.InventoryItem.InventoryItemRequestDTO;
import org.lgp.Entity.InventoryItem.InventoryItemResponseDTO;
import org.lgp.Entity.InventoryItem.Status;
import org.lgp.Service.InventoryService;
import java.net.URI;
import java.util.List;

@Path("/inventory")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class InventoryResource {

    @Inject
    InventoryService inventoryService;

    @Inject
    SecurityIdentity identity;

    // =========================================================================
    // READ
    // =========================================================================

    @GET
    public List<InventoryItemResponseDTO> list(
            @QueryParam("gameId") String gameId,
            @QueryParam("status") String status,
            @QueryParam("condition") String condition
    ) {
        Status requestedStatus = (status != null) ? Status.fromString(status) : null;
        Condition requestedCondition = (condition != null) ? Condition.fromString(condition) : null;

        boolean isPrivileged = identity.hasRole("maintainer");

        if (!isPrivileged) {
            requestedStatus = Status.AVAILABLE;
            requestedCondition = null;
        }

        return inventoryService.searchInventory(gameId, requestedStatus, requestedCondition);
    }

    @GET
    @Path("/{id}")
    public InventoryItemResponseDTO get(@PathParam("id") String id) {
        return inventoryService.getItem(id);
    }

    // =========================================================================
    // WRITE (Maintainer Only)
    // =========================================================================

    @POST
    @RolesAllowed("maintainer")
    public Response create(@Valid InventoryItemRequestDTO request) {
        String id = inventoryService.createItem(request);
        return Response.created(URI.create("/inventory/" + id)).build();
    }

    @PUT
    @Path("/{id}/lifecycle")
    @RolesAllowed("maintainer")
    public Response updateLifecycle(@PathParam("id") String id, @Valid InventoryStatusUpdateRequestDTO request) {
        inventoryService.transitionItem(
                id,
                Status.fromString(request.status()),
                Condition.fromString(request.condition()),
                request.details()
        );
        return Response.ok().build();
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("maintainer")
    public Response delete(@PathParam("id") String id) {
        inventoryService.deleteItem(id);
        return Response.noContent().build();
    }
}
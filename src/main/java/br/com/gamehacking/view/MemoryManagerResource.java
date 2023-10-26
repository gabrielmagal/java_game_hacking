package br.com.gamehacking.view;

import br.com.gamehacking.controller.MemoryManager;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/memorymanager")
public class MemoryManagerResource {
    @Inject
    MemoryManager memoryManager;

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public String changeMemory(@QueryParam("processName") String processName,
                             @QueryParam("address") String address,
                             @QueryParam("value") String value,
                             @QueryParam("size") int size) {
        return memoryManager.changeMemory(processName, Long.parseLong(address, 16), Long.parseLong(value), size);
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String readMemory(@QueryParam("processName") String processName,
                             @QueryParam("address") String address,
                             @QueryParam("size") int size) {
        return memoryManager.readMemory(processName, Long.parseLong(address, 16), size);
    }
}

package io.github.xydani.fleetcare.vehicle.owner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import io.github.xydani.fleetcare.vehicle.owner.dto.OwnerRequest;
import io.github.xydani.fleetcare.vehicle.owner.dto.OwnerResponse;

@WebMvcTest(OwnerController.class)
class OwnerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OwnerService ownerService;

    private static final String VALID_BODY = """
            {"firstName": "Giulia", "lastName": "Bianchi", "email": "giulia.bianchi@example.it"}
            """;

    @Test
    void shouldReturnOwner_whenOwnerExists() throws Exception {
        when(ownerService.findById(1L)).thenReturn(response(1L));

        mockMvc.perform(get("/api/owners/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("giulia.bianchi@example.it"));
    }

    @Test
    void shouldReturn404_whenOwnerDoesNotExist() throws Exception {
        when(ownerService.findById(99L)).thenThrow(new OwnerNotFoundException(99L));

        mockMvc.perform(get("/api/owners/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Owner 99 not found"));
    }

    @Test
    void shouldReturn201WithLocation_whenOwnerIsCreated() throws Exception {
        when(ownerService.create(any(OwnerRequest.class))).thenReturn(response(7L));

        mockMvc.perform(post("/api/owners").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/owners/7"))
                .andExpect(jsonPath("$.id").value(7));
    }

    @Test
    void shouldReturn400_whenEmailIsInvalid() throws Exception {
        mockMvc.perform(post("/api/owners")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName": "Giulia", "lastName": "Bianchi", "email": "not-an-email"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    void shouldReturn409_whenEmailIsAlreadyUsed() throws Exception {
        when(ownerService.create(any(OwnerRequest.class)))
                .thenThrow(new DuplicateEmailException("giulia.bianchi@example.it"));

        mockMvc.perform(post("/api/owners").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturn204_whenOwnerIsDeleted() throws Exception {
        mockMvc.perform(delete("/api/owners/1"))
                .andExpect(status().isNoContent());
    }

    private static OwnerResponse response(Long id) {
        return new OwnerResponse(id, "Giulia", "Bianchi", "giulia.bianchi@example.it", null,
                Instant.parse("2026-10-19T08:00:00Z"));
    }
}

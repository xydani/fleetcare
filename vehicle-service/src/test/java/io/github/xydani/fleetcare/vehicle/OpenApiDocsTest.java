package io.github.xydani.fleetcare.vehicle;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class OpenApiDocsTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldPublishOpenApiDocument_withAllApiGroups() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.servers[0].url").value("/"))
                .andExpect(jsonPath("$.info.title").value("FleetCare Vehicle Service"))
                .andExpect(jsonPath("$.paths['/api/owners']").exists())
                .andExpect(jsonPath("$.paths['/api/vehicles/{id}']").exists())
                .andExpect(jsonPath("$.paths['/api/vehicles/{vehicleId}/mileage']").exists())
                .andExpect(jsonPath("$.paths['/api/vehicles/inspections/expiring']").exists())
                .andExpect(jsonPath("$.paths['/api/vehicles/stats']").exists());
    }

    @Test
    void shouldServeSwaggerUi() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }
}

package com.reservo.api.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiDocumentationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /v3/api-docs should return 200 OK with OpenAPI specification")
    void shouldReturnOpenApiDocumentation() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi", containsString("3.")))
                .andExpect(jsonPath("$.info.title").value("Reservation API"))
                .andExpect(jsonPath("$.paths['/api/v1/establishments']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/establishments'].post.summary").value("Create a new establishment"))
                .andExpect(jsonPath("$.paths['/api/v1/establishments/{id}'].get.summary").value("Get establishment by ID"));
    }
}

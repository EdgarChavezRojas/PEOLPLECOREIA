package com.solveria.iamservice.contract;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solveria.core.shared.exceptions.EntityNotFoundException;
import com.solveria.iamservice.api.exception.ErrorCodes;
import com.solveria.iamservice.api.exception.GlobalExceptionHandler;
import com.solveria.iamservice.api.rest.AssignPermissionsToRoleController;
import com.solveria.iamservice.application.dto.AssignPermissionsToRoleResponse;
import com.solveria.iamservice.application.orchestration.AssignPermissionsToRoleOrchestrator;
import com.solveria.iamservice.config.JpaConfig;
import com.solveria.iamservice.config.security.JwtProperties;
import com.solveria.iamservice.config.security.SecurityConfig;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
        controllers = AssignPermissionsToRoleController.class,
        excludeFilters =
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JpaConfig.class))
@Import({
    GlobalExceptionHandler.class,
    SecurityConfig.class,
    AssignPermissionsToRoleContractTest.TestConfig.class
})
@EnableConfigurationProperties(JwtProperties.class)
class AssignPermissionsToRoleContractTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @Autowired private AssignPermissionsToRoleOrchestrator assignPermissionsToRoleOrchestrator;

    @BeforeEach
    void resetMocks() {
        reset(assignPermissionsToRoleOrchestrator);
    }

    @Test
    void shouldReturn200OkWhenPermissionsAssignedSuccessfully() throws Exception {
        Long roleId = 1L;
        List<Long> permissionIds = List.of(1L, 2L, 3L, 4L);

        AssignPermissionsToRoleResponse response =
                new AssignPermissionsToRoleResponse(
                        1L, "ADMIN", "Administrator role", permissionIds);

        doReturn(response).when(assignPermissionsToRoleOrchestrator).execute(any());

        String requestBody =
                objectMapper.writeValueAsString(
                        new com.solveria.iamservice.application.dto.PermissionIdsRequest(
                                permissionIds));

        mockMvc.perform(
                        put("/api/v1/iam/roles/{roleId}/permissions", roleId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("ADMIN"))
                .andExpect(jsonPath("$.description").value("Administrator role"))
                .andExpect(jsonPath("$.permissionIds").isArray())
                .andExpect(jsonPath("$.permissionIds[0]").value(1L))
                .andExpect(jsonPath("$.permissionIds[1]").value(2L))
                .andExpect(jsonPath("$.permissionIds[2]").value(3L))
                .andExpect(jsonPath("$.permissionIds[3]").value(4L));
    }

    @Test
    void shouldReturn400BadRequestWhenValidationFails() throws Exception {
        Long roleId = 1L;
        String requestBody =
                """
                {
                  "permissionIds": []
                }
                """;

        mockMvc.perform(
                        put("/api/v1/iam/roles/{roleId}/permissions", roleId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errorCode").value(ErrorCodes.VALIDATION_ERROR))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/api/v1/iam/roles/1/permissions"))
                .andExpect(jsonPath("$.details").exists())
                .andExpect(jsonPath("$.details.permissionIds").exists());
    }

    @Test
    void shouldReturn404NotFoundWhenRoleNotFound() throws Exception {
        Long roleId = 999L;
        List<Long> permissionIds = List.of(1L, 2L);

        EntityNotFoundException exception = new EntityNotFoundException("Role", roleId.toString());
        doThrow(exception).when(assignPermissionsToRoleOrchestrator).execute(any());

        String requestBody =
                objectMapper.writeValueAsString(
                        new com.solveria.iamservice.application.dto.PermissionIdsRequest(
                                permissionIds));

        mockMvc.perform(
                        put("/api/v1/iam/roles/{roleId}/permissions", roleId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errorCode").value("error.entity.not_found"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/api/v1/iam/roles/999/permissions"));
    }

    @Test
    void shouldReturn500InternalServerErrorWhenUnexpectedErrorOccurs() throws Exception {
        Long roleId = 1L;
        List<Long> permissionIds = List.of(1L, 2L);

        doThrow(new RuntimeException("Unexpected error"))
                .when(assignPermissionsToRoleOrchestrator)
                .execute(any());

        String requestBody =
                objectMapper.writeValueAsString(
                        new com.solveria.iamservice.application.dto.PermissionIdsRequest(
                                permissionIds));

        mockMvc.perform(
                        put("/api/v1/iam/roles/{roleId}/permissions", roleId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errorCode").value(ErrorCodes.UNEXPECTED_ERROR))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/api/v1/iam/roles/1/permissions"));
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        AssignPermissionsToRoleOrchestrator assignPermissionsToRoleOrchestrator() {
            return Mockito.mock(AssignPermissionsToRoleOrchestrator.class);
        }
    }
}

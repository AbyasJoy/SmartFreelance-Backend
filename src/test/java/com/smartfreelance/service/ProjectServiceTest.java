package com.smartfreelance.service;

import com.smartfreelance.dto.PageResponse;
import com.smartfreelance.dto.ProjectRequest;
import com.smartfreelance.dto.ProjectResponse;
import com.smartfreelance.entity.Category;
import com.smartfreelance.entity.Project;
import com.smartfreelance.entity.Role;
import com.smartfreelance.entity.Skill;
import com.smartfreelance.entity.User;
import com.smartfreelance.repository.CategoryRepository;
import com.smartfreelance.repository.ProjectRepository;
import com.smartfreelance.repository.SkillRepository;
import com.smartfreelance.repository.UserRepository;
import com.smartfreelance.security.CurrentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private SkillRepository skillRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private ProjectService projectService;

    private User clientUser;
    private User adminUser;
    private Project openProject;

    @BeforeEach
    void setUp() {
        clientUser = new User(1L, "Client User", "client@example.com", "pass", Role.CLIENT, true);
        adminUser = new User(2L, "System Admin", "admin@example.com", "pass", Role.ADMIN, true);
        openProject = new Project(10L, "Java Web App", "Build Spring Boot backend", "1500.00", "2026-12-31", "OPEN", 1L, null);
    }

    @Test
    void testCreateProject_Success() {
        when(currentUserService.getCurrentUser()).thenReturn(clientUser);
        when(userRepository.existsById(1L)).thenReturn(true);
        when(projectRepository.save(any(Project.class))).thenAnswer(inv -> {
            Project p = inv.getArgument(0);
            p.setId(10L);
            return p;
        });

        ProjectRequest request = new ProjectRequest("Java Web App", "Build Spring Boot backend", "1500.00", "2026-12-31", 1L);
        ProjectResponse response = projectService.createProject(request);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("Java Web App", response.getTitle());
        assertEquals("OPEN", response.getStatus());
    }

    @Test
    void testGetProjectsPaginated_DefaultStatusIsOpen() {
        when(currentUserService.getCurrentUser()).thenReturn(clientUser);
        Page<Project> page = new PageImpl<>(List.of(openProject));
        when(projectRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        PageResponse<ProjectResponse> response = projectService.getProjectsPaginated(
                null, null, null, null, null, null, 0, 10, "id", "asc");

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals("OPEN", response.getContent().get(0).getStatus());
    }

    @Test
    void testGetProjectsPaginated_KeywordSearch() {
        when(currentUserService.getCurrentUser()).thenReturn(clientUser);
        Page<Project> page = new PageImpl<>(List.of(openProject));
        when(projectRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        PageResponse<ProjectResponse> response = projectService.getProjectsPaginated(
                "Spring", null, null, null, null, "OPEN", 0, 10, "id", "asc");

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
    }

    @Test
    void testGetProjectsPaginated_CategoryFilter() {
        when(currentUserService.getCurrentUser()).thenReturn(clientUser);
        openProject.setCategoryId(5L);
        Page<Project> page = new PageImpl<>(List.of(openProject));
        when(projectRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        PageResponse<ProjectResponse> response = projectService.getProjectsPaginated(
                null, 5L, null, null, null, "OPEN", 0, 10, "id", "asc");

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
    }

    @Test
    void testGetProjectsPaginated_SkillFilter() {
        when(currentUserService.getCurrentUser()).thenReturn(clientUser);
        Skill skill = new Skill(2L, "Java", true);
        openProject.setSkills(List.of(skill));
        Page<Project> page = new PageImpl<>(List.of(openProject));
        when(projectRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        PageResponse<ProjectResponse> response = projectService.getProjectsPaginated(
                null, null, 2L, null, null, "OPEN", 0, 10, "id", "asc");

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
    }

    @Test
    void testGetProjectsPaginated_MinAndMaxBudgetFilter() {
        when(currentUserService.getCurrentUser()).thenReturn(clientUser);
        Page<Project> page = new PageImpl<>(List.of(openProject));
        when(projectRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        PageResponse<ProjectResponse> response = projectService.getProjectsPaginated(
                null, null, null, 1000.0, 2000.0, "OPEN", 0, 10, "id", "asc");

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
    }

    @Test
    void testGetProjectsPaginated_NonAdminRequestingNonOpenStatus_ThrowsAccessDeniedException() {
        when(currentUserService.getCurrentUser()).thenReturn(clientUser);

        assertThrows(AccessDeniedException.class, () ->
                projectService.getProjectsPaginated(null, null, null, null, null, "COMPLETED", 0, 10, "id", "asc"));

        assertThrows(AccessDeniedException.class, () ->
                projectService.getProjectsPaginated(null, null, null, null, null, "IN_PROGRESS", 0, 10, "id", "asc"));

        assertThrows(AccessDeniedException.class, () ->
                projectService.getProjectsPaginated(null, null, null, null, null, "ALL", 0, 10, "id", "asc"));
    }

    @Test
    void testGetProjectsPaginated_AdminRequestingNonOpenStatus_Success() {
        when(currentUserService.getCurrentUser()).thenReturn(adminUser);
        Project completedProject = new Project(11L, "Completed App", "Done", "2000.00", "2026-05-01", "COMPLETED", 1L, 3L);
        Page<Project> page = new PageImpl<>(List.of(completedProject));
        when(projectRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        PageResponse<ProjectResponse> response = projectService.getProjectsPaginated(
                null, null, null, null, null, "COMPLETED", 0, 10, "id", "asc");

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals("COMPLETED", response.getContent().get(0).getStatus());
    }

    @Test
    void testGetProjectsPaginated_DisallowedSortFieldClientId_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                projectService.getProjectsPaginated(null, null, null, null, null, "OPEN", 0, 10, "clientId", "asc"));

        assertThrows(IllegalArgumentException.class, () ->
                projectService.getProjectsPaginated(null, null, null, null, null, "OPEN", 0, 10, "freelancerId", "asc"));
    }

    @Test
    void testGetProjectsPaginated_EmptyResults() {
        when(currentUserService.getCurrentUser()).thenReturn(clientUser);
        Page<Project> emptyPage = new PageImpl<>(Collections.emptyList());
        when(projectRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(emptyPage);

        PageResponse<ProjectResponse> response = projectService.getProjectsPaginated(
                "NonExistentKeyword", 99L, 88L, 5000.0, 10000.0, "OPEN", 0, 10, "id", "asc");

        assertNotNull(response);
        assertTrue(response.getContent().isEmpty());
        assertEquals(0, response.getTotalElements());
    }

    @Test
    void testGetProjectsPaginated_InvalidBudgetRange_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                projectService.getProjectsPaginated(null, null, null, 2000.0, 1000.0, "OPEN", 0, 10, "id", "asc"));

        assertThrows(IllegalArgumentException.class, () ->
                projectService.getProjectsPaginated(null, null, null, -100.0, 500.0, "OPEN", 0, 10, "id", "asc"));
    }

    @Test
    void testGetProjectsPaginated_InvalidPagination_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                projectService.getProjectsPaginated(null, null, null, null, null, "OPEN", -1, 10, "id", "asc"));

        assertThrows(IllegalArgumentException.class, () ->
                projectService.getProjectsPaginated(null, null, null, null, null, "OPEN", 0, 0, "id", "asc"));

        assertThrows(IllegalArgumentException.class, () ->
                projectService.getProjectsPaginated(null, null, null, null, null, "OPEN", 0, 100, "id", "asc"));
    }

    @Test
    void testGetProjectsPaginated_InvalidSortField_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                projectService.getProjectsPaginated(null, null, null, null, null, "OPEN", 0, 10, "unsupportedField", "asc"));
    }

    @Test
    void testGetProjectsPaginated_InvalidStatus_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                projectService.getProjectsPaginated(null, null, null, null, null, "INVALID_STATUS", 0, 10, "id", "asc"));
    }
}

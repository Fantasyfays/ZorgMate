package com.example.zorgmate;

import com.example.zorgmate.dal.entity.Invoice.TimeEntry;
import com.example.zorgmate.dal.entity.Project.Project;
import com.example.zorgmate.dal.repository.ProjectRepository;
import com.example.zorgmate.dal.repository.TimeEntryRepository;
import com.example.zorgmate.service.impl.ProjectServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProjectServiceImplTest {

    @Mock
    private ProjectRepository projectRepo;

    @Mock
    private TimeEntryRepository timeEntryRepo;

    @InjectMocks
    private ProjectServiceImpl projectService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void isProjectOverHours_shouldReturnTrue_whenTotalHoursExceedLimit() {
        // Arrange
        Long projectId = 1L;
        Project project = Project.builder().id(projectId).agreedHoursLimit(8).build();

        List<TimeEntry> entries = List.of(
                TimeEntry.builder().hours(5).build(),
                TimeEntry.builder().hours(4).build()
        );

        when(projectRepo.findById(projectId)).thenReturn(Optional.of(project));
        when(timeEntryRepo.findByProjectId(projectId)).thenReturn(entries);

        // Act
        boolean result = projectService.isProjectOverHours(projectId);

        // Assert
        assertTrue(result);
    }

    @Test
    void isProjectOverHours_shouldReturnFalse_whenTotalHoursEqualLimit() {
        // Arrange
        Long projectId = 2L;
        Project project = Project.builder().id(projectId).agreedHoursLimit(10).build();

        List<TimeEntry> entries = List.of(
                TimeEntry.builder().hours(5).build(),
                TimeEntry.builder().hours(5).build()
        );

        when(projectRepo.findById(projectId)).thenReturn(Optional.of(project));
        when(timeEntryRepo.findByProjectId(projectId)).thenReturn(entries);

        // Act
        boolean result = projectService.isProjectOverHours(projectId);

        // Assert
        assertFalse(result);
    }

    @Test
    void isProjectOverHours_shouldReturnFalse_whenTotalHoursUnderLimit() {
        // Arrange
        Long projectId = 3L;
        Project project = Project.builder().id(projectId).agreedHoursLimit(10).build();

        List<TimeEntry> entries = List.of(
                TimeEntry.builder().hours(3).build(),
                TimeEntry.builder().hours(4).build()
        );

        when(projectRepo.findById(projectId)).thenReturn(Optional.of(project));
        when(timeEntryRepo.findByProjectId(projectId)).thenReturn(entries);

        // Act
        boolean result = projectService.isProjectOverHours(projectId);

        // Assert
        assertFalse(result);
    }

    @Test
    void isProjectOverHours_shouldReturnFalse_whenLimitIsNull() {
        // Arrange
        Long projectId = 4L;
        Project project = Project.builder().id(projectId).agreedHoursLimit(null).build();

        when(projectRepo.findById(projectId)).thenReturn(Optional.of(project));

        // Act
        boolean result = projectService.isProjectOverHours(projectId);

        // Assert
        assertFalse(result);
    }

    @Test
    void isProjectOverHours_shouldThrowException_whenProjectNotFound() {
        // Arrange
        Long projectId = 404L;
        when(projectRepo.findById(projectId)).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(RuntimeException.class, () -> projectService.isProjectOverHours(projectId));
    }
}

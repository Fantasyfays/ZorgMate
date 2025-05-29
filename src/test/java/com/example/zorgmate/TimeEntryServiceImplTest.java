package com.example.zorgmate;

import com.example.zorgmate.dal.entity.Client.Client;
import com.example.zorgmate.dal.entity.Invoice.TimeEntry;
import com.example.zorgmate.dal.entity.Project.Project;
import com.example.zorgmate.dal.repository.ClientRepository;
import com.example.zorgmate.dal.repository.ProjectRepository;
import com.example.zorgmate.dal.repository.TimeEntryRepository;
import com.example.zorgmate.dto.timeentry.TimeEntryCreateDTO;
import com.example.zorgmate.dto.timeentry.TimeEntryResponseDTO;
import com.example.zorgmate.service.impl.TimeEntryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TimeEntryServiceImplTest {

    @Mock private TimeEntryRepository timeEntryRepo;
    @Mock private ClientRepository clientRepo;
    @Mock private ProjectRepository projectRepo;
    @InjectMocks private TimeEntryServiceImpl timeEntryService;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createTimeEntry_shouldReturnDTO_whenClientAndProjectExist() {
        String username = "testuser";

        TimeEntryCreateDTO dto = TimeEntryCreateDTO.builder()
                .description("Consultatie")
                .hours(4)
                .hourlyRate(BigDecimal.valueOf(75))
                .date(LocalDate.of(2025, 5, 29))
                .clientId(1L)
                .projectId(10L)
                .build();

        Client client = Client.builder().id(1L).name("ZorgClient").build();
        Project project = Project.builder().id(10L).name("ZorgProject").build();

        when(clientRepo.findById(1L)).thenReturn(Optional.of(client));
        when(projectRepo.findById(10L)).thenReturn(Optional.of(project));
        when(timeEntryRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        TimeEntryResponseDTO result = timeEntryService.createTimeEntry(dto);

        assertEquals("Consultatie", result.getDescription());
        assertEquals(4, result.getHours());
        assertEquals(BigDecimal.valueOf(75), result.getHourlyRate());
        assertEquals("ZorgClient", result.getClientName());
        assertEquals("ZorgProject", result.getProjectName());
    }

    @Test
    void createTimeEntry_shouldHandleNullProject() {
        String username = "testuser";

        TimeEntryCreateDTO dto = TimeEntryCreateDTO.builder()
                .description("Evaluatie")
                .hours(2)
                .hourlyRate(BigDecimal.valueOf(100))
                .date(LocalDate.of(2025, 5, 29))
                .clientId(1L)
                .projectId(null)
                .build();

        Client client = Client.builder().id(1L).name("ClientX").build();

        when(clientRepo.findById(1L)).thenReturn(Optional.of(client));
        when(timeEntryRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        TimeEntryResponseDTO result = timeEntryService.createTimeEntry(dto);

        assertEquals("Evaluatie", result.getDescription());
        assertEquals("ClientX", result.getClientName());
        assertNull(result.getProjectName());
    }

    @Test
    void createTimeEntry_shouldThrow_whenClientNotFound() {
        TimeEntryCreateDTO dto = TimeEntryCreateDTO.builder()
                .description("Consult")
                .hours(1)
                .hourlyRate(BigDecimal.TEN)
                .date(LocalDate.now())
                .clientId(404L)
                .build();

        when(clientRepo.findById(404L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> timeEntryService.createTimeEntry(dto));
    }

    @Test
    void getAllTimeEntries_shouldReturnMappedDTOs() {
        String username = "user1";
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(username, null)
        );

        Client client = Client.builder().name("ClientA").build();
        Project project = Project.builder().name("ProjectA").build();

        TimeEntry entry = TimeEntry.builder()
                .id(1L)
                .description("Advies")
                .hours(3)
                .hourlyRate(BigDecimal.valueOf(50))
                .date(LocalDate.of(2025, 5, 29))
                .client(client)
                .project(project)
                .createdBy(username)
                .build();

        when(timeEntryRepo.findByCreatedBy(username)).thenReturn(List.of(entry));

        List<TimeEntryResponseDTO> result = timeEntryService.getAllTimeEntries();

        assertEquals(1, result.size());
        assertEquals("Advies", result.get(0).getDescription());
    }


    @Test
    void getUnbilledEntriesByClient_shouldReturnCorrectDTOs() {
        String username = "user2";
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(username, null)
        );

        Client client = Client.builder().name("ClientB").build();
        TimeEntry entry = TimeEntry.builder()
                .id(2L)
                .description("Analyse")
                .hours(5)
                .hourlyRate(BigDecimal.valueOf(120))
                .date(LocalDate.of(2025, 5, 28))
                .client(client)
                .project(null)
                .createdBy(username)
                .build();

        when(timeEntryRepo.findByClientIdAndInvoiceIsNullAndCreatedBy(1L, username)).thenReturn(List.of(entry));

        List<TimeEntryResponseDTO> result = timeEntryService.getUnbilledEntriesByClient(1L);

        assertEquals(1, result.size());
        assertEquals("Analyse", result.get(0).getDescription());
        assertEquals("ClientB", result.get(0).getClientName());
        assertNull(result.get(0).getProjectName());
    }

}

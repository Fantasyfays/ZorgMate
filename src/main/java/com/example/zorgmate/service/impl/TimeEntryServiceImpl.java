package com.example.zorgmate.service.impl;

import com.example.zorgmate.dal.entity.Client.Client;
import com.example.zorgmate.dal.entity.Invoice.TimeEntry;
import com.example.zorgmate.dal.entity.Project.Project;
import com.example.zorgmate.dal.repository.ClientRepository;
import com.example.zorgmate.dal.repository.ProjectRepository;
import com.example.zorgmate.dal.repository.TimeEntryRepository;
import com.example.zorgmate.dto.timeentry.TimeEntryCreateDTO;
import com.example.zorgmate.dto.timeentry.TimeEntryResponseDTO;
import com.example.zorgmate.security.AuthUtils;
import com.example.zorgmate.service.interfaces.TimeEntryService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TimeEntryServiceImpl implements TimeEntryService {

    private final TimeEntryRepository timeEntryRepo;
    private final ClientRepository clientRepo;
    private final ProjectRepository projectRepo;

    public TimeEntryServiceImpl(TimeEntryRepository timeEntryRepo, ClientRepository clientRepo, ProjectRepository projectRepo) {
        this.timeEntryRepo = timeEntryRepo;
        this.clientRepo = clientRepo;
        this.projectRepo = projectRepo;
    }

    @Override
    public TimeEntryResponseDTO createTimeEntry(TimeEntryCreateDTO dto) {
        String username = AuthUtils.getCurrentUsername();
        Client client = clientRepo.findById(dto.getClientId()).orElseThrow();
        Project project = dto.getProjectId() != null ? projectRepo.findById(dto.getProjectId()).orElse(null) : null;

        TimeEntry entry = TimeEntry.builder()
                .description(dto.getDescription())
                .hours(dto.getHours())
                .hourlyRate(dto.getHourlyRate())
                .date(dto.getDate())
                .client(client)
                .project(project)
                .createdBy(username)
                .build();

        timeEntryRepo.save(entry);
        return mapToDTO(entry);
    }

    @Override
    public List<TimeEntryResponseDTO> getAllTimeEntries() {
        String username = AuthUtils.getCurrentUsername();
        return timeEntryRepo.findByCreatedBy(username).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TimeEntryResponseDTO> getUnbilledEntriesByClient(Long clientId) {
        String username = AuthUtils.getCurrentUsername();
        return timeEntryRepo.findByClientIdAndInvoiceIsNullAndCreatedBy(clientId, username).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private TimeEntryResponseDTO mapToDTO(TimeEntry entry) {
        return TimeEntryResponseDTO.builder()
                .id(entry.getId())
                .description(entry.getDescription())
                .hours(entry.getHours())
                .hourlyRate(entry.getHourlyRate())
                .date(entry.getDate())
                .clientName(entry.getClient().getName())
                .projectName(entry.getProject() != null ? entry.getProject().getName() : null)
                .build();
    }

    @Override
    public List<TimeEntry> findByInvoiceId(Long invoiceId) {
        return timeEntryRepo.findByInvoiceId(invoiceId);
    }

}

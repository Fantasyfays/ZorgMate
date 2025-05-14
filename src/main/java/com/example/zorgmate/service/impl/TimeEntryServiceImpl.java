package com.example.zorgmate.service.impl;

import com.example.zorgmate.dal.entity.Client.Client;
import com.example.zorgmate.dal.entity.Invoice.TimeEntry;
import com.example.zorgmate.dal.entity.Project.Project;
import com.example.zorgmate.dal.repository.*;
import com.example.zorgmate.dto.timeentry.*;
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
        Client client = clientRepo.findById(dto.getClientId()).orElseThrow();
        Project project = dto.getProjectId() != null ? projectRepo.findById(dto.getProjectId()).orElse(null) : null;

        TimeEntry entry = TimeEntry.builder()
                .description(dto.getDescription())
                .hours(dto.getHours())
                .hourlyRate(dto.getHourlyRate())
                .date(dto.getDate())
                .client(client)
                .project(project)
                .build();

        timeEntryRepo.save(entry);

        return mapToDTO(entry);
    }

    @Override
    public List<TimeEntryResponseDTO> getAllTimeEntries() {
        return timeEntryRepo.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TimeEntryResponseDTO> getUnbilledEntriesByClient(Long clientId) {
        return timeEntryRepo.findByClientIdAndInvoiceIsNull(clientId).stream()
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
}

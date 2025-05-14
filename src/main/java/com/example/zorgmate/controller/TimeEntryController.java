package com.example.zorgmate.controller;

import com.example.zorgmate.dto.timeentry.TimeEntryCreateDTO;
import com.example.zorgmate.dto.timeentry.TimeEntryResponseDTO;
import com.example.zorgmate.service.interfaces.TimeEntryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/time-entries")
public class TimeEntryController {

    private final TimeEntryService timeEntryService;

    public TimeEntryController(TimeEntryService timeEntryService) {
        this.timeEntryService = timeEntryService;
    }

    @PostMapping
    public TimeEntryResponseDTO createTimeEntry(@Valid @RequestBody TimeEntryCreateDTO dto) {
        return timeEntryService.createTimeEntry(dto);
    }

    @GetMapping
    public List<TimeEntryResponseDTO> getAllTimeEntries() {
        return timeEntryService.getAllTimeEntries();
    }

    @GetMapping("/unbilled/{clientId}")
    public List<TimeEntryResponseDTO> getUnbilledByClient(@PathVariable Long clientId) {
        return timeEntryService.getUnbilledEntriesByClient(clientId);
    }
}

package com.example.zorgmate.service.impl;

import com.example.zorgmate.dal.entity.Project.Project;
import com.example.zorgmate.dal.repository.ProjectRepository;
import com.example.zorgmate.dal.repository.TimeEntryRepository;
import com.example.zorgmate.service.interfaces.ProjectService;
import org.springframework.stereotype.Service;

@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepo;
    private final TimeEntryRepository timeEntryRepo;

    public ProjectServiceImpl(ProjectRepository projectRepo, TimeEntryRepository timeEntryRepo) {
        this.projectRepo = projectRepo;
        this.timeEntryRepo = timeEntryRepo;
    }

    @Override
    public boolean isProjectOverHours(Long projectId) {
        Project project = projectRepo.findById(projectId).orElseThrow();
        if (project.getAgreedHoursLimit() == null) return false;

        int totalHours = timeEntryRepo.findByProjectId(projectId)
                .stream()
                .mapToInt(e -> e.getHours())
                .sum();

        return totalHours > project.getAgreedHoursLimit();
    }
}

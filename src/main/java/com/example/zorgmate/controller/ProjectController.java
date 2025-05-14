package com.example.zorgmate.controller;

import com.example.zorgmate.service.interfaces.ProjectService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping("/{id}/check-over-hours")
    public boolean isProjectOverHours(@PathVariable Long id) {
        return projectService.isProjectOverHours(id);
    }
}

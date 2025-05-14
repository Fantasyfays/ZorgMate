package com.example.zorgmate.dal.repository;

import com.example.zorgmate.dal.entity.Project.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}

package com.example.zorgmate.dal.repository;

import com.example.zorgmate.dal.entity.Client.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClientRepository extends JpaRepository<Client, Long> {
    List<Client> findByCreatedBy(String createdBy);
}

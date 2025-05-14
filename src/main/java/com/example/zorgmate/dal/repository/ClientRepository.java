package com.example.zorgmate.dal.repository;

import com.example.zorgmate.dal.entity.Client.Client;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, Long> {
}

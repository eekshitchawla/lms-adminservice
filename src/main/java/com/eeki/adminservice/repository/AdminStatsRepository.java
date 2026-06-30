package com.eeki.adminservice.repository;

import com.eeki.adminservice.entity.AdminStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminStatsRepository extends JpaRepository<AdminStats, Long> {
}

package com.example.workflow;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowRepository extends JpaRepository<WorkflowItem, Long> {
    List<WorkflowItem> findByArchivedFalseOrderByCreatedAtDesc();
    List<WorkflowItem> findByArchivedFalseAndAssigneeUsernameIgnoreCaseOrderByCreatedAtDesc(String username);
    boolean existsByTitleIgnoreCase(String title);
    List<WorkflowItem> findByArchivedTrueOrderByCreatedAtDesc();
}

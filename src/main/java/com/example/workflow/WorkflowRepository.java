package com.example.workflow;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;

@Repository
public class WorkflowRepository {
    private List<WorkflowItem> items = new ArrayList<>();
    private Long idCounter = 1L;

    public List<WorkflowItem> findAll() {
        return items;
    }

    public WorkflowItem save(WorkflowItem item) {
        item.setId(idCounter++);
        items.add(item);
        return item;
    }
}

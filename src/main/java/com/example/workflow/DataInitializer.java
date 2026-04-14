package com.example.workflow;

import java.time.LocalDate;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {
    private final AppUserRepository userRepository;
    private final WorkflowRepository workflowRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(AppUserRepository userRepository,
                           WorkflowRepository workflowRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.workflowRepository = workflowRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        AppUser manager = ensureUser("manager", "manager123", "Maya Manager", Role.MANAGER);
        AppUser employeeOne = ensureUser("justin", "employee123", "Justin Employee", Role.EMPLOYEE);
        AppUser employeeTwo = ensureUser("johnny", "employee123", "Johnny Employee", Role.EMPLOYEE);
        AppUser employeeThree = ensureUser("sevin", "employee123", "Sevin Employee", Role.EMPLOYEE);
        migrateLegacyCasey(employeeTwo, manager);

        if (workflowRepository.count() == 0) {
            workflowRepository.save(seedTask(
                    "Sprint 3 Demo Prep",
                    "Prepare the integrated sprint 3 demo workflow.",
                    "Workflow",
                    Priority.HIGH,
                    TaskType.STORY,
                    Status.IN_REVIEW,
                    manager,
                    employeeOne,
                    LocalDate.now().plusDays(3)));

            workflowRepository.save(seedTask(
                    "Fix task permissions",
                    "Verify managers and employees see the correct actions.",
                    "Platform",
                    Priority.MEDIUM,
                    TaskType.BUG,
                    Status.SUBMITTED,
                    manager,
                    employeeTwo,
                    LocalDate.now().plusDays(5)));

            workflowRepository.save(seedTask(
                    "Review employee dashboard",
                    "Check that employee task visibility only shows assigned work.",
                    "Platform",
                    Priority.LOW,
                    TaskType.TASK,
                    Status.SUBMITTED,
                    manager,
                    employeeThree,
                    LocalDate.now().plusDays(7)));
        }
    }

    private void migrateLegacyCasey(AppUser replacementAssignee, AppUser fallbackCreator) {
        userRepository.findByUsernameIgnoreCase("casey").ifPresent(legacyUser -> {
            for (WorkflowItem item : workflowRepository.findAll()) {
                boolean updated = false;
                if (item.getAssignee() != null
                        && item.getAssignee().getUsername().equalsIgnoreCase("casey")) {
                    item.setAssignee(replacementAssignee);
                    updated = true;
                }
                if (item.getCreatedBy() != null
                        && item.getCreatedBy().getUsername().equalsIgnoreCase("casey")) {
                    item.setCreatedBy(fallbackCreator);
                    updated = true;
                }
                if (updated) {
                    workflowRepository.save(item);
                }
            }
            userRepository.delete(legacyUser);
        });
    }

    private AppUser ensureUser(String username, String rawPassword, String displayName, Role role) {
        return userRepository.findByUsernameIgnoreCase(username)
                .orElseGet(() -> {
                    AppUser user = new AppUser();
                    user.setUsername(username);
                    user.setPassword(passwordEncoder.encode(rawPassword));
                    user.setDisplayName(displayName);
                    user.setRole(role);
                    return userRepository.save(user);
                });
    }

    private WorkflowItem seedTask(String title, String description, String project, Priority priority,
                                  TaskType taskType, Status status, AppUser createdBy, AppUser assignee,
                                  LocalDate dueDate) {
        WorkflowItem item = new WorkflowItem();
        item.setTitle(title);
        item.setDescription(description);
        item.setProject(project);
        item.setPriority(priority);
        item.setTaskType(taskType);
        item.setStatus(status);
        item.setCreatedBy(createdBy);
        item.setAssignee(assignee);
        item.setDueDate(dueDate);
        item.setCreatedAt(java.time.LocalDateTime.now());
        item.setArchived(false);
        return item;
    }
}

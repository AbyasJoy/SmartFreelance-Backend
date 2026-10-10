package com.smartfreelance.repository;

import com.smartfreelance.entity.Project;
import com.smartfreelance.entity.Skill;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ProjectSpecification {

    public static Specification<Project> filterProjects(
            String keyword,
            Long categoryId,
            Long skillId,
            Double minBudget,
            Double maxBudget,
            String status) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                query.distinct(true);
            }

            if (keyword != null && !keyword.trim().isEmpty()) {
                String searchPattern = "%" + keyword.trim().toLowerCase() + "%";
                Predicate titleMatch = cb.like(cb.lower(root.get("title")), searchPattern);
                Predicate descMatch = cb.like(cb.lower(root.get("description")), searchPattern);
                predicates.add(cb.or(titleMatch, descMatch));
            }

            if (categoryId != null) {
                predicates.add(cb.equal(root.get("categoryId"), categoryId));
            }

            if (skillId != null) {
                Join<Project, Skill> skillJoin = root.join("skills");
                predicates.add(cb.equal(skillJoin.get("id"), skillId));
            }

            if (minBudget != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("budget").as(Double.class), minBudget));
            }

            if (maxBudget != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("budget").as(Double.class), maxBudget));
            }

            if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status.trim())) {
                predicates.add(cb.equal(cb.upper(root.get("status")), status.trim().toUpperCase()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

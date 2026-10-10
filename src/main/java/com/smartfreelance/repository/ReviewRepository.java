package com.smartfreelance.repository;

import com.smartfreelance.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByProjectId(Long projectId);

    List<Review> findByRevieweeId(Long revieweeId);

    Optional<Review> findByProjectIdAndReviewerId(Long projectId, Long reviewerId);

    boolean existsByProjectIdAndReviewerId(Long projectId, Long reviewerId);
}

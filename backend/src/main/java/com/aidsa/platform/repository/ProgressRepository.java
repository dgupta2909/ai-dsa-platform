package com.aidsa.platform.repository;

import com.aidsa.platform.model.User;
import com.aidsa.platform.model.UserProblemProgress;
import com.aidsa.platform.model.Problem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProgressRepository extends JpaRepository<UserProblemProgress, Long> {

    Optional<UserProblemProgress> findByUserAndProblem(User user, Problem problem);

    List<UserProblemProgress> findByUser(User user);

    List<UserProblemProgress> findByUserAndStatus(
            User user,
            UserProblemProgress.Status status
    );

    long countByUserAndStatus(
            User user,
            UserProblemProgress.Status status
    );
}
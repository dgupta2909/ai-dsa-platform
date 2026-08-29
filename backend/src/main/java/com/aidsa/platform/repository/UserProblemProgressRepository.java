package com.aidsa.platform.repository;

import com.aidsa.platform.model.User;
import com.aidsa.platform.model.UserProblemProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserProblemProgressRepository
        extends JpaRepository<UserProblemProgress, Long> {

    Optional<UserProblemProgress> findByUserAndProblemId(
            User user,
            Long problemId);

    List<UserProblemProgress> findByUser(User user);

    long countByUserAndStatus(
            User user,
            UserProblemProgress.Status status);
}
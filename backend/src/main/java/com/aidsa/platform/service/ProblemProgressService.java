package com.aidsa.platform.service;

import com.aidsa.platform.dto.ProblemProgressResponse;
import com.aidsa.platform.exception.ResourceNotFoundException;
import com.aidsa.platform.model.Problem;
import com.aidsa.platform.model.User;
import com.aidsa.platform.model.UserProblemProgress;
import com.aidsa.platform.repository.ProblemRepository;
import com.aidsa.platform.repository.UserProblemProgressRepository;
import com.aidsa.platform.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class ProblemProgressService {

    private final UserProblemProgressRepository progressRepository;
    private final UserRepository userRepository;
    private final ProblemRepository problemRepository;

    public ProblemProgressService(
            UserProblemProgressRepository progressRepository,
            UserRepository userRepository,
            ProblemRepository problemRepository) {

        this.progressRepository = progressRepository;
        this.userRepository = userRepository;
        this.problemRepository = problemRepository;
    }

    @Transactional
    public ProblemProgressResponse markAttempted(Long userId, Long problemId) {

        User user = getUser(userId);
        Problem problem = getProblem(problemId);

        UserProblemProgress progress = progressRepository.findByUserAndProblemId(user, problemId)
                .orElseGet(() -> {
                    UserProblemProgress newProgress = new UserProblemProgress();

                    newProgress.setUser(user);
                    newProgress.setProblem(problem);
                    newProgress.setStatus(
                            UserProblemProgress.Status.ATTEMPTED);
                    newProgress.setAttempts(0);

                    return newProgress;
                });

        progress.setStatus(UserProblemProgress.Status.ATTEMPTED);
        progress.setAttempts(progress.getAttempts() + 1);
        progress.setLastAttemptedAt(OffsetDateTime.now());

        return ProblemProgressResponse.fromEntity(
                progressRepository.save(progress));
    }

    @Transactional
    public ProblemProgressResponse markSolved(Long userId, Long problemId) {

        User user = getUser(userId);
        Problem problem = getProblem(problemId);

        UserProblemProgress progress = progressRepository.findByUserAndProblemId(user, problemId)
                .orElseGet(() -> {
                    UserProblemProgress newProgress = new UserProblemProgress();

                    newProgress.setUser(user);
                    newProgress.setProblem(problem);
                    newProgress.setAttempts(0);

                    return newProgress;
                });

        progress.setStatus(UserProblemProgress.Status.SOLVED);

        if (progress.getSolvedAt() == null) {
            progress.setSolvedAt(OffsetDateTime.now());
        }

        progress.setLastAttemptedAt(OffsetDateTime.now());

        return ProblemProgressResponse.fromEntity(
                progressRepository.save(progress));
    }

    @Transactional(readOnly = true)
    public ProblemProgressResponse getProgress(
            Long userId,
            Long problemId) {

        User user = getUser(userId);

        return progressRepository
                .findByUserAndProblemId(user, problemId)
                .map(ProblemProgressResponse::fromEntity)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<ProblemProgressResponse> getUserProgress(Long userId) {

        User user = getUser(userId);

        return progressRepository.findByUser(user)
                .stream()
                .map(ProblemProgressResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public long getSolvedCount(Long userId) {

        User user = getUser(userId);

        return progressRepository.countByUserAndStatus(
                user,
                UserProblemProgress.Status.SOLVED);
    }

    private User getUser(Long userId) {

        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + userId));
    }

    private Problem getProblem(Long problemId) {

        return problemRepository.findById(problemId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Problem not found with id: " + problemId));
    }
}
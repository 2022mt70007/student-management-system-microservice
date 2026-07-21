package com.sms.auth.service;

import com.sms.auth.entity.User;
import com.sms.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthLockService {

    private final UserRepository userRepository;

    /**
     * Commits in its own transaction so a later login failure exception
     * cannot roll back the failed-attempt counter / lockout.
     *
     * @return attempts counted after this failure (maxAttempts if lock was applied)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int recordFailedLogin(Long userId, int maxAttempts, int lockMinutes) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        int attempts = user.getFailedLoginAttempts() + 1;
        if (attempts >= maxAttempts) {
            user.setAccountLockedUntil(LocalDateTime.now().plusMinutes(lockMinutes));
            user.setFailedLoginAttempts(0);
            userRepository.save(user);
            return maxAttempts;
        }

        user.setFailedLoginAttempts(attempts);
        userRepository.save(user);
        return attempts;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void clearLock(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setFailedLoginAttempts(0);
            user.setAccountLockedUntil(null);
            userRepository.save(user);
        });
    }
}

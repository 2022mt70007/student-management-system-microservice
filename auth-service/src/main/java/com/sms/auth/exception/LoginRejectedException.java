package com.sms.auth.exception;

import lombok.Getter;

@Getter
public class LoginRejectedException extends RuntimeException {

    private final boolean locked;
    private final int failedAttempts;
    private final int maxAttempts;
    private final int remainingAttempts;
    private final boolean warnLockout;

    public LoginRejectedException(
            String message,
            boolean locked,
            int failedAttempts,
            int maxAttempts,
            boolean warnLockout) {
        super(message);
        this.locked = locked;
        this.failedAttempts = failedAttempts;
        this.maxAttempts = maxAttempts;
        this.remainingAttempts = Math.max(0, maxAttempts - failedAttempts);
        this.warnLockout = warnLockout;
    }
}

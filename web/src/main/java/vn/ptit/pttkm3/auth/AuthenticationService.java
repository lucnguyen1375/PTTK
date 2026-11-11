package vn.ptit.pttkm3.auth;

import java.sql.SQLException;

public final class AuthenticationService {
    private final EmployeeAccountDao accounts;
    private final LoginAttemptLimiter limiter;

    public AuthenticationService(EmployeeAccountDao accounts, LoginAttemptLimiter limiter) {
        this.accounts = accounts;
        this.limiter = limiter;
    }

    public Result authenticate(String username, char[] password, String ip) throws SQLException {
        if (username == null || password == null || username.strip().isEmpty()
                || username.strip().length() > 120 || password.length == 0 || password.length > 1024) {
            return new Result(Status.INVALID_INPUT, 0, null);
        }
        String normalized = username.strip();
        long now = System.currentTimeMillis();
        long blocked = limiter.blockedSeconds(normalized, ip, now);
        if (blocked > 0) return new Result(Status.RATE_LIMITED, blocked, null);

        EmployeeAccountDao.AccountRecord account = accounts.findByUsername(normalized);
        String encoded = account == null ? null : account.passwordHash();
        boolean passwordMatches = PasswordHasher.verify(password, encoded);
        if (!passwordMatches || account.employee() == null) {
            limiter.recordFailure(normalized, ip, System.currentTimeMillis());
            return new Result(Status.INVALID_CREDENTIALS, 0, null);
        }
        limiter.clear(normalized, ip);
        return new Result(Status.AUTHENTICATED, 0, account.employee());
    }

    public enum Status { AUTHENTICATED, INVALID_INPUT, INVALID_CREDENTIALS, RATE_LIMITED }
    public record Result(Status status, long retryAfterSeconds, SessionEmployee employee) { }
}

package vn.ptit.pttkm3.auth;

public final class SessionEmployee {
    private final long id;
    private final String code;
    private final String fullName;
    private final String role;

    public SessionEmployee(long id, String code, String fullName, String role) {
        this.id = id;
        this.code = code;
        this.fullName = fullName;
        this.role = role;
    }

    public long getId() { return id; }
    public String getCode() { return code; }
    public String getFullName() { return fullName; }
    public String getRole() { return role; }
}

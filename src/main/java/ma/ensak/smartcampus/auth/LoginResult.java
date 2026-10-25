package ma.ensak.smartcampus.auth;

public enum LoginResult {
    SUCCESS,
    INVALID_CREDENTIALS,
    INVALID_EMAIL_FORMAT,
    INACTIVE_ACCOUNT,
    LOCKED_AND_LOGGED
}

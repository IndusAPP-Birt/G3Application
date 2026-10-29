package eus.birtlh.g3.user;

public final class UserValidationConstraints {

    public static final int MIN_NAME_LENGTH = 2;
    public static final int MAX_NAME_LENGTH = 100;
    public static final int MAX_EMAIL_LENGTH = 254;
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_PASSWORD_HASH_LENGTH = 255;

    public static final String EMAIL_REGEX =
            "^[A-Za-z0-9_+&*-]+(?:\\.[A-Za-z0-9_+&*-]+)*@"
                    + "(?:[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?\\.)+[A-Za-z]{2,}$";

    private UserValidationConstraints() {
    }
}

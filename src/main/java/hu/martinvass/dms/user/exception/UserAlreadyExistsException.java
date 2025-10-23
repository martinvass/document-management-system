package hu.martinvass.dms.user.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class UserAlreadyExistsException extends IllegalStateException {

    public UserAlreadyExistsException(String msg) {
        super(msg);
    }
}
package com.lxk.iotcore.exception;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.amazonaws.services.cognitoidentity.model.NotAuthorizedException;
import com.lxk.iotcore.R;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * @author https://github.com/103style
 * @date 2020/4/16 17:20
 */
public class IoTAuthException extends Throwable {
    public NotAuthorizedException authorizedException;

    public IoTAuthException(NotAuthorizedException authorizedException) {
        this.authorizedException = authorizedException;
    }

    private boolean check() {
        return authorizedException != null;
    }

    @Nullable
    @Override
    public String getMessage() {
        return check() ? authorizedException.getMessage() : super.getMessage();
    }

    @Nullable
    @Override
    public String getLocalizedMessage() {
        return check() ? authorizedException.getLocalizedMessage() : super.getLocalizedMessage();
    }

    @Nullable
    @Override
    public synchronized Throwable getCause() {
        return check() ? authorizedException.getCause() : super.getCause();
    }

    @NonNull
    @Override
    public synchronized Throwable initCause(@Nullable Throwable cause) {
        return check() ? authorizedException.initCause(cause) : super.initCause(cause);
    }

    @NonNull
    @Override
    public String toString() {
        return check() ? authorizedException.toString() : super.toString();
    }

    @Override
    public void printStackTrace() {
        if (check()) {
            authorizedException.printStackTrace();
        } else {
            super.printStackTrace();
        }
    }

    @Override
    public void printStackTrace(@NonNull PrintStream s) {
        if (check()) {
            authorizedException.printStackTrace(s);
        } else {
            super.printStackTrace(s);
        }
    }

    @Override
    public void printStackTrace(@NonNull PrintWriter s) {
        if (check()) {
            authorizedException.printStackTrace(s);
        } else {
            super.printStackTrace(s);
        }
    }

    @NonNull
    @Override
    public synchronized Throwable fillInStackTrace() {
        return check() ? authorizedException.fillInStackTrace() : super.fillInStackTrace();
    }

    @NonNull
    @Override
    public StackTraceElement[] getStackTrace() {
        return check() ? authorizedException.getStackTrace() : super.getStackTrace();
    }

    @Override
    public void setStackTrace(@NonNull StackTraceElement[] stackTrace) {
        if (check()) {
            authorizedException.setStackTrace(stackTrace);
        } else {
            super.setStackTrace(stackTrace);
        }
    }
}

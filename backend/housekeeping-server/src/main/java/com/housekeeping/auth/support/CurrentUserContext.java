package com.housekeeping.auth.support;

public final class CurrentUserContext {

    private static final ThreadLocal<SessionUser> HOLDER = new ThreadLocal<>();

    private CurrentUserContext() {
    }

    public static void set(SessionUser user) {
        HOLDER.set(user);
    }

    public static SessionUser get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }
}

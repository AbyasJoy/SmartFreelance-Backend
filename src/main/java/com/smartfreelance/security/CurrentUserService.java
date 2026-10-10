package com.smartfreelance.security;

import com.smartfreelance.entity.User;

public interface CurrentUserService {

    User getCurrentUser();

    Long getCurrentUserId();

    String getCurrentUserEmail();

    String getCurrentUserRole();
}

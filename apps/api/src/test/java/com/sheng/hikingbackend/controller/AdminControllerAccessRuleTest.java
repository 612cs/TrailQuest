package com.sheng.hikingbackend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

class AdminControllerAccessRuleTest {

    @Test
    void adminControllerShouldRequireAdminRoleAtClassLevel() {
        PreAuthorize annotation = AdminController.class.getAnnotation(PreAuthorize.class);

        assertNotNull(annotation);
        assertEquals("hasRole('ADMIN')", annotation.value());
    }
}

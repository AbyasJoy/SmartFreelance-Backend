package com.smartfreelance.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcOperations;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DatabaseSchemaInitializerTest {

    @Mock
    private JdbcOperations jdbcTemplate;

    @InjectMocks
    private DatabaseSchemaInitializer schemaInitializer;

    @Test
    @DisplayName("run executes primary CREATE TABLE IF NOT EXISTS DDL successfully")
    void testRun_PrimaryDdlSuccess() {
        doNothing().when(jdbcTemplate).execute(anyString());

        assertDoesNotThrow(() -> schemaInitializer.run());

        verify(jdbcTemplate, times(1)).execute(contains("CREATE TABLE IF NOT EXISTS project_skills"));
    }

    @Test
    @DisplayName("run triggers fallback DDL when primary execution throws exception")
    void testRun_FallbackDdlTriggeredOnException() {
        doThrow(new DataAccessException("Syntax error") {}).doNothing().when(jdbcTemplate).execute(anyString());

        assertDoesNotThrow(() -> schemaInitializer.run());

        verify(jdbcTemplate, times(2)).execute(anyString());
    }
}

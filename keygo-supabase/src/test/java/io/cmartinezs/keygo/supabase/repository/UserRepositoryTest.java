package io.cmartinezs.keygo.supabase.repository;

import io.cmartinezs.keygo.supabase.entity.UserEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for UserRepository
 * Pruebas unitarias para UserRepository
 *
 * @author cmartinezs
 * @version 1.0
 */
class UserRepositoryTest {

    @Test
    void shouldCreateUserEntity() {
        // Given
        UserEntity user = UserEntity.builder()
                .username("testuser")
                .email("test@example.com")
                .passwordHash("hashedpassword")
                .firstName("Test")
                .lastName("User")
                .build();

        // Then
        assertNotNull(user);
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("hashedpassword", user.getPasswordHash());
        assertEquals("Test", user.getFirstName());
        assertEquals("User", user.getLastName());
        assertTrue(user.getIsActive());
        assertFalse(user.getIsVerified());
    }
}



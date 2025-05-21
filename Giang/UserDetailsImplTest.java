package com.thanhtam.backend;

import com.thanhtam.backend.entity.Role;
import com.thanhtam.backend.entity.User;
import com.thanhtam.backend.service.UserDetailsImpl;
import com.thanhtam.backend.ultilities.ERole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserDetailsImplTest {

    private User mockUser;
    private Role roleAdmin;
    private UserDetailsImpl details;

    @BeforeEach
    void init() {
        /* ----- Tạo dữ liệu giả ----- */
        roleAdmin = new Role();
        roleAdmin.setName(ERole.ROLE_ADMIN);

        mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(1L);
        when(mockUser.getUsername()).thenReturn("johnDoe");
        when(mockUser.getEmail()).thenReturn("test@example.com");
        when(mockUser.getPassword()).thenReturn("hashed");
        when(mockUser.getRoles()).thenReturn(Set.of(roleAdmin));

        /* ----- Khởi tạo đối tượng kiểm thử ----- */
        details = UserDetailsImpl.build(mockUser);
    }

    /* ----------------- Nhóm kiểm thử getter ----------------- */
    @Nested
    @DisplayName("Getter methods")
    class GetterTests {

        // 10.1 Get the list of Authorities
        @Test
        void getAuthorities_shouldReturnMappedAuthorities() {
            List<GrantedAuthority> expected = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));

            assertEquals(expected, details.getAuthorities());
        }

        // (bonus) – nhiều quyền, không nằm trong danh sách 10.x nhưng giữ lại để branch‑coverage
        @Test
        void getAuthorities_withTwoRoles_returnsBoth() {
            Role roleStudent = new Role();
            roleStudent.setName(ERole.ROLE_STUDENT);

            when(mockUser.getRoles()).thenReturn(Set.of(roleAdmin, roleStudent));

            UserDetailsImpl twoRoles = UserDetailsImpl.build(mockUser);

            assertEquals(
                    Set.of(new SimpleGrantedAuthority("ROLE_ADMIN"),
                            new SimpleGrantedAuthority("ROLE_STUDENT")),
                    Set.copyOf(twoRoles.getAuthorities()));
        }

        // 10.2 Retrieve ID
        @Test
        void getId_shouldReturnUserId() {
            assertEquals(1L, details.getId());
        }

        // 10.3 Retrieve Email
        @Test
        void getEmail_shouldReturnEmail() {
            assertEquals("test@example.com", details.getEmail());
        }

        // 10.4 Retrieve Password
        @Test
        void getPassword_shouldReturnPassword() {
            assertEquals("hashed", details.getPassword());
        }

        // 10.5 Retrieve Username
        @Test
        void getUsername_shouldReturnUsername() {
            assertEquals("johnDoe", details.getUsername());
        }
    }

    /* -------------- Nhóm kiểm thử trạng thái tài khoản -------------- */
    @Nested
    @DisplayName("Account flags")
    class AccountFlagTests {

        // 10.6 isAccountNonExpired
        @Test
        void isAccountNonExpired_isAlwaysTrue() {
            assertTrue(details.isAccountNonExpired());
        }

        // 10.7 isAccountNonLocked
        @Test
        void isAccountNonLocked_isAlwaysTrue() {
            assertTrue(details.isAccountNonLocked());
        }

        // 10.8 isCredentialsNonExpired
        @Test
        void isCredentialsNonExpired_isAlwaysTrue() {
            assertTrue(details.isCredentialsNonExpired());
        }

        // 10.9 isEnabled
        @Test
        void isEnabled_isAlwaysTrue() {
            assertTrue(details.isEnabled());
        }

        // 10.10 equals() – same reference
        @Test
        void equals_sameReference_returnsTrue() {
            assertTrue(details.equals(details));
        }
    }

    /* ------------------- equals ------------------- */
    @Nested
    @DisplayName("equals()")
    class EqualsTests {

        // 10.13 equals() – same ID
        @Test
        void equals_sameId_returnsTrue() {
            UserDetailsImpl other = new UserDetailsImpl(1L, "other", "o@e.com", "pw", List.of());
            assertEquals(details, other);
        }

        // 10.14 equals() – different ID
        @Test
        void equals_differentId_returnsFalse() {
            UserDetailsImpl other = new UserDetailsImpl(2L, "johnDoe", "test@example.com", "hashed", List.of());
            assertNotEquals(details, other);
        }

        // 10.11 & 10.12 equals() – null object & different class
        @Test
        void equals_nullOrDifferentClass_returnsFalse() {
            assertNotEquals(details, null);      // 10.11
            assertNotEquals(details, "string"); // 10.12
        }
    }

    /* ------------------ build(User) static ------------------ */

    // 10.15 build(User user) – basic user
    @Test
    @DisplayName("build(User) phải map đúng các trường & authority")
    void build_mapsAllFieldsCorrectly() {
        assertAll(
                () -> assertEquals(1L, details.getId()),
                () -> assertEquals("johnDoe", details.getUsername()),
                () -> assertEquals("test@example.com", details.getEmail()),
                () -> assertEquals("hashed", details.getPassword()),
                () -> assertTrue(details.getAuthorities()
                        .contains(new SimpleGrantedAuthority("ROLE_ADMIN")))
        );

        verify(mockUser).getRoles();
        verify(mockUser).getId();
        verify(mockUser).getUsername();
        verify(mockUser).getEmail();
        verify(mockUser).getPassword();
        verifyNoMoreInteractions(mockUser);
    }

    // 10.16 build(User user) – user with no roles
    @Test
    void build_withEmptyRoles_resultsInEmptyAuthorities() {
        when(mockUser.getRoles()).thenReturn(Set.of());

        UserDetailsImpl noRole = UserDetailsImpl.build(mockUser);

        assertTrue(noRole.getAuthorities().isEmpty());
    }
}

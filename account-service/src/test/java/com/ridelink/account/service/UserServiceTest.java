package com.ridelink.account.service;

import com.ridelink.account.dto.UpdateProfileRequest;
import com.ridelink.account.dto.UpdateStatusRequest;
import com.ridelink.account.dto.UserProfileDto;
import com.ridelink.account.model.AccountStatus;
import com.ridelink.account.model.Role;
import com.ridelink.account.model.User;
import com.ridelink.account.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id("65fabc1234567890abcdef12")
                .email("driver@ridelink.com")
                .password("encodedPassword")
                .firstName("Jane")
                .lastName("Smith")
                .phoneNumber("+9876543210")
                .role(Role.DRIVER)
                .status(AccountStatus.ACTIVE)
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should retrieve user profile by valid user ID")
    void getUserById_Success() {
        when(userRepository.findById("65fabc1234567890abcdef12")).thenReturn(Optional.of(testUser));

        UserProfileDto profile = userService.getUserById("65fabc1234567890abcdef12");

        assertNotNull(profile);
        assertEquals("65fabc1234567890abcdef12", profile.getId());
        assertEquals("driver@ridelink.com", profile.getEmail());
        assertEquals("Jane", profile.getFirstName());
        assertEquals(Role.DRIVER, profile.getRole());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when user ID is not found")
    void getUserById_NotFound_ThrowsException() {
        when(userRepository.findById("invalidId")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.getUserById("invalidId")
        );

        assertTrue(exception.getMessage().contains("User not found"));
    }

    @Test
    @DisplayName("Should successfully update user profile details")
    void updateProfile_Success() {
        UpdateProfileRequest updateRequest = UpdateProfileRequest.builder()
                .firstName("Janet")
                .lastName("Smithson")
                .phoneNumber("+1122334455")
                .build();

        when(userRepository.findById("65fabc1234567890abcdef12")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserProfileDto updatedProfile = userService.updateProfile("65fabc1234567890abcdef12", updateRequest);

        assertNotNull(updatedProfile);
        assertEquals("Janet", updatedProfile.getFirstName());
        assertEquals("Smithson", updatedProfile.getLastName());
        assertEquals("+1122334455", updatedProfile.getPhoneNumber());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should successfully update user account status")
    void updateAccountStatus_Success() {
        UpdateStatusRequest statusRequest = UpdateStatusRequest.builder()
                .status(AccountStatus.SUSPENDED)
                .build();

        when(userRepository.findById("65fabc1234567890abcdef12")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserProfileDto updatedProfile = userService.updateAccountStatus("65fabc1234567890abcdef12", statusRequest);

        assertNotNull(updatedProfile);
        assertEquals(AccountStatus.SUSPENDED, updatedProfile.getStatus());
        assertFalse(updatedProfile.isActive());
    }
}

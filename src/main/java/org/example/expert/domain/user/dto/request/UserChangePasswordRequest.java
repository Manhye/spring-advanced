package org.example.expert.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.expert.domain.common.validation.ValidPassword;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserChangePasswordRequest {

    @NotBlank(message = "Enter the current password")
    private String oldPassword;

    @ValidPassword
    private String newPassword;
}

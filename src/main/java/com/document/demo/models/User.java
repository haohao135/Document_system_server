package com.document.demo.models;

import com.document.demo.models.enums.UserRole;
import com.document.demo.models.enums.UserStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "users")
public class User implements UserDetails {
    @Id
    private String userId;

    @NotBlank(message = "Username is required")
    @Indexed(unique = true)
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    @Builder.Default
    private String avatar = "https://res.cloudinary.com/dysmw04kc/image/upload/v1702052066/samples/balloons.jpg";

    @Builder.Default
    private String background = "https://res.cloudinary.com/dysmw04kc/image/upload/v1735927635/samples/landscapes/background.jpg";

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email is invalid")
    @Indexed(unique = true)
    private String email;

    @NotBlank(message = "Phone is required")
    private String phone;

    private String position;

    @Builder.Default
    @NotNull(message = "Role is required")
    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.USER;

    @Builder.Default
    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    private UserStatus status = UserStatus.INACTIVE;

    @DBRef
    private List<Folder> folders = new ArrayList<>();

    @DBRef
    private List<Documents> documents = new ArrayList<>();

    @DBRef
    private Department department;

    @DBRef
    private List<Distribution> sentDistributions = new ArrayList<>();

    @DBRef
    private List<Distribution> receivedDistributions = new ArrayList<>();

    @DBRef
    private List<Tracking> trackings = new ArrayList<>();

    // UserDetails implementation
    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return status != UserStatus.LOCKED;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return status == UserStatus.ACTIVE;
    }
}

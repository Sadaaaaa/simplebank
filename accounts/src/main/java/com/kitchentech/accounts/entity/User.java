package com.kitchentech.accounts.entity;

import com.kitchentech.accounts.config.StringListConverter;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@EntityListeners(AuditingEntityListener.class)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
    private String email;
    private String firstName;
    private String lastName;
    
    // Временно убираем конвертер для отладки
    // @Convert(converter = StringListConverter.class)
    // @ElementCollection(fetch = FetchType.EAGER)
    // private List<String> roles;
    private String roles;
    
    private Boolean enabled;
    private LocalDate birthDate;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;
    
    @Column(name = "deleted_by")
    private String deletedBy;
}

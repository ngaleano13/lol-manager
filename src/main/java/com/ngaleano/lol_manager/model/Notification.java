package com.ngaleano.lol_manager.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User recipient;

    private String message;
    private boolean isRead = false;
    private LocalDateTime createdAt = LocalDateTime.now();
    private String type;
    private Long referenceId;
}

package com.ngaleano.lol_manager.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

@Data
@Entity
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del equipo es requerido")
    private String name;

    @NotBlank(message = "El tag es requerido")
    @Size(min = 3, max = 5, message = "El tag debe tener entre 3 y 5 caracteres")
    private String tag;

    @OneToOne
    @JoinColumn(name = "leader_id")
    private Player leader;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Player> players;

    public boolean isFull() {
        return this.players != null && this.players.size() >= 6;
    }

}
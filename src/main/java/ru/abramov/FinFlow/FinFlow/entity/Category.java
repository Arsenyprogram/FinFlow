package ru.abramov.FinFlow.FinFlow.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.sql.Timestamp;


@Entity
@Table(name = "categories")
@Data
public class Category {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name")
    @NotNull
    private String name;

    @Column(name = "type")
    @NotNull
    private String type;

    @Column(name = "description")
    private String description;

    @Column(name = "created_at")
    private Timestamp created_at;

    @Column(name = "updated_at")
    private Timestamp updated_at;

    @ManyToOne
    @JoinColumn(name="user_id", referencedColumnName = "id")
    private Person user;

    public Category() {
    }

    public Category(String name, String description, String type, Timestamp created_at, Timestamp updated_at, Person user) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.created_at = created_at;
        this.updated_at = updated_at;
        this.user = user;
    }


}

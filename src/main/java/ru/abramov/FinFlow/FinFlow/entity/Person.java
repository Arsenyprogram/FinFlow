package ru.abramov.FinFlow.FinFlow.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.sql.Timestamp;


import java.util.List;

@Entity
@Table(name = "users")
@Data
public class Person {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name")
    @NotNull
    private String name;

    @Column(name = "first_name")
    @NotNull
    private String firstName;

    @Column(name = "last_name")
    @NotNull
    private String lastName;

    @Column(name = "phone_Number")
    @NotNull
    private String phoneNumber;

    @Column(name = "password")
    @NotNull
    private String password;

    @Column(name = "default_currency")
    private String defaultCurrency;

    @Column(name = "created_at")
    private Timestamp created_at;

    @Column(name = "updated_at")
    private Timestamp updated_at;

    @Column(name = "deleted")
    private Boolean deleted = false;

    @Column(name = "delete_reason")
    private String deleteReason;

    @Column(name = "deleted_at")
    private Timestamp deletedAt;

    @Column(name = "restore_deadline")
    private Timestamp restoreDeadline;

    @OneToMany(mappedBy = "user")
    private List<Category> categories;

    @OneToMany(mappedBy = "user")
    private List<Transaction> transactions;

    @PrePersist
    protected void onCreate() {
        created_at = new Timestamp(System.currentTimeMillis());
    }

    @PreUpdate
    protected void onUpdate() {
        updated_at = new Timestamp(System.currentTimeMillis());
    }

    public Person(String name, String password, String defaultCurrency) {
        this.name = name;
        this.password = password;
        this.defaultCurrency = defaultCurrency;
    }

    public Person(String name, String password) {
        this.name = name;
        this.password = password;
    }

    public Person() {
    }

}

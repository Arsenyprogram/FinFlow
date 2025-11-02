package ru.abramov.FinFlow.FinFlow.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.LocalDate;


@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "amount")
    @NotNull
    private Double amount;

    @Column(name = "type")
    @NotNull
    private String type;

    @Column(name = "date")
    @NotNull
    private LocalDate date;

    @Column(name ="createdat")
    private Timestamp createdAt;

    @Column(name = "updateat")
    private Timestamp updatedAt;


    @ManyToOne
    @JoinColumn(name="user_id", referencedColumnName="id")
    private Person user;

    @ManyToOne
    @JoinColumn(name = "category_id", referencedColumnName = "id")
    private Category category;




}

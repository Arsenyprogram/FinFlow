package ru.abramov.FinFlow.FinFlow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.abramov.FinFlow.FinFlow.entity.Budget;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepositories extends JpaRepository<Budget, Long> {
    @Query("SELECT b FROM Budget b JOIN FETCH b.category WHERE b.person.id = :personId")
    List<Budget> findAllByPersonId(Integer personId);

    @Query("SELECT b FROM Budget b JOIN FETCH b.category WHERE b.id = :id AND b.person.id = :personId")
    Optional<Budget> findByPersonIdAndId(
            @Param("personId") Integer personId, // :personId → personId
            @Param("id") Long budgetId           // :id → budgetId
    );

}

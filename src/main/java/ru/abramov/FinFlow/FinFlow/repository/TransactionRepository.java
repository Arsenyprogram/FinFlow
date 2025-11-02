package ru.abramov.FinFlow.FinFlow.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Repository;
import ru.abramov.FinFlow.FinFlow.dto.Analytics.DateSumProjection;
import ru.abramov.FinFlow.FinFlow.dto.Analytics.ExpenseByCategoryDTO;
import ru.abramov.FinFlow.FinFlow.dto.Analytics.MonthlySumProjection;
import ru.abramov.FinFlow.FinFlow.entity.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findAllByUserId(int userId);

    @Query("""
        SELECT COALESCE(SUM(
            CASE WHEN t.type = :income THEN t.amount
                 WHEN t.type = :expense THEN -t.amount
                 ELSE 0 END
        ), 0)
        FROM Transaction t
        WHERE (:userId IS NULL OR t.user.id = :userId)
          AND t.date < :start
        """)
    BigDecimal sumBefore(int userId, LocalDate start, String income, String expense);




    @Query("""
        SELECT t.date as date, COALESCE(SUM(
            CASE WHEN t.type = :income THEN t.amount
                 WHEN t.type = :expense THEN -t.amount
                 ELSE 0 END
        ), 0) as sum
        FROM Transaction t
        WHERE (:userId IS NULL OR t.user.id = :userId)
          AND t.date BETWEEN :start AND :end
        GROUP BY t.date
        ORDER BY t.date
        """)
    List<DateSumProjection> sumByDateBetween(int userId,
                                             LocalDate start,
                                             LocalDate end,
                                             String income,
                                             String expense);

    @Query("SELECT new ru.abramov.FinFlow.FinFlow.dto.Analytics.ExpenseByCategoryDTO(t.category.name, SUM(t.amount)) " +
            "FROM Transaction t " +
            "WHERE t.user.id = :userId " +
            "AND t.type = :expenseType " +
            "AND t.date >= :startDate AND t.date <= :endDate " +
            "GROUP BY t.category.name")
    List<ExpenseByCategoryDTO> findExpensesByCategory(
            @Param("userId") int userId,
            @Param("expenseType") String expenseType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );


    @Query(value = """
    SELECT TO_CHAR(date, 'YYYY-MM') AS month_name,
           SUM(amount) AS sum
    FROM transactions
    WHERE type = 'INCOME'
      AND EXTRACT(YEAR FROM date) = :year
      AND user_id = :userId
    GROUP BY month_name
    ORDER BY month_name
    """, nativeQuery = true)
    List<MonthlySumProjection> getMonthlyIncome(@Param("year") int year, @Param("userId") int userId);


    @Query(value = "SELECT TO_CHAR(date, 'YYYY-MM') as month_name, SUM(amount) as sum " +
            "FROM transactions " +
            "WHERE type = 'EXPENSE' AND EXTRACT(YEAR FROM date) = :year " +
            "AND user_id = :userId " +
            "GROUP BY month_name " +
            "ORDER BY month_name",
            nativeQuery = true)
    List<MonthlySumProjection> getMonthlyExpenses(@Param("year") int year, @Param("userId") int userId);



    @Query("SELECT COALESCE(SUM(t.amount), 0) " +
            "FROM Transaction t " +
            "WHERE t.type = 'INCOME' " +
            "AND FUNCTION('TO_CHAR', t.date, 'YYYY') = :year " +
            "AND FUNCTION('TO_CHAR', t.date, 'MM') = :month " +
            "AND t.user.id = :userId")
    BigDecimal getMonthlyIncome(@Param("year") String year, @Param("month") String month, @Param("userId") int userId);


    @Query("SELECT COALESCE(SUM(t.amount), 0) " +
            "FROM Transaction t " +
            "WHERE t.type = 'EXPENSE' " +
            "AND FUNCTION('TO_CHAR', t.date, 'YYYY') = :year " +
            "AND FUNCTION('TO_CHAR', t.date, 'MM') = :month " +
            "AND t.user.id = :userId")
    BigDecimal getMonthlyExpenses(@Param("year") String year, @Param("month") String month, @Param("userId") int userId);


    @Query("SELECT t FROM Transaction t " +
            "WHERE t.type = 'EXPENSE' " +
            "AND FUNCTION('TO_CHAR', t.date, 'YYYY-MM') = :month " +
            "AND t.user.id = :userId " +
            "ORDER BY t.amount DESC")
    List<Transaction> findLargestTransaction(@Param("month") String month, @Param("userId") int userId, Pageable pageable);


    @Query(value = "SELECT c.name " +
            "FROM transactions t " +
            "JOIN categories c ON t.category_id = c.id " +
            "WHERE t.type = 'EXPENSE' " +
            "AND TO_CHAR(t.date, 'YYYY-MM') = :month " +
            "AND t.user_id = :userId " +
            "GROUP BY c.name " +
            "ORDER BY SUM(t.amount) DESC " +
            "LIMIT 1",
            nativeQuery = true)
    List<String> findMostExpensiveCategory(@Param("month") String month, @Param("userId") int userId);


    @Query("SELECT COALESCE(SUM(t.amount), 0) " +
            "FROM Transaction t " +
            "WHERE t.type = 'EXPENSE' " +
            "AND t.user.id = :userId " +
            "AND t.category.id = :categoryId " +
            "AND t.date BETWEEN :startDate AND :endDate")
    BigDecimal getTotalExpensesByCategoryAndPeriod(@Param("userId") Integer userId,
                                                   @Param("categoryId") Integer categoryId,
                                                   @Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);


}

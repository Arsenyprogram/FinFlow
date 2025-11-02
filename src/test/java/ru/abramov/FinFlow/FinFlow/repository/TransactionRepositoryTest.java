package ru.abramov.FinFlow.FinFlow.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.abramov.FinFlow.FinFlow.dto.Analytics.DateSumProjection;
import ru.abramov.FinFlow.FinFlow.dto.Analytics.ExpenseByCategoryDTO;
import ru.abramov.FinFlow.FinFlow.dto.Analytics.MonthlySumProjection;
import ru.abramov.FinFlow.FinFlow.entity.Category;
import ru.abramov.FinFlow.FinFlow.entity.Person;
import ru.abramov.FinFlow.FinFlow.entity.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Transaction transaction;
    private Transaction transaction2;
    private Transaction transaction3;

    private Person person;

    @BeforeEach
    void setUp(){
        person = new Person();
        person.setName("Test");
        person.setPhoneNumber("1234567890");
        person.setFirstName("Test");
        person.setLastName("Test");
        person.setPassword("12345");



        transaction = new Transaction();
        transaction.setAmount(1000.0);
        transaction.setType("INCOME");
        transaction.setDate(LocalDate.of(2025, 1, 10));
        transaction.setUser(person);

        transaction2 = new Transaction();
        transaction2.setAmount(1000.0);
        transaction2.setType("INCOME");
        transaction2.setDate(LocalDate.of(2025, 1, 9));
        transaction2.setUser(person);

        transaction3 = new Transaction();
        transaction3.setAmount(490.0);
        transaction3.setType("EXPENSE");
        transaction3.setDate(LocalDate.of(2025, 1, 9));
        transaction3.setUser(person);

        person.setTransactions(List.of(transaction, transaction2, transaction3));
        personRepository.save(person);

        transactionRepository.save(transaction2);
        transactionRepository.save(transaction3);
        transactionRepository.save(transaction);



    }


    @Test
    @DisplayName("Тест поиска всех транзакции по id пользователя")
    void findAllByUserId() {
        List<Transaction> result = transactionRepository.findAllByUserId(person.getId());
        assertFalse(result.isEmpty());
        assertTrue(result.stream().allMatch(t -> t.getUser().getId() == person.getId()));
        assertEquals(3, result.size());

    }

    @Test
    void sumBefore() {
        BigDecimal result = transactionRepository.sumBefore(person.getId(), LocalDate.of(2025, 1, 11), "INCOME", "EXPENSE");
        assertNotNull(result);
        assertEquals(0, result.compareTo(BigDecimal.valueOf(1510.0)));

    }

    @Test
    void sumByDateBetween() {
        List<DateSumProjection> result = transactionRepository.sumByDateBetween(person.getId(), LocalDate.of(2025, 1, 9), LocalDate.of(2025, 1, 11), "INCOME", "EXPENSE");
        assertNotNull(result);
        assertEquals(2, result.size(), "Должно быть 2 разных даты");

        DateSumProjection first = result.get(0);
        assertEquals(LocalDate.of(2025, 1, 9), first.getDate());
        assertEquals(0, first.getSum().compareTo(BigDecimal.valueOf(510.0)));

        DateSumProjection second = result.get(1);
        assertEquals(LocalDate.of(2025, 1, 10), second.getDate());
        assertEquals(0, second.getSum().compareTo(BigDecimal.valueOf(1000.0)));

    }


    @Test
    @DisplayName("Проверка метода findExpensesByCategory")
    void findExpensesByCategory() {
        // Создаём категории
        Category catFood = new Category();
        catFood.setName("Food");
        catFood.setType("EXPENSE");

        Category catTransport = new Category();
        catTransport.setName("Transport");
        catTransport.setType("EXPENSE");

        // Сохраняем категории
        categoryRepository.save(catFood);
        categoryRepository.save(catTransport);

        // Привязываем категории к расходным транзакциям
        transaction3.setCategory(catFood); // 490
        Transaction transaction4 = new Transaction();
        transaction4.setAmount(300.0);
        transaction4.setType("EXPENSE");
        transaction4.setDate(LocalDate.of(2025, 1, 10));
        transaction4.setUser(person);
        transaction4.setCategory(catTransport);

        // Сохраняем новую транзакцию
        transactionRepository.save(transaction3);
        transactionRepository.save(transaction4);

        // Вызываем метод
        List<ExpenseByCategoryDTO> result = transactionRepository.findExpensesByCategory(
                person.getId(),
                "EXPENSE",
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 31)
        );

        // Проверки
        assertNotNull(result);
        assertEquals(2, result.size(), "Должны вернуться 2 категории расходов");

        ExpenseByCategoryDTO food = result.stream()
                .filter(r -> r.getCategory().equals("Food"))
                .findFirst()
                .orElseThrow();
        assertEquals(0, food.getTotal().compareTo(BigDecimal.valueOf(490.0)));

        ExpenseByCategoryDTO transport = result.stream()
                .filter(r -> r.getCategory().equals("Transport"))
                .findFirst()
                .orElseThrow();
        assertEquals(0, transport.getTotal().compareTo(BigDecimal.valueOf(300.0)));
    }



    @Test
    void getMonthlyIncome() {
        Transaction newTransaction = new Transaction();
        newTransaction.setAmount(300.0);
        newTransaction.setType("INCOME");
        newTransaction.setDate(LocalDate.of(2025, 2, 10));
        newTransaction.setUser(person);
        transactionRepository.save(newTransaction);

        List<MonthlySumProjection> result = transactionRepository.getMonthlyIncome(2025, person.getId());
        assertNotNull(result);
        assertEquals(2, result.size(), "Должно быть 2 месяца");

        assertEquals(300, result.get(1).getSum());
        assertEquals(2000, result.get(0).getSum());

    }

    @Test
    void getMonthlyExpenses() {
        Transaction newTransaction = new Transaction();
        newTransaction.setAmount(300.0);
        newTransaction.setType("EXPENSE");
        newTransaction.setDate(LocalDate.of(2025, 2, 10));
        newTransaction.setUser(person);
        transactionRepository.save(newTransaction);

        List<MonthlySumProjection> result = transactionRepository.getMonthlyExpenses(2025, person.getId());
        assertNotNull(result);
        assertEquals(2, result.size(), "Должно быть 2 месяца");

        assertEquals(300, result.get(1).getSum());
        assertEquals(490, result.get(0).getSum());

    }

    @Test
    void testGetMonthlyIncome() {
        BigDecimal result = transactionRepository.getMonthlyIncome("2025", "01", person.getId());
        assertEquals(0, result.compareTo(BigDecimal.valueOf(2000)));
    }

    @Test
    void testGetMonthlyExpenses() {
        BigDecimal result = transactionRepository.getMonthlyExpenses("2025", "01", person.getId());
        assertEquals(0, result.compareTo(BigDecimal.valueOf(490)));
    }

    @Test
    void findLargestTransaction() {
        Transaction newTransaction = new Transaction();
        newTransaction.setAmount(12000.0);
        newTransaction.setType("EXPENSE");
        newTransaction.setDate(LocalDate.of(2025, 1, 19));
        newTransaction.setUser(person);
        transactionRepository.save(newTransaction);

        Pageable limitOne = PageRequest.of(0, 1);

        List<Transaction> result = transactionRepository.findLargestTransaction("2025-01", person.getId(), limitOne);
        assertEquals(1, result.size());
        assertEquals(12000, result.get(0).getAmount());
    }

    @Test
    void findMostExpensiveCategory() {
        Category catFood = new Category();
        catFood.setName("Food");
        catFood.setType("EXPENSE");

        Category catTransport = new Category();
        catTransport.setName("Transport");
        catTransport.setType("EXPENSE");

        Transaction transaction4 = new Transaction();
        transaction4.setAmount(500.0);
        transaction4.setType("EXPENSE");
        transaction4.setDate(LocalDate.of(2025, 1, 9));
        transaction4.setUser(person);

        Transaction transaction5 = new Transaction();
        transaction5.setAmount(200.0);
        transaction5.setType("EXPENSE");
        transaction5.setDate(LocalDate.of(2025, 1, 15));
        transaction5.setUser(person);


        categoryRepository.save(catFood);
        categoryRepository.save(catTransport);
        transaction3.setCategory(catFood);
        transaction4.setCategory(catFood);
        transaction5.setCategory(catTransport);

        List<String> result = transactionRepository.findMostExpensiveCategory("2025-01", person.getId());
        assertEquals(1, result.size());
        assertEquals("Food", result.get(0));
    }

    @Test
    @DisplayName("Проверка метода getTotalExpensesByCategoryAndPeriod — корректный подсчёт суммы расходов по категории и периоду")
    void getTotalExpensesByCategoryAndPeriod() {
        // создаём категории
        Category catFood = new Category();
        catFood.setName("Food");
        catFood.setType("EXPENSE");

        Category catTransport = new Category();
        catTransport.setName("Transport");
        catTransport.setType("EXPENSE");

        categoryRepository.save(catFood);
        categoryRepository.save(catTransport);

        // привязываем категории к расходам
        transaction3.setCategory(catFood); // 490 (есть уже в базе)
        transactionRepository.save(transaction3);

        Transaction transaction4 = new Transaction();
        transaction4.setAmount(300.0);
        transaction4.setType("EXPENSE");
        transaction4.setDate(LocalDate.of(2025, 1, 15));
        transaction4.setUser(person);
        transaction4.setCategory(catTransport);
        transactionRepository.save(transaction4);

        Transaction transaction5 = new Transaction();
        transaction5.setAmount(210.0);
        transaction5.setType("EXPENSE");
        transaction5.setDate(LocalDate.of(2025, 1, 20));
        transaction5.setUser(person);
        transaction5.setCategory(catFood);
        transactionRepository.save(transaction5);

        // вызывем метод для категории "Food" в январе
        BigDecimal total = transactionRepository.getTotalExpensesByCategoryAndPeriod(
                person.getId(),
                catFood.getId(),
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 31)
        );

        assertNotNull(total);
        assertEquals(0, total.compareTo(BigDecimal.valueOf(700.0)),
                "Сумма по категории Food должна быть 700 (490 + 210)");
    }

    @Test
    @DisplayName("getTotalExpensesByCategoryAndPeriod должен вернуть 0, если нет транзакций")
    void getTotalExpensesByCategoryAndPeriod_shouldReturnZeroIfEmpty() {
        Category catEmpty = new Category();
        catEmpty.setName("EmptyCategory");
        catEmpty.setType("EXPENSE");
        categoryRepository.save(catEmpty);

        BigDecimal total = transactionRepository.getTotalExpensesByCategoryAndPeriod(
                person.getId(),
                catEmpty.getId(),
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 31)
        );

        assertNotNull(total);
        assertEquals(0, BigDecimal.ZERO.compareTo(total), "Если транзакций нет, должна возвращаться 0, а не null");
    }

}
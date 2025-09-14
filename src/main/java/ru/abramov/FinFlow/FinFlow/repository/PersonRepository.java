package ru.abramov.FinFlow.FinFlow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.abramov.FinFlow.FinFlow.entity.Person;

import java.util.Optional;

public interface PersonRepository extends JpaRepository<Person, Long> {
    Optional<Person> findByName(String username);
}

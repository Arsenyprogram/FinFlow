package ru.abramov.FinFlow.FinFlow.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import ru.abramov.FinFlow.FinFlow.entity.Person;

import java.util.Collection;

class PersonDetailsTest {

    @Test
    void testPersonDetails() {
        Person person = new Person();
        person.setName("Arseny");
        person.setPassword("secret");

        PersonDetails details = new PersonDetails(person);

        // Проверяем делегирование
        assertEquals("Arseny", details.getUsername());
        assertEquals("secret", details.getPassword());
        assertEquals(person, details.getUser());

        // Проверяем authorities
        Collection<? extends GrantedAuthority> authorities = details.getAuthorities();
        assertEquals(1, authorities.size());
        assertEquals("ROLE_USER", authorities.iterator().next().getAuthority());

        // Проверяем фиксированные методы
        assertTrue(details.isAccountNonExpired());
        assertTrue(details.isAccountNonLocked());
        assertTrue(details.isCredentialsNonExpired());
        assertTrue(details.isEnabled());
    }
}

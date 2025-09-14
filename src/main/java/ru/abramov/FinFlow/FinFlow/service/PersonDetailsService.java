package ru.abramov.FinFlow.FinFlow.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.abramov.FinFlow.FinFlow.entity.Person;
import ru.abramov.FinFlow.FinFlow.repository.PersonRepository;
import ru.abramov.FinFlow.FinFlow.security.PersonDetails;

import java.util.Optional;

@Service
@Transactional
public class PersonDetailsService implements UserDetailsService {
    private final PersonRepository userRepository;

    public PersonDetailsService(PersonRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
         Optional<Person> user = userRepository.findByName(username);
         if(user.isEmpty()) {
             throw new UsernameNotFoundException("Пользователь не найден ");
         }
         if (user.get().getDeleted().equals(true)) {
             throw new RuntimeException("Пользователь был удален");
         }
         return new PersonDetails(user.get());
    }
}

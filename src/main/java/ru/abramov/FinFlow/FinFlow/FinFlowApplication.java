package ru.abramov.FinFlow.FinFlow;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import ru.abramov.FinFlow.FinFlow.dto.Person.PersonUpdateDTO;
import ru.abramov.FinFlow.FinFlow.entity.Person;

@EnableCaching
@SpringBootApplication
@EnableAspectJAutoProxy
public class FinFlowApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinFlowApplication.class, args);
	}


	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	} // убрать в конфиг


	// openapi + генерация контроллеров при помощи плагина gradle/maven
	// docker compose
	// попробовать написать кастомную аннотация для логирования/  (aop)
	// написать ci cd базовый и попробовать развернуть на сервере прилож
	// тесты unit + integrations
	// добавить кэширование некоторых методов @cacheable (caffein)

	// будет круто сделать интеграцию с внешним сервисом


	// после всего этого можно попробовать:
	// партиционирование таблиц в postgres
	// подключить кафку (spring kafka)
	// попробовать развернуть всё в kubernetes (вместо docker compose)
	// посмотреть что такое grpc


	// попробовать написать многомодульный проект на gradle (2 сервиса - backend + dwh),
	// первый принимает запросы отгружает данные по кафке во второй,
	// второй принимает данные по кафке и сохраняет в партиционированные таблицы для дальнейшей аналитики
}

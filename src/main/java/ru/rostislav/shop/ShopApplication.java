package ru.rostislav.shop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Точка запуска Spring Boot и корень поиска компонентов интернет-магазина. */
@SpringBootApplication
public class ShopApplication {

    /**
     * Запускает приложение, встраиваемый HTTP-сервер и подключение к БД.
     *
     * @param args аргументы Spring Boot, например номер порта
     */
    public static void main(String[] args) {
        SpringApplication.run(ShopApplication.class, args);
    }
}

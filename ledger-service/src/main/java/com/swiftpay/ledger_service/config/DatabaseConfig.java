//package com.swiftpay.ledger_service.config;
//
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import java.sql.Connection;
//import java.sql.ResultSet;
//import java.sql.Statement;
//
//@Configuration
//public class DatabaseConfig {
//
//    @Bean
//    CommandLineRunner createDatabase(org.springframework.core.env.Environment env) {
//        return args -> {
//
//            String dbName = "mydbps";
//
//            String url = env.getProperty("spring.datasource.url");
//            String username = env.getProperty("spring.datasource.username");
//            String password = env.getProperty("spring.datasource.password");
//
//            try (Connection connection =
//                         java.sql.DriverManager.getConnection(url, username, password);
//                 Statement statement = connection.createStatement()) {
//
//                ResultSet rs = statement.executeQuery(
//                        "SELECT 1 FROM pg_database WHERE datname = '" + dbName + "'"
//                );
//
//                if (!rs.next()) {
//                    statement.executeUpdate("CREATE DATABASE " + dbName);
//                    System.out.println("Database created: " + dbName);
//                } else {
//                    System.out.println("Database already exists: " + dbName);
//                }
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        };
//    }
//}
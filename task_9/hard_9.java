/* Объектно-ориентированное программирование. Необходимо создать класс ArrayPI
(модификатор доступа - public), в котором необходимо создать два двухмерных массива (ввод с
клавиатуры, 7 столбцов и 7 строк). Далее необходимо создать классы-наследники (у всех - модификатор
доступа - public final, каждая операция с матрицами - отдельный класс-наследник), в которых будут
наследоваться данные матрицы и: перемножаться, складываться, вычитаться, возводиться в степень. После
выполнения каждого класса необходимо выводить итоговый результат.

Реализовать программу с интерактивным консольным меню.
Каждый пункт меню должен быть отдельным классом-наследником (подклассом).
1. Вывести все таблицы из базы данных MySQL.
2. Создать таблицу в базе данных MySQL.
3. Ввести две матрицы с клавиатуры и каждую из них сохранить в MySQL с последующим
форматированным выводом в консоль.
4. Перемножить, сложить, вычесть, возвести в степень матрицы, а также сохранить результаты в MySQL c выводом в консоль.
5. Сохранить результаты из MySQL в Excel и вывести их в консоль. */

package task_9;

import java.sql.*;
import java.util.Scanner;


public class hard_9 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;
        String tableName = "";

        String url = "jdbc:mysql://localhost:3305/";
        String dbName = "DB";
        String username = "root";
        String password = "root";

        try (Connection connection = DriverManager.getConnection(url + dbName, username, password)) {
            System.out.println("Подключение к базе данных успешно!");

            int[][] firstMatrix = null;
            int[][] secondMatrix = null;

            while (running) {
                System.out.println("1. Вывести все таблицы из MySQL.");
                System.out.println("2. Создать таблицу в MySQL.");
                System.out.println("3. Ввести две матрицы с клавиатуры и каждую из них сохранить в MySQL с последующим выводом в консоль.");
                System.out.println("4. Перемножить, сложить, вычесть, возвести в степень матрицы, а также сохранить результаты в MySQL\n" +
                        "и вывести в консоль.");
                System.out.println("5. Сохранить результаты из MySQL в Excel и вывести их в консоль.");
                System.out.println("0. Выйти из программы.");
                System.out.println("Выберите действие: ");
                int choice = scanner.nextInt();
                scanner.nextLine();

                switch (choice) {
                    case 1:
                        try (Statement statement = connection.createStatement()) {
                            statement.executeUpdate("USE " + dbName);
                            ResultSet resultSet = statement.executeQuery("SHOW TABLES");
                            System.out.println("Таблицы в базе данных:");
                            while (resultSet.next()) {
                                tableName = resultSet.getString(1);
                                System.out.println(tableName);
                            }
                        } catch (SQLException e) {
                            System.out.println("Ошибка при выполнении запроса: " + e.getMessage());
                        }
                        break;

                    case 2:
                        System.out.println("Введите название таблицы: ");
                        tableName = scanner.next();

                        String createTableQuery = "CREATE TABLE IF NOT EXISTS " + tableName +
                                "(id INT AUTO_INCREMENT PRIMARY KEY, value INT)";

                        try (Statement statement = connection.createStatement()) {
                            statement.executeUpdate(createTableQuery);
                            System.out.println("Таблица успешно создана.");
                        } catch (SQLException e) {
                            System.out.println("Ошибка при создании таблицы: " + e.getMessage());
                        }
                        break;

                    case 3:
                        System.out.println("Введите значения для первой матрицы (размер 7x7):");
                        firstMatrix = ArrayPI_9.readMatrixFromInput(scanner, 7, 7);
                        System.out.println("Первая матрица:");
                        MatrixMultiplier.printMatrix(firstMatrix);

                        System.out.println("Введите значения для второй матрицы (размер 7x7):");
                        secondMatrix = ArrayPI_9.readMatrixFromInput(scanner, 7, 7);
                        System.out.println("Вторая матрица:");
                        MatrixMultiplier.printMatrix(secondMatrix);

                        // Сохранение результатов в базу данных
                        ArrayPI_9.saveMatrixToDatabase(connection, firstMatrix, tableName + "_1");
                        ArrayPI_9.saveMatrixToDatabase(connection, secondMatrix, tableName + "_2");

                        break;

                    case 4:
                        if (firstMatrix != null && secondMatrix != null) {
                            // Сложение матриц
                            MatrixAdder adder = new MatrixAdder(firstMatrix, secondMatrix);
                            int[][] sumMatrix = adder.getResultMatrix();
                            adder.addAndPrintResult();
                            // Вычитание матриц
                            MatrixSubtractor subtractor = new MatrixSubtractor(firstMatrix, secondMatrix);
                            int[][] differenceMatrix = subtractor.getResultMatrix();
                            subtractor.subtractAndPrintResult();
                            // Умножение матриц
                            MatrixMultiplier multiplier = new MatrixMultiplier(firstMatrix, secondMatrix);
                            int[][] productMatrix = multiplier.getResultMatrix();
                            multiplier.multiplyAndPrintResult();
                            // Возведение матриц в степень
                            System.out.println("Введите степень для возведения матриц в эту степень:");
                            int power = scanner.nextInt();
                            scanner.nextLine();
                            MatrixPower powerer = new MatrixPower(firstMatrix, secondMatrix, power);
                            powerer.powerAndPrintResult();

                            // Сохранение результатов в Excel
                            ArrayPI_9.saveDataToExcel(tableName, firstMatrix, secondMatrix, sumMatrix, differenceMatrix,
                                    productMatrix, powerer.getResultMatrix1(), powerer.getResultMatrix2());
                        } else {
                            System.out.println("Пожалуйста, сначала введите значения для обеих матриц.");
                        }
                        break;

                    case 5:
                        System.out.println("Введите название файла для сохранения в Excel:");
                        String excelFileName = scanner.nextLine();
                        try {
                            Connection connection1 = DriverManager.getConnection(url, username, password);
                            ArrayPI_9.saveDataToExcel(excelFileName, firstMatrix, secondMatrix, null, null, null, null, null);
                        } catch (SQLException e) {
                            System.out.println("Ошибка при подключении к базе данных: " + e.getMessage());
                        }
                        break;


                    case 0:
                        running = false;
                        System.out.println("Программа завершена.");
                        break;

                    default:
                        System.out.println("Некорректный выбор. Попробуйте снова.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Ошибка подключения к базе данных: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }
}
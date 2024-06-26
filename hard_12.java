/* Объектно-ориентированное программирование и коллекции.
Создать класс Students с модификатором доступа public.
В данном классе создать метод, в котором добавляем поля с модификатором доступа private,
с их последующим вводом с клавиатуры:  ID студента; Направление подготовки студента; ФИО студента; Группа.
Далее создать класс-наследник, в котором мы переопределяем данный метод супер-класса и
создадим в нем отсортированный по алфавиту (по фамилии) список.
Необходимо сделать вывод в виде таблицы с помощью форматированных строк. Минимальное количество студентов 7.

Реализовать программу с интерактивным консольным меню.
Каждый пункт меню должен быть отдельным классом-наследником (подклассом).
1. Вывести все таблицы из базы данных MySQL.
2. Создать таблицу в базе данных MySQL.
3. Ввести данные о всех студентах и сохранить список в MySQL с последующим табличным
(форматированным) выводом в консоль.
4. Вывести данные о студенте по ID из MySQL.
5. Удалить данные о студенте из MySQL по ID.
6. Сохранить итоговые результаты из MySQL в Excel и вывести их в консоль. */

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.*;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class hard_12 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        String url = "jdbc:mysql://localhost:3305/";
        String dbName = "DB";
        String username = "root";
        String password = "root";

        Connection connection = null;
        try {
            connection = DriverManager.getConnection(url, username, password);
            System.out.println("Подключение к базе данных успешно!");
        } catch (SQLException e) {
            System.out.println("Ошибка подключения к базе данных: " + e.getMessage());
            return;
        }

        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate("USE " + dbName);
        } catch (SQLException e) {
            System.out.println("Ошибка при выборе базы данных: " + e.getMessage());
            return;
        }

        try {
            Statement statement = connection.createStatement();
            String createDBQuery = "CREATE DATABASE IF NOT EXISTS " + dbName;
            statement.executeUpdate(createDBQuery);
            System.out.println("База данных успешно создана или уже существует.");
        } catch (SQLException e) {
            System.out.println("Ошибка при создании базы данных: " + e.getMessage());
            return;
        }

        url += dbName;
        String studentTableName = "";
        List<Student> studentList = new ArrayList<>();

        while (running) {
            System.out.println("Выберите действие:");
            System.out.println("1. Вывести все таблицы из базы данных");
            System.out.println("2. Создать таблицу в базе данных");
            System.out.println("3. Ввести данные о студентах, отсортировать по фамилии и вывести список. Сохранить в MySQL и Excel.");
            System.out.println("4. Вывести данные о студенте по ID из MySQL.");
            System.out.println("5. Удалить данные о студенте из MySQL по ID.");
            System.out.println("6. Сохранить данные в Excel");
            System.out.println("0. Выход");
            try {
                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline character

                switch (choice) {
                    case 1:
                        try {
                            Statement statement = connection.createStatement();
                            ResultSet resultSet = statement.executeQuery("SHOW TABLES");
                            System.out.println("Таблицы в базе данных:");
                            while (resultSet.next()) {
                                String tableName = resultSet.getString(1);
                                System.out.println(tableName);
                            }
                        } catch (SQLException e) {
                            System.out.println("Ошибка при выполнении запроса: " + e.getMessage());
                        }
                        break;

                    case 2:
                        System.out.println("Введите название таблицы:");
                        String tableName = scanner.nextLine();
                        String createTableQuery = "CREATE TABLE IF NOT EXISTS " + tableName + " (id INT AUTO_INCREMENT PRIMARY KEY, faculty VARCHAR(255), fullName VARCHAR(255), group_name VARCHAR(255))";
                        try {
                            Statement statement = connection.createStatement();
                            statement.executeUpdate(createTableQuery);
                            System.out.println("Таблица успешно создана.");
                        } catch (SQLException e) {
                            System.out.println("Ошибка при создании таблицы: " + e.getMessage());
                        }
                        break;

                    case 3:
                        System.out.println("Введите название таблицы для ввода данных о студентах:");
                        studentTableName = scanner.nextLine();
                        studentList = Student.inputAndSortStudentData(connection, studentTableName, scanner);
                        Student.displayStudents(studentList);
                        break;

                    case 4:
                        System.out.println("Введите ID студента для поиска:");
                        int studentIdToFind = scanner.nextInt();
                        scanner.nextLine(); // Consume newline character
                        try {
                            String selectQuery = "SELECT * FROM " + studentTableName + " WHERE id = ?";
                            PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);
                            preparedStatement.setInt(1, studentIdToFind);
                            ResultSet resultSet = preparedStatement.executeQuery();
                            if (resultSet.next()) {
                                int id = resultSet.getInt("id");
                                String faculty = resultSet.getString("faculty");
                                String fullName = resultSet.getString("fullName");
                                String group = resultSet.getString("group_name");
                                System.out.printf("ID: %d, Факультет: %s, ФИО: %s, Группа: %s%n", id, faculty, fullName, group);
                            } else {
                                System.out.println("Студент с таким ID не найден.");
                            }
                        } catch (SQLException e) {
                            System.out.println("Ошибка при выполнении запроса: " + e.getMessage());
                        }
                        break;

                    case 5:
                        System.out.println("Введите ID студента для удаления:");
                        int studentIdToDelete = scanner.nextInt();
                        scanner.nextLine(); // Consume newline character
                        try {
                            String deleteQuery = "DELETE FROM " + studentTableName + " WHERE id = ?";
                            PreparedStatement preparedStatement = connection.prepareStatement(deleteQuery);
                            preparedStatement.setInt(1, studentIdToDelete);
                            int rowsAffected = preparedStatement.executeUpdate();
                            if (rowsAffected > 0) {
                                System.out.println("Студент с ID " + studentIdToDelete + " удален из БД.");
                                List<Student> updatedStudentList = new ArrayList<>(studentList);
                                updatedStudentList.removeIf(student -> student.getId() == studentIdToDelete);
                                studentList = updatedStudentList;
                            } else {
                                System.out.println("Студент с таким ID не найден.");
                            }
                        } catch (SQLException e) {
                            System.out.println("Ошибка при удалении данных: " + e.getMessage());
                        }
                        break;

                    case 6:
                        System.out.println("Введите название таблицы для сохранения данных:");
                        studentTableName = scanner.nextLine();
                        Student.saveToDatabase(connection, studentList, studentTableName);
                        SaveToExcel.saveResultsToExcel(studentList);
                        break;

                    case 0:
                        running = false;
                        break;

                    default:
                        System.out.println("Некорректный выбор. Попробуйте еще раз.");
                        break;
                }
            } catch (InputMismatchException e) {
                System.out.println("Некорректный ввод. Попробуйте еще раз.");
                scanner.next(); // Consume invalid input
            }
        }

        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при закрытии соединения: " + e.getMessage());
        }

        scanner.close();
    }
}



class SaveToExcel {
    public static void saveResultsToExcel(Connection connection, String studentTableName) {
        try {
            PreparedStatement selectStatement = connection.prepareStatement("SELECT * FROM " + studentTableName + " ORDER BY fullName");
            ResultSet resultSet = selectStatement.executeQuery();

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Students");

            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            Row headerRow = sheet.createRow(0);
            for (int i = 1; i <= columnCount; i++) {
                headerRow.createCell(i - 1).setCellValue(metaData.getColumnName(i));
            }

            int rowNum = 1;
            while (resultSet.next()) {
                Row row = sheet.createRow(rowNum++);
                for (int i = 1; i <= columnCount; i++) {
                    row.createCell(i - 1).setCellValue(resultSet.getString(i));
                }
            }

            for (int i = 0; i < columnCount; i++) {
                sheet.autoSizeColumn(i);
            }

            String excelFilePath = studentTableName + ".xlsx";
            try (FileOutputStream outputStream = new FileOutputStream(excelFilePath)) {
                workbook.write(outputStream);
            }

            workbook.close();

            System.out.println("Итоговые результаты сохранены в файл " + excelFilePath);
        } catch (SQLException | IOException e) {
            System.out.println("Ошибка при выполнении запроса или сохранении Excel-файла: " + e.getMessage());
        }
    }

    public static void saveResultsToExcel(List<Student> studentList) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Students");

        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("ID");
        headerRow.createCell(1).setCellValue("Факультет");
        headerRow.createCell(2).setCellValue("ФИО");
        headerRow.createCell(3).setCellValue("Группа");

        int rowNum = 1;
        for (Student student : studentList) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(student.getId());
            row.createCell(1).setCellValue(student.getFaculty());
            row.createCell(2).setCellValue(student.getFullName());
            row.createCell(3).setCellValue(student.getGroupName());
        }

        try {
            String excelFilePath = "students_data.xlsx";
            try (FileOutputStream outputStream = new FileOutputStream(excelFilePath)) {
                workbook.write(outputStream);
            }
            workbook.close();
            System.out.println("Итоговые результаты сохранены в файл " + excelFilePath);
        } catch (IOException e) {
            System.out.println("Ошибка при сохранении Excel-файла: " + e.getMessage());
        }
    }
}


class Student {
    private int id;
    private String faculty;
    private String fullName;
    private String groupName;

    public Student(int id, String faculty, String fullName, String groupName) {
        this.id = id;
        this.faculty = faculty;
        this.fullName = fullName;
        this.groupName = groupName;
    }

    public int getId() {
        return id;
    }

    public String getFaculty() {
        return faculty;
    }

    public String getFullName() {
        return fullName;
    }

    public String getGroupName() {
        return groupName;
    }

    public static List<Student> inputAndSortStudentData(Connection connection, String tableName, Scanner scanner) {
        boolean validInput = false;
        while (!validInput) {
            System.out.println("Введите количество студентов (минимум 7):");
            int numOfStudents;
            try {
                numOfStudents = Integer.parseInt(scanner.nextLine());
                if (numOfStudents < 7) {
                    System.out.println("Не менее 7 студентов!");
                    continue;
                }
            } catch (NumberFormatException e) {
                System.out.println("Ошибка! Попробуйте еще раз.");
                continue;
            }

            validInput = true;

            List<Student> studentList = new ArrayList<>();

            for (int i = 0; i < numOfStudents; i++) {
                System.out.println("Введите ID студента " + (i + 1) + ":");
                int id;
                try {
                    id = Integer.parseInt(scanner.nextLine());
                } catch (NumberFormatException e) {
                    System.out.println("Ошибка! Введите целое число. Попробуйте еще раз.");
                    i--;
                    continue;
                }

                System.out.println("Введите факультет студента " + (i + 1) + ":");
                String faculty = scanner.nextLine();

                System.out.println("Введите ФИО студента " + (i + 1) + ":");
                String fullName = scanner.nextLine();

                System.out.println("Введите группу студента " + (i + 1) + ":");
                String group = scanner.nextLine();

                Student student = new Student(id, faculty, fullName, group);
                studentList.add(student);
            }

            // Сортировка студентов по полному имени
            studentList.sort(Comparator.comparing(Student::getFullName));

            // Вставка отсортированных данных в базу данных
            String insertQuery = "INSERT INTO " + tableName + " (id, faculty, fullName, group_name) VALUES (?, ?, ?, ?)";
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);

                for (Student student : studentList) {
                    student.addToDatabase(preparedStatement);
                }

                System.out.println("Данные о студентах успешно внесены в базу данных.");
            } catch (SQLException e) {
                System.out.println("Ошибка при вводе данных о студентах: " + e.getMessage());
            }

            return studentList;
        }
        return Collections.emptyList();
    }

    public void addToDatabase(PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setInt(1, id);
        preparedStatement.setString(2, faculty);
        preparedStatement.setString(3, fullName);
        preparedStatement.setString(4, groupName);
        preparedStatement.executeUpdate();
    }

    public static List<Student> retrieveStudents(Connection connection, String tableName) {
        List<Student> students = new ArrayList<>();
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM " + tableName);
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String faculty = resultSet.getString("faculty");
                String fullName = resultSet.getString("fullName");
                String group = resultSet.getString("group_name");
                Student student = new Student(id, faculty, fullName, group);
                students.add(student);
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при выполнении запроса: " + e.getMessage());
        }
        return students;
    }

    public static void saveToDatabase(Connection connection, List<Student> students, String tableName) {
        String insertQuery = "INSERT INTO " + tableName + " (id, faculty, fullName, group_name) VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);

            for (Student student : students) {
                preparedStatement.setInt(1, student.getId());
                preparedStatement.setString(2, student.getFaculty());
                preparedStatement.setString(3, student.getFullName());
                preparedStatement.setString(4, student.getGroupName());
                preparedStatement.addBatch();
            }

            preparedStatement.executeBatch();
            System.out.println("Данные успешно сохранены в базе данных.");
        } catch (SQLException e) {
            System.out.println("Ошибка при сохранении данных в базу данных: " + e.getMessage());
        }
    }

    public static void displayStudents(List<Student> studentList) {
        System.out.printf("%-5s %-20s %-30s %-10s%n", "ID", "Факультет", "ФИО", "Группа");
        for (Student student : studentList) {
            System.out.printf("%-5d %-20s %-30s %-10s%n", student.getId(), student.getFaculty(), student.getFullName(), student.getGroupName());
        }
    }
}



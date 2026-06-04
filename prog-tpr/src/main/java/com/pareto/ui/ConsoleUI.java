package com.pareto.ui;

import com.pareto.model.Alternative;
import com.pareto.service.FileService;
import com.pareto.service.ParetoSetFinder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Консольный пользовательский интерфейс с поддержкой различных форматов файлов
 */
public class ConsoleUI {
    private final Scanner scanner;
    private final ParetoSetFinder paretoFinder;
    private final FileService fileService;
    private List<Alternative> alternatives;
    private boolean[] maximizeDirections;

    /**
     * Конструктор
     */
    public ConsoleUI() {
        this.scanner = new Scanner(System.in, "UTF-8");
        this.paretoFinder = new ParetoSetFinder();
        this.fileService = new FileService();
        this.alternatives = new ArrayList<>();
    }

    /**
     * Запуск главного меню программы
     */
    public void run() {
        System.out.println("=".repeat(60));
        System.out.println("ПРОГРАММА ДЛЯ ВЫДЕЛЕНИЯ МНОЖЕСТВА ПАРЕТО");
        System.out.println("Метод выделения на дискретном множестве альтернатив");
        System.out.println("=".repeat(60));
        System.out.println();
        
        boolean exit = false;
        while (!exit) {
            showMainMenu();
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    inputDataManually();
                    break;
                case "2":
                    showFileLoadMenu();
                    break;
                case "3":
                    findAndShowParetoSet();
                    break;
                case "4":
                    editData();
                    break;
                case "5":
                    showSaveMenu();
                    break;
                case "6":
                    showCurrentData();
                    break;
                case "0":
                    exit = true;
                    System.out.println("Программа завершена.");
                    break;
                default:
                    System.out.println("Неверный выбор. Попробуйте снова.");
            }
            
            if (!exit) {
                System.out.println("\nНажмите Enter для продолжения...");
                scanner.nextLine();
            }
        }
        scanner.close();
    }

    /**
     * Отображение главного меню
     */
    private void showMainMenu() {
        System.out.println("\nГЛАВНОЕ МЕНЮ:");
        System.out.println("1. Ввести данные вручную");
        System.out.println("2. Загрузить данные из файла (TXT/CSV/Excel)");
        System.out.println("3. Найти множество Парето");
        System.out.println("4. Редактировать данные");
        System.out.println("5. Сохранить результаты в файл");
        System.out.println("6. Показать текущие данные");
        System.out.println("0. Выход");
        System.out.print("Выберите действие: ");
    }

    /**
     * Меню загрузки из файла
     */
    private void showFileLoadMenu() {
        System.out.println("\nЗАГРУЗКА ДАННЫХ ИЗ ФАЙЛА");
        System.out.println("-".repeat(30));
        System.out.println("Поддерживаемые форматы:");
        System.out.println("1. Текстовый файл (.txt)");
        System.out.println("2. CSV файл (.csv)");
        System.out.println("3. Excel файл (.xlsx)");
        System.out.println("4. Excel файл старого формата (.xls)");
        System.out.println("0. Вернуться в главное меню");
        System.out.print("\nВыберите формат файла: ");
        
        String formatChoice = scanner.nextLine().trim();
        
        if (formatChoice.equals("0")) {
            return;
        }
        
        showFileFormatHelp(formatChoice);
        System.out.print("\nВведите путь к файлу: ");
        String filePath = scanner.nextLine().trim();
        
        loadDataFromFile(filePath);
    }

    /**
     * Показывает справку по формату файла
     */
    private void showFileFormatHelp(String formatChoice) {
        System.out.println("\nФОРМАТ ФАЙЛА:");
        
        switch (formatChoice) {
            case "1":
                System.out.println("Текстовый файл (.txt):");
                System.out.println("Строка 1: [кол-во критериев] [направления (1=max, 0=min)]");
                System.out.println("Пример: 3 1 0 1");
                System.out.println("Остальные строки: [название] [знач1] [знач2] ...");
                System.out.println("Пример: A 85 100 90");
                break;
            case "2":
                System.out.println("CSV файл (.csv):");
                System.out.println("Строка 1: [кол-во критериев],[направление1],[направление2],...");
                System.out.println("Пример: 3,1,0,1 или 3,max,min,max");
                System.out.println("Остальные строки: [название],[знач1],[знач2],...");
                System.out.println("Пример: A,85,100,90");
                break;
            case "3":
            case "4":
                System.out.println("Excel файл (.xlsx/.xls):");
                System.out.println("Первая строка (заголовок):");
                System.out.println("  A1: 'Критерии' (или любое название)");
                System.out.println("  B1: 'Направление' (или любое название)");
                System.out.println("  C1, D1, ... : направления (1 или 'max' для максимизации,");
                System.out.println("                 0 или 'min' для минимизации)");
                System.out.println("Последующие строки:");
                System.out.println("  Колонка A: название альтернативы");
                System.out.println("  Колонки B, C, ... : значения критериев");
                break;
        }
    }

    /**
     * Загрузка данных из файла
     */
    private void loadDataFromFile(String filePath) {
        try {
            Object[] data = fileService.loadFromFile(filePath);
            alternatives = (List<Alternative>) data[0];
            maximizeDirections = (boolean[]) data[1];
            
            System.out.println("\n✓ Данные успешно загружены из файла!");
            System.out.printf("  Загружено альтернатив: %d%n", alternatives.size());
            System.out.printf("  Количество критериев: %d%n", maximizeDirections.length);
            
            // Показываем краткую сводку
            System.out.println("\n  Направления оптимизации:");
            for (int i = 0; i < maximizeDirections.length; i++) {
                System.out.printf("    Критерий %d: %s%n", i + 1, 
                        maximizeDirections[i] ? "↑ максимизация" : "↓ минимизация");
            }
            
        } catch (IOException e) {
            System.out.println("✗ Ошибка при чтении файла: " + e.getMessage());
            System.out.println("  Проверьте путь к файлу и его формат.");
        } catch (Exception e) {
            System.out.println("✗ Ошибка при обработке данных: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Меню сохранения результатов
     */
    private void showSaveMenu() {
        if (alternatives.isEmpty()) {
            System.out.println("Нет данных для сохранения! Сначала введите или загрузите данные.");
            return;
        }
        
        // Сначала находим множество Парето
        List<Alternative> paretoSet = paretoFinder.findParetoSet(alternatives, maximizeDirections);
        
        System.out.println("\nСОХРАНЕНИЕ РЕЗУЛЬТАТОВ");
        System.out.println("-".repeat(30));
        System.out.println("Выберите формат сохранения:");
        System.out.println("1. Текстовый файл (.txt)");
        System.out.println("2. Excel файл (.xlsx) - с выделением цветом");
        System.out.println("0. Вернуться в главное меню");
        System.out.print("\nВыберите формат: ");
        
        String formatChoice = scanner.nextLine().trim();
        
        if (formatChoice.equals("0")) {
            return;
        }
        
        System.out.print("Введите путь для сохранения файла: ");
        String filePath = scanner.nextLine().trim();
        
        try {
            switch (formatChoice) {
                case "1":
                    // Добавляем расширение .txt если не указано
                    if (!filePath.toLowerCase().endsWith(".txt")) {
                        filePath += ".txt";
                    }
                    fileService.saveToFile(filePath, paretoSet);
                    System.out.println("✓ Результаты сохранены в текстовый файл: " + filePath);
                    break;
                case "2":
                    // Добавляем расширение .xlsx если не указано
                    if (!filePath.toLowerCase().endsWith(".xlsx")) {
                        filePath += ".xlsx";
                    }
                    fileService.saveToExcel(filePath, paretoSet, alternatives);
                    System.out.println("✓ Результаты сохранены в Excel файл: " + filePath);
                    System.out.println("  Лист 1: Все альтернативы (Парето-оптимальные выделены зеленым)");
                    System.out.println("  Лист 2: Только множество Парето");
                    break;
                default:
                    System.out.println("Неверный выбор формата!");
            }
        } catch (IOException e) {
            System.out.println("✗ Ошибка при сохранении файла: " + e.getMessage());
        }
    }

    /**
     * Ручной ввод данных
     */
    private void inputDataManually() {
        System.out.println("\nВВОД ДАННЫХ ВРУЧНУЮ");
        System.out.println("-".repeat(30));
        
        try {
            // Ввод количества критериев
            System.out.print("Введите количество критериев: ");
            int criteriaCount = Integer.parseInt(scanner.nextLine().trim());
            
            if (criteriaCount <= 0) {
                System.out.println("✗ Количество критериев должно быть положительным числом!");
                return;
            }
            
            // Ввод направлений оптимизации
            maximizeDirections = new boolean[criteriaCount];
            System.out.println("\nУкажите направление оптимизации для каждого критерия:");
            System.out.println("  1 или max - максимизация");
            System.out.println("  0 или min - минимизация");
            for (int i = 0; i < criteriaCount; i++) {
                System.out.printf("Критерий %d: ", i + 1);
                String input = scanner.nextLine().trim().toLowerCase();
                maximizeDirections[i] = input.equals("1") || input.equals("max");
            }
            
            // Ввод количества альтернатив
            System.out.print("\nВведите количество альтернатив: ");
            int altCount = Integer.parseInt(scanner.nextLine().trim());
            
            if (altCount <= 0) {
                System.out.println("✗ Количество альтернатив должно быть положительным числом!");
                return;
            }
            
            // Ввод альтернатив
            alternatives.clear();
            System.out.println("\nВведите альтернативы в формате: название значение1 значение2 ...");
            System.out.println("Пример: ПроектА 85.5 100 90.2");
            
            for (int i = 0; i < altCount; i++) {
                System.out.printf("\nАльтернатива %d из %d: ", i + 1, altCount);
                String line = scanner.nextLine().trim();
                String[] tokens = line.split("\\s+");
                
                if (tokens.length != criteriaCount + 1) {
                    System.out.printf("✗ Ошибка: ожидалось %d значений критериев, получено %d%n", 
                            criteriaCount, tokens.length - 1);
                    i--; // Повторяем ввод для этой альтернативы
                    continue;
                }
                
                String name = tokens[0];
                List<Double> criteria = new ArrayList<>();
                
                boolean hasError = false;
                for (int j = 1; j <= criteriaCount; j++) {
                    try {
                        criteria.add(Double.parseDouble(tokens[j]));
                    } catch (NumberFormatException e) {
                        System.out.printf("✗ Ошибка: '%s' не является числом%n", tokens[j]);
                        hasError = true;
                        break;
                    }
                }
                
                if (hasError) {
                    i--;
                    continue;
                }
                
                alternatives.add(new Alternative(name, criteria));
            }
            
            System.out.println("\n✓ Данные успешно введены!");
            System.out.printf("  Введено альтернатив: %d%n", alternatives.size());
            
        } catch (NumberFormatException e) {
            System.out.println("✗ Ошибка: неверный формат числа!");
        }
    }

    /**
     * Поиск и отображение множества Парето
     */
    private void findAndShowParetoSet() {
        if (alternatives.isEmpty()) {
            System.out.println("✗ Нет данных! Сначала введите или загрузите данные.");
            return;
        }
        
        System.out.println("\nПОИСК МНОЖЕСТВА ПАРЕТО");
        System.out.println("-".repeat(30));
        
        // Показываем направления оптимизации
        System.out.println("\nНаправления оптимизации:");
        for (int i = 0; i < maximizeDirections.length; i++) {
            System.out.printf("  Критерий %d: %s%n", i + 1, 
                    maximizeDirections[i] ? "↑ максимизация" : "↓ минимизация");
        }
        
        // Показываем все альтернативы
        System.out.println("\nВсе альтернативы:");
        for (int i = 0; i < alternatives.size(); i++) {
            System.out.printf("  %d. %s%n", i + 1, alternatives.get(i));
        }
        
        // Находим множество Парето
        List<Alternative> paretoSet = paretoFinder.findParetoSet(alternatives, maximizeDirections);
        
        // Выводим результаты
        System.out.println("\n" + "=".repeat(50));
        System.out.println("РЕЗУЛЬТАТ: МНОЖЕСТВО ПАРЕТО");
        System.out.println("=".repeat(50));
        System.out.printf("Найдено Парето-оптимальных альтернатив: %d из %d (%.1f%%)%n", 
                paretoSet.size(), alternatives.size(),
                (double) paretoSet.size() / alternatives.size() * 100);
        
        System.out.println("\nПарето-оптимальные альтернативы:");
        for (int i = 0; i < paretoSet.size(); i++) {
            System.out.printf("  %d. ✓ %s%n", i + 1, paretoSet.get(i));
        }
        
        // Показываем неоптимальные альтернативы
        List<Alternative> dominated = new ArrayList<>(alternatives);
        dominated.removeAll(paretoSet);
        if (!dominated.isEmpty()) {
            System.out.println("\nДоминируемые альтернативы:");
            for (int i = 0; i < dominated.size(); i++) {
                System.out.printf("  %d. ✗ %s%n", i + 1, dominated.get(i));
            }
        }
    }

    /**
     * Редактирование данных
     */
    private void editData() {
        if (alternatives.isEmpty()) {
            System.out.println("✗ Нет данных для редактирования!");
            return;
        }
        
        boolean back = false;
        while (!back) {
            System.out.println("\nРЕДАКТИРОВАНИЕ ДАННЫХ");
            System.out.println("-".repeat(30));
            showCurrentData();
            
            System.out.println("\nДоступные действия:");
            System.out.println("1. Изменить значение критерия альтернативы");
            System.out.println("2. Добавить новую альтернативу");
            System.out.println("3. Удалить альтернативу");
            System.out.println("4. Изменить направление оптимизации критерия");
            System.out.println("5. Изменить название альтернативы");
            System.out.println("0. Вернуться в главное меню");
            System.out.print("Выберите действие: ");
            
            String choice = scanner.nextLine().trim();
            
            try {
                switch (choice) {
                    case "1":
                        editAlternativeCriterion();
                        break;
                    case "2":
                        addAlternative();
                        break;
                    case "3":
                        removeAlternative();
                        break;
                    case "4":
                        changeOptimizationDirection();
                        break;
                    case "5":
                        changeAlternativeName();
                        break;
                    case "0":
                        back = true;
                        break;
                    default:
                        System.out.println("Неверный выбор!");
                }
            } catch (Exception e) {
                System.out.println("✗ Ошибка при редактировании: " + e.getMessage());
            }
            
            if (!back) {
                System.out.println("\nНажмите Enter для продолжения...");
                scanner.nextLine();
            }
        }
    }

    /**
     * Изменение названия альтернативы
     */
    private void changeAlternativeName() {
        System.out.print("Введите индекс альтернативы (1-" + alternatives.size() + "): ");
        int altIndex = Integer.parseInt(scanner.nextLine().trim()) - 1;
        
        if (altIndex < 0 || altIndex >= alternatives.size()) {
            System.out.println("✗ Неверный индекс альтернативы!");
            return;
        }
        
        Alternative alt = alternatives.get(altIndex);
        System.out.printf("Текущее название: %s%n", alt.getName());
        
        System.out.print("Введите новое название: ");
        String newName = scanner.nextLine().trim();
        
        if (newName.isEmpty()) {
            System.out.println("✗ Название не может быть пустым!");
            return;
        }
        
        alt.setName(newName);
        System.out.println("✓ Название успешно изменено!");
    }

    /**
     * Изменение критерия альтернативы
     */
    private void editAlternativeCriterion() {
        System.out.print("Введите индекс альтернативы (1-" + alternatives.size() + "): ");
        int altIndex = Integer.parseInt(scanner.nextLine().trim()) - 1;
        
        if (altIndex < 0 || altIndex >= alternatives.size()) {
            System.out.println("✗ Неверный индекс альтернативы!");
            return;
        }
        
        Alternative alt = alternatives.get(altIndex);
        System.out.printf("Текущие значения: %s%n", alt);
        
        System.out.print("Введите индекс критерия (1-" + alt.getCriteriaCount() + "): ");
        int critIndex = Integer.parseInt(scanner.nextLine().trim()) - 1;
        
        if (critIndex < 0 || critIndex >= alt.getCriteriaCount()) {
            System.out.println("✗ Неверный индекс критерия!");
            return;
        }
        
        System.out.printf("Текущее значение критерия %d: %.2f%n", critIndex + 1, alt.getCriterion(critIndex));
        System.out.print("Введите новое значение: ");
        
        try {
            double newValue = Double.parseDouble(scanner.nextLine().trim());
            alt.setCriterion(critIndex, newValue);
            System.out.println("✓ Значение успешно изменено!");
            System.out.printf("  Новые значения: %s%n", alt);
        } catch (NumberFormatException e) {
            System.out.println("✗ Ошибка: введено не число!");
        }
    }

    /**
     * Добавление новой альтернативы
     */
    private void addAlternative() {
        int criteriaCount = maximizeDirections.length;
        
        System.out.print("Введите название новой альтернативы: ");
        String name = scanner.nextLine().trim();
        
        if (name.isEmpty()) {
            System.out.println("✗ Название не может быть пустым!");
            return;
        }
        
        List<Double> criteria = new ArrayList<>();
        System.out.println("Введите значения критериев:");
        
        for (int i = 0; i < criteriaCount; i++) {
            System.out.printf("Критерий %d (%s): ", i + 1, 
                    maximizeDirections[i] ? "max" : "min");
            try {
                criteria.add(Double.parseDouble(scanner.nextLine().trim()));
            } catch (NumberFormatException e) {
                System.out.println("✗ Ошибка: введено не число!");
                return;
            }
        }
        
        alternatives.add(new Alternative(name, criteria));
        System.out.println("✓ Альтернатива успешно добавлена!");
        System.out.printf("  Новая альтернатива: %s%n", alternatives.get(alternatives.size() - 1));
    }

    /**
     * Удаление альтернативы
     */
    private void removeAlternative() {
        System.out.print("Введите индекс альтернативы для удаления (1-" + alternatives.size() + "): ");
        int index = Integer.parseInt(scanner.nextLine().trim()) - 1;
        
        if (index < 0 || index >= alternatives.size()) {
            System.out.println("✗ Неверный индекс!");
            return;
        }
        
        Alternative removed = alternatives.get(index);
        alternatives.remove(index);
        System.out.println("✓ Альтернатива успешно удалена!");
        System.out.printf("  Удалена: %s%n", removed);
    }

    /**
     * Изменение направления оптимизации критерия
     */
    private void changeOptimizationDirection() {
        System.out.println("\nТекущие направления оптимизации:");
        for (int i = 0; i < maximizeDirections.length; i++) {
            System.out.printf("  Критерий %d: %s%n", i + 1, 
                    maximizeDirections[i] ? "↑ максимизация" : "↓ минимизация");
        }
        
        System.out.print("\nВведите индекс критерия (1-" + maximizeDirections.length + "): ");
        int index = Integer.parseInt(scanner.nextLine().trim()) - 1;
        
        if (index < 0 || index >= maximizeDirections.length) {
            System.out.println("✗ Неверный индекс!");
            return;
        }
        
        System.out.print("Новое направление (1 или max - максимизация, 0 или min - минимизация): ");
        String input = scanner.nextLine().trim().toLowerCase();
        maximizeDirections[index] = input.equals("1") || input.equals("max");
        
        System.out.println("✓ Направление оптимизации успешно изменено!");
        System.out.printf("  Критерий %d теперь: %s%n", index + 1, 
                maximizeDirections[index] ? "↑ максимизация" : "↓ минимизация");
    }

    /**
     * Отображение текущих данных
     */
    private void showCurrentData() {
        if (alternatives.isEmpty()) {
            System.out.println("Данные не загружены.");
            return;
        }
        
        System.out.println("\nТЕКУЩИЕ ДАННЫЕ:");
        System.out.println("-".repeat(30));
        
        System.out.println("Направления оптимизации:");
        for (int i = 0; i < maximizeDirections.length; i++) {
            System.out.printf("  Критерий %d: %s%n", i + 1, 
                    maximizeDirections[i] ? "↑ максимизация" : "↓ минимизация");
        }
        
        System.out.println("\nСписок альтернатив (" + alternatives.size() + " шт.):");
        for (int i = 0; i < alternatives.size(); i++) {
            System.out.printf("  %d. %s%n", i + 1, alternatives.get(i));
        }
    }
}
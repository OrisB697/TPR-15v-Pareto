package com.pareto;

import com.pareto.ui.ConsoleUI;

/**
 * Главный класс программы для выделения множества Парето
 * на дискретном множестве альтернатив
 * 
 * @author Student
 * @version 1.0
 */
public class Main {
    
    /**
     * Точка входа в программу
     * Запускает консольный пользовательский интерфейс
     * 
     * @param args аргументы командной строки (не используются)
     */
    public static void main(String[] args) {
        ConsoleUI ui = new ConsoleUI();
        ui.run();
    }
}
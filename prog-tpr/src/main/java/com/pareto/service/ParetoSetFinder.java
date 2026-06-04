package com.pareto.service;

import com.pareto.model.Alternative;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс для нахождения множества Парето
 * Реализует алгоритм выделения Парето-оптимальных решений
 * на дискретном множестве альтернатив
 */
public class ParetoSetFinder {
    
    /**
     * Находит множество Парето из списка альтернатив
     * Алгоритм:
     * 1. Проверяем каждую альтернативу на доминирование другими альтернативами
     * 2. Альтернатива A доминирует над альтернативой B, если:
     *    - A не хуже B по всем критериям
     *    - A строго лучше B хотя бы по одному критерию
     * 3. Если альтернатива не доминируется ни одной другой, она входит в множество Парето
     *
     * @param alternatives список всех альтернатив
     * @param maximizeDirections массив направлений оптимизации (true - максимизация, false - минимизация)
     * @return список Парето-оптимальных альтернатив
     */
    public List<Alternative> findParetoSet(List<Alternative> alternatives, boolean[] maximizeDirections) {
        // Валидация входных данных
        validateInput(alternatives, maximizeDirections);
        
        List<Alternative> paretoSet = new ArrayList<>();
        
        // Проверяем каждую альтернативу
        for (int i = 0; i < alternatives.size(); i++) {
            Alternative current = alternatives.get(i);
            boolean isDominated = false;
            
            // Проверяем, доминируется ли текущая альтернатива какой-либо другой
            for (int j = 0; j < alternatives.size(); j++) {
                if (i == j) continue; // Пропускаем сравнение с самой собой
                
                Alternative other = alternatives.get(j);
                
                if (dominates(other, current, maximizeDirections)) {
                    isDominated = true;
                    break; // Найдена доминирующая альтернатива
                }
            }
            
            // Если альтернатива не доминируется ни одной другой, добавляем в множество Парето
            if (!isDominated) {
                paretoSet.add(current);
            }
        }
        
        return paretoSet;
    }

    /**
     * Проверяет, доминирует ли альтернатива A над альтернативой B
     * 
     * @param a первая альтернатива
     * @param b вторая альтернатива
     * @param maximizeDirections направления оптимизации
     * @return true, если A доминирует над B
     */
    private boolean dominates(Alternative a, Alternative b, boolean[] maximizeDirections) {
        boolean atLeastOneStrictlyBetter = false;
        
        // Проверяем все критерии
        for (int i = 0; i < a.getCriteriaCount(); i++) {
            double valueA = a.getCriterion(i);
            double valueB = b.getCriterion(i);
            
            if (maximizeDirections[i]) {
                // Для максимизации: большее значение лучше
                if (valueA < valueB) {
                    return false; // A хуже B по данному критерию
                }
                if (valueA > valueB) {
                    atLeastOneStrictlyBetter = true; // A лучше B
                }
            } else {
                // Для минимизации: меньшее значение лучше
                if (valueA > valueB) {
                    return false; // A хуже B по данному критерию
                }
                if (valueA < valueB) {
                    atLeastOneStrictlyBetter = true; // A лучше B
                }
            }
        }
        
        // A доминирует над B, если A не хуже по всем критериям и лучше хотя бы по одному
        return atLeastOneStrictlyBetter;
    }

    /**
     * Валидация входных данных
     * 
     * @param alternatives список альтернатив
     * @param maximizeDirections направления оптимизации
     */
    private void validateInput(List<Alternative> alternatives, boolean[] maximizeDirections) {
        if (alternatives == null || alternatives.isEmpty()) {
            throw new IllegalArgumentException("Список альтернатив не может быть пустым");
        }
        
        if (maximizeDirections == null || maximizeDirections.length == 0) {
            throw new IllegalArgumentException("Массив направлений оптимизации не может быть пустым");
        }
        
        int criteriaCount = alternatives.get(0).getCriteriaCount();
        for (Alternative alt : alternatives) {
            if (alt.getCriteriaCount() != criteriaCount) {
                throw new IllegalArgumentException("Все альтернативы должны иметь одинаковое количество критериев");
            }
        }
        
        if (maximizeDirections.length != criteriaCount) {
            throw new IllegalArgumentException("Количество направлений оптимизации должно соответствовать количеству критериев");
        }
    }
}
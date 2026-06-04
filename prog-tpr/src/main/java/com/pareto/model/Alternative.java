package com.pareto.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Класс, представляющий альтернативу с набором критериев
 */
public class Alternative {
    private String name;
    private List<Double> criteria;

    /**
     * Конструктор альтернативы
     * @param name название альтернативы
     * @param criteria список значений критериев
     */
    public Alternative(String name, List<Double> criteria) {
        this.name = name;
        this.criteria = new ArrayList<>(criteria);
    }

    /**
     * Получить название альтернативы
     * @return название
     */
    public String getName() {
        return name;
    }

    /**
     * Установить название альтернативы
     * @param name название
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Получить список критериев
     * @return список критериев
     */
    public List<Double> getCriteria() {
        return new ArrayList<>(criteria);
    }

    /**
     * Получить значение конкретного критерия
     * @param index индекс критерия
     * @return значение критерия
     */
    public double getCriterion(int index) {
        return criteria.get(index);
    }

    /**
     * Установить значение критерия
     * @param index индекс критерия
     * @param value новое значение
     */
    public void setCriterion(int index, double value) {
        criteria.set(index, value);
    }

    /**
     * Получить количество критериев
     * @return количество критериев
     */
    public int getCriteriaCount() {
        return criteria.size();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(": [");
        for (int i = 0; i < criteria.size(); i++) {
            sb.append(String.format("%.2f", criteria.get(i)));
            if (i < criteria.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
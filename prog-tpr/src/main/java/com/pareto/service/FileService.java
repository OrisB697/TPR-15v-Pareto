package com.pareto.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.pareto.model.Alternative;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Сервис для работы с файловым вводом/выводом
 * Поддерживает форматы: TXT, CSV, XLSX, XLS
 */
public class FileService {
    
    /**
     * Определяет тип файла по расширению
     */
    public enum FileType {
        TXT, CSV, XLSX, XLS
    }
    
    /**
     * Загружает альтернативы из файла (автоматически определяет формат)
     * 
     * @param filePath путь к файлу
     * @return массив объектов: [0] - список альтернатив, [1] - массив направлений оптимизации
     * @throws IOException если возникла ошибка при чтении файла
     */
    public Object[] loadFromFile(String filePath) throws IOException {
        String extension = getFileExtension(filePath).toLowerCase();
        
        switch (extension) {
            case "xlsx":
                return loadFromExcel(filePath, true);
            case "xls":
                return loadFromExcel(filePath, false);
            case "csv":
                return loadFromCSV(filePath);
            case "txt":
            default:
                return loadFromTXT(filePath);
        }
    }
    
    /**
     * Получает расширение файла
     */
    private String getFileExtension(String filePath) {
        int lastDot = filePath.lastIndexOf('.');
        if (lastDot > 0) {
            return filePath.substring(lastDot + 1);
        }
        return "";
    }
    
    /**
     * Загружает данные из Excel файла (.xlsx или .xls)
     * Ожидаемая структура Excel файла:
     * - Первый лист содержит данные
     * - Первая строка: "Критерии" | "Направление" | значение1 | значение2 | ...
     *   где значение: 1 - максимизация, 0 - минимизация
     * - Последующие строки: название_альтернативы | значение1 | значение2 | ...
     * 
     * @param filePath путь к файлу
     * @param isXlsx true для .xlsx, false для .xls
     * @return массив объектов с данными
     * @throws IOException если возникла ошибка
     */
    private Object[] loadFromExcel(String filePath, boolean isXlsx) throws IOException {
        List<Alternative> alternatives = new ArrayList<>();
        boolean[] maximizeDirections = null;
        
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = isXlsx ? new XSSFWorkbook(fis) : new HSSFWorkbook(fis)) {
            
            // Берем первый лист
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            
            if (!rowIterator.hasNext()) {
                throw new IOException("Excel файл пуст");
            }
            
            // Читаем первую строку с направлениями оптимизации
            Row headerRow = rowIterator.next();
            int criteriaCount = headerRow.getLastCellNum() - 2; // минус колонки "Критерии" и "Направление"
            
            if (criteriaCount <= 0) {
                throw new IOException("Неверный формат заголовка Excel файла");
            }
            
            maximizeDirections = new boolean[criteriaCount];
            
            // Читаем направления оптимизации из второй колонки и далее
            for (int i = 0; i < criteriaCount; i++) {
                Cell cell = headerRow.getCell(i + 2); // +2 пропускаем название и "Направление"
                if (cell != null) {
                    if (cell.getCellType() == CellType.NUMERIC) {
                        maximizeDirections[i] = cell.getNumericCellValue() == 1.0;
                    } else if (cell.getCellType() == CellType.STRING) {
                        maximizeDirections[i] = cell.getStringCellValue().trim().equals("1") || 
                                               cell.getStringCellValue().trim().toLowerCase().equals("max");
                    }
                }
            }
            
            // Читаем альтернативы
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                
                // Пропускаем пустые строки
                if (row == null || row.getLastCellNum() < 0) continue;
                
                Cell nameCell = row.getCell(0);
                if (nameCell == null) continue;
                
                String name = getCellValueAsString(nameCell);
                if (name.isEmpty()) continue;
                
                List<Double> criteria = new ArrayList<>();
                boolean hasValidData = false;
                
                for (int i = 0; i < criteriaCount; i++) {
                    Cell cell = row.getCell(i + 1);
                    if (cell != null) {
                        try {
                            if (cell.getCellType() == CellType.NUMERIC) {
                                criteria.add(cell.getNumericCellValue());
                                hasValidData = true;
                            } else if (cell.getCellType() == CellType.STRING) {
                                criteria.add(Double.parseDouble(cell.getStringCellValue().trim()));
                                hasValidData = true;
                            }
                        } catch (NumberFormatException e) {
                            throw new IOException(String.format(
                                "Неверное числовое значение в строке '%s', колонка %d", name, i + 1));
                        }
                    } else {
                        criteria.add(0.0);
                    }
                }
                
                if (hasValidData) {
                    alternatives.add(new Alternative(name, criteria));
                }
            }
        }
        
        if (alternatives.isEmpty()) {
            throw new IOException("Не найдено ни одной альтернативы в Excel файле");
        }
        
        return new Object[]{alternatives, maximizeDirections};
    }
    
    /**
     * Получает строковое значение из ячейки
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return String.valueOf(cell.getNumericCellValue());
                } catch (Exception e) {
                    return cell.getStringCellValue().trim();
                }
            default:
                return "";
        }
    }
    
    /**
     * Загружает данные из CSV файла
     * Формат:
     * Первая строка: количество_критериев,направление1,направление2,...
     * Остальные строки: название,значение1,значение2,...
     */
    private Object[] loadFromCSV(String filePath) throws IOException {
        List<Alternative> alternatives = new ArrayList<>();
        boolean[] maximizeDirections = null;
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {
            
            String firstLine = reader.readLine();
            if (firstLine == null) {
                throw new IOException("CSV файл пуст");
            }
            
            String[] firstTokens = firstLine.split("[;,]");
            int criteriaCount = Integer.parseInt(firstTokens[0].trim());
            maximizeDirections = new boolean[criteriaCount];
            
            for (int i = 0; i < criteriaCount; i++) {
                maximizeDirections[i] = firstTokens[i + 1].trim().equals("1") || 
                                       firstTokens[i + 1].trim().toLowerCase().equals("max");
            }
            
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                String[] tokens = line.split("[;,]");
                if (tokens.length < criteriaCount + 1) continue;
                
                String name = tokens[0].trim();
                List<Double> criteria = new ArrayList<>();
                
                for (int i = 1; i <= criteriaCount; i++) {
                    criteria.add(Double.parseDouble(tokens[i].trim()));
                }
                
                alternatives.add(new Alternative(name, criteria));
            }
        }
        
        return new Object[]{alternatives, maximizeDirections};
    }
    
    /**
     * Загружает данные из TXT файла (оригинальный формат)
     * Первая строка: количество_критериев направления_оптимизации (1 - max, 0 - min через пробел)
     * Остальные строки: название_альтернативы значение1 значение2 ... значениеN
     */
    private Object[] loadFromTXT(String filePath) throws IOException {
        List<Alternative> alternatives = new ArrayList<>();
        boolean[] maximizeDirections = null;
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {
            
            String firstLine = reader.readLine();
            if (firstLine == null) {
                throw new IOException("Файл пуст");
            }
            
            String[] firstTokens = firstLine.trim().split("\\s+");
            int criteriaCount = Integer.parseInt(firstTokens[0]);
            maximizeDirections = new boolean[criteriaCount];
            
            for (int i = 0; i < criteriaCount; i++) {
                maximizeDirections[i] = firstTokens[i + 1].equals("1");
            }
            
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                String[] tokens = line.split("\\s+");
                if (tokens.length < criteriaCount + 1) continue;
                
                String name = tokens[0];
                List<Double> criteria = new ArrayList<>();
                
                for (int i = 1; i <= criteriaCount; i++) {
                    criteria.add(Double.parseDouble(tokens[i]));
                }
                
                alternatives.add(new Alternative(name, criteria));
            }
        }
        
        return new Object[]{alternatives, maximizeDirections};
    }
    
    /**
     * Сохраняет результаты в Excel файл
     * 
     * @param filePath путь к файлу
     * @param paretoSet множество Парето
     * @param allAlternatives все альтернативы (для сравнения)
     * @throws IOException если возникла ошибка
     */
    public void saveToExcel(String filePath, List<Alternative> paretoSet, 
                           List<Alternative> allAlternatives) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Результаты");
            
            // Создаем стиль для заголовков
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            // Создаем стиль для Парето-оптимальных альтернатив
            CellStyle paretoStyle = workbook.createCellStyle();
            paretoStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            paretoStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            int rowNum = 0;
            
            // Заголовок "Все альтернативы"
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Все альтернативы (Парето-оптимальные выделены зеленым)");
            titleCell.setCellStyle(headerStyle);
            
            // Заголовки колонок
            Row headerRow = sheet.createRow(rowNum++);
            headerRow.createCell(0).setCellValue("Название");
            if (!allAlternatives.isEmpty()) {
                int criteriaCount = allAlternatives.get(0).getCriteriaCount();
                for (int i = 0; i < criteriaCount; i++) {
                    headerRow.createCell(i + 1).setCellValue("Критерий " + (i + 1));
                }
                headerRow.createCell(criteriaCount + 1).setCellValue("Парето-оптимальная");
            }
            
            // Данные альтернатив
            for (Alternative alt : allAlternatives) {
                Row dataRow = sheet.createRow(rowNum++);
                dataRow.createCell(0).setCellValue(alt.getName());
                
                int criteriaCount = alt.getCriteriaCount();
                for (int i = 0; i < criteriaCount; i++) {
                    dataRow.createCell(i + 1).setCellValue(alt.getCriterion(i));
                }
                
                boolean isParetoOptimal = paretoSet.contains(alt);
                Cell paretoCell = dataRow.createCell(criteriaCount + 1);
                paretoCell.setCellValue(isParetoOptimal ? "Да" : "Нет");
                
                if (isParetoOptimal) {
                    for (int i = 0; i <= criteriaCount + 1; i++) {
                        dataRow.getCell(i).setCellStyle(paretoStyle);
                    }
                }
            }
            
            // Добавляем отдельный лист только с Парето-оптимальными
            Sheet paretoSheet = workbook.createSheet("Множество Парето");
            int paretoRowNum = 0;
            
            Row paretoTitleRow = paretoSheet.createRow(paretoRowNum++);
            Cell paretoTitleCell = paretoTitleRow.createCell(0);
            paretoTitleCell.setCellValue("Множество Парето");
            paretoTitleCell.setCellStyle(headerStyle);
            
            Row paretoHeaderRow = paretoSheet.createRow(paretoRowNum++);
            paretoHeaderRow.createCell(0).setCellValue("Название");
            if (!paretoSet.isEmpty()) {
                int criteriaCount = paretoSet.get(0).getCriteriaCount();
                for (int i = 0; i < criteriaCount; i++) {
                    paretoHeaderRow.createCell(i + 1).setCellValue("Критерий " + (i + 1));
                }
            }
            
            for (Alternative alt : paretoSet) {
                Row dataRow = paretoSheet.createRow(paretoRowNum++);
                dataRow.createCell(0).setCellValue(alt.getName());
                
                for (int i = 0; i < alt.getCriteriaCount(); i++) {
                    dataRow.createCell(i + 1).setCellValue(alt.getCriterion(i));
                }
            }
            
            // Авторазмер колонок
            for (int i = 0; i < 10; i++) {
                sheet.autoSizeColumn(i);
                paretoSheet.autoSizeColumn(i);
            }
            
            // Сохраняем файл
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }
        }
    }
    
    /**
     * Сохраняет результаты в текстовый файл
     */
    public void saveToFile(String filePath, List<Alternative> paretoSet) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8))) {
            
            writer.write("Множество Парето (Парето-оптимальные альтернативы):");
            writer.newLine();
            writer.write("=".repeat(50));
            writer.newLine();
            
            for (Alternative alt : paretoSet) {
                writer.write(alt.toString());
                writer.newLine();
            }
            
            writer.newLine();
            writer.write("Количество Парето-оптимальных альтернатив: " + paretoSet.size());
        }
    }
}
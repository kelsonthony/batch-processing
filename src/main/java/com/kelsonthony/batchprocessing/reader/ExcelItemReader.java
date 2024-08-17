package com.kelsonthony.batchprocessing.reader;

import com.kelsonthony.batchprocessing.entity.Customer;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;


import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

@Component
public class ExcelItemReader implements ItemReader<Customer> {

    private Iterator<Row> rowIterator;

    public ExcelItemReader() {
        try {
            FileInputStream file = new FileInputStream("src/main/resources/customers.xlsx");
            Workbook workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);
            rowIterator = sheet.iterator();
            rowIterator.next(); // Skip header row
        } catch (IOException e) {
            throw new RuntimeException("Failed to read Excel file", e);
        }
    }

    @Override
    public Customer read() {
        if (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Customer customer = new Customer();
            customer.setId(getNumericCellValue(row.getCell(0)));
            customer.setFirstname(getStringCellValue(row.getCell(1)));
            customer.setLastName(getStringCellValue(row.getCell(2)));
            customer.setEmail(getStringCellValue(row.getCell(3)));
            customer.setGender(getStringCellValue(row.getCell(4)));
            customer.setContactNo(getStringCellValue(row.getCell(5)));
            customer.setCountry(getStringCellValue(row.getCell(6)));
            customer.setDob(String.valueOf(row.getCell(7).getDateCellValue()));
            customer.setAge(String.valueOf(getNumericCellValue(row.getCell(8))));
            return customer;
        } else {
            return null;
        }
    }

    private int getNumericCellValue(Cell cell) {
        if (cell.getCellType() == CellType.NUMERIC) {
            return (int) cell.getNumericCellValue();
        } else if (cell.getCellType() == CellType.STRING) {
            return Integer.parseInt(cell.getStringCellValue());
        } else {
            throw new IllegalArgumentException("Cannot get numeric value from cell type: " + cell.getCellType());
        }
    }

    private String getStringCellValue(Cell cell) {
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue();
        } else if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf(cell.getNumericCellValue());
        } else {
            throw new IllegalArgumentException("Cannot get string value from cell type: " + cell.getCellType());
        }
    }
}
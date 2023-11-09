package com.asurma.account.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.asurma.account.Constants;
import com.asurma.account.config.AdyenConfiguration;
import com.asurma.account.model.AdyenAccount;
import com.asurma.account.model.AdyenAccountRepository;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExcelFileService {

    final Logger log = LoggerFactory.getLogger(ExcelFileService.class);

    @Autowired
    private AdyenAccountRepository adyenAccountRepository;

    public void accountsFromExcel(InputStream inputStream, String fileName) throws IOException {

        Date date = new Date(System.currentTimeMillis());
        Calendar calendar = Calendar.getInstance();

        String id = String.valueOf(calendar.get(Calendar.MONTH)+1) + String.valueOf(calendar.get(Calendar.DAY_OF_MONTH))
                + String.valueOf(calendar.get(Calendar.HOUR_OF_DAY)) + String.valueOf(calendar.get(Calendar.MINUTE));
        Integer transactionId = Integer.valueOf(id);

        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet worksheet = workbook.getSheetAt(0);

        for (int i = 1; i < worksheet.getPhysicalNumberOfRows(); i++) {
            AdyenAccount account = new AdyenAccount();

            Row row = worksheet.getRow(i);

            account.setTransactionId(transactionId);
            account.setLastName(row.getCell(0).getStringCellValue());
            account.setFirstName(row.getCell(1).getStringCellValue());
            account.setEmail(row.getCell(2).getStringCellValue());
            account.setLogin(row.getCell(3).getStringCellValue());
            account.setActive(row.getCell(4).getBooleanCellValue());
            account.setUserGroup(row.getCell(5).getStringCellValue());
            account.setMarket(row.getCell(6).getStringCellValue());
            
//generateLogin(Constants.TYPE_CUSTOMER_SERVICE, account)
            account.setStatus(Constants.AccountRecordStatus.FILE_IMPORTED.getValue());
            account.setEnvironment(AdyenConfiguration.getEnvironment());
            account.setFileName(fileName);
            account.setCreateDate(date);

            try {
                adyenAccountRepository.save(account);
            } catch (Exception exc) {
                log.error("Unable to insert record." + exc.getMessage());
            }

        }
    }

    private boolean encodeAciveValue(String stringCellValue) {
        if ("TRUE".equals(stringCellValue)) return true;
        else return false;
    }

    /**
     * 
     * @param accounts
     * @return
     * @throws IOException
     */
    public ByteArrayInputStream accountsToExcel(List<AdyenAccount> accounts) throws IOException {
        String[] COLUMNs = {"Last Name","First Name","Email","Username","Password", "Active?","User Group","Market"};

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream();) {
            CreationHelper createHelper = workbook.getCreationHelper();

            Sheet sheet = workbook.createSheet("Accounts");

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.BLACK.getIndex());

            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);

            headerCellStyle.setBorderBottom(BorderStyle.THIN);
            headerCellStyle.setBorderTop(BorderStyle.THIN);
            headerCellStyle.setBorderRight(BorderStyle.THIN);
            headerCellStyle.setBorderLeft(BorderStyle.THIN);

            // Row for Header
            Row headerRow = sheet.createRow(0);

            // Header
            for (int col = 0; col < COLUMNs.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(COLUMNs[col]);
                cell.setCellStyle(headerCellStyle);
            }

            // CellStyle for Age
            CellStyle recordCellStyle = workbook.createCellStyle();
            //ageCellStyle.setsetDataFormat(createHelper.createDataFormat().getFormat("#"));
            
            recordCellStyle.setBorderBottom(BorderStyle.THIN);
            recordCellStyle.setBorderTop(BorderStyle.THIN);
            recordCellStyle.setBorderRight(BorderStyle.THIN);
            recordCellStyle.setBorderLeft(BorderStyle.THIN);
            
            int rowIdx = 1;
            for (AdyenAccount account : accounts) {
                Row row = sheet.createRow(rowIdx++);

                Cell createCell = row.createCell(0);
                createCell.setCellStyle(recordCellStyle);
                createCell.setCellValue(account.getLastName());
                
                
                Cell createCell2 = row.createCell(1);
                createCell2.setCellStyle(recordCellStyle);
                createCell2.setCellValue(account.getFirstName());
                
                Cell createCell3 = row.createCell(2);
                createCell3.setCellStyle(recordCellStyle);
                createCell3.setCellValue(account.getEmail());
                
                
                
                Cell createCell4 = row.createCell(3);
                
                String prefix= getPrefixByUserGroup(account.getUserGroup());

                String strFormula= "LOWER(CONCATENATE(\""+ prefix + "\",LEFT(A" +rowIdx + ",5),LEFT(B"+  rowIdx  +",3)))";
                //createCell4.setCellType(Cell.CELL_TYPE_FORMULA);
                //createCell4.setCellFormula(strFormula);

                
                createCell4.setCellStyle(recordCellStyle);
                createCell4.setCellValue(account.getLogin());

                Cell createCell5 = row.createCell(4);
                createCell5.setCellStyle(recordCellStyle);
                createCell5.setCellValue(account.getPassword());

                Cell createCell6 = row.createCell(5);
                createCell6.setCellStyle(recordCellStyle);
                createCell6.setCellValue(account.isActive());

                Cell createCell7 = row.createCell(6);
                createCell7.setCellStyle(recordCellStyle);
                createCell7.setCellValue(account.getUserGroup());

                Cell createCell8 = row.createCell(7);
                createCell8.setCellStyle(recordCellStyle);
                createCell8.setCellValue(account.getMarket());

                
                // Cell ageCell = row.createCell(3);
                // ageCell.setCellValue(customer.getAge());
                // ageCell.setCellStyle(ageCellStyle);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    private String getPrefixByUserGroup(String userGroup) {
        String prefix = "";
        if (Constants.TYPE_CUSTOMER_SERVICE.equals(userGroup)) {
            prefix = Constants.CUSTOMER_SERVICE_PREFIX;
        } else if (Constants.TYPE_FINANCE.equals(userGroup)) {
            prefix = Constants.CUSTOMER_FINANCE;
        }
        // TODO Auto-generated method stub
        return prefix;
    }

    /**
     * 
     * @param type
     * @param account
     * @return
     */
    private String generateLogin(String type, AdyenAccount account) {
        String login = "";
        if (Constants.TYPE_CUSTOMER_SERVICE.equals(type)) {
            int indexLastName = account.getLastName().length() >= 6 ? 6 : account.getLastName().length();
            int indexFirstName = account.getFirstName().length() >= 3 ? 3 : account.getFirstName().length();

            String name = account.getLastName().substring(0, indexLastName)
                    + account.getFirstName().substring(0, indexFirstName);

            login = Constants.CUSTOMER_SERVICE_PREFIX + name;
        }
        log.info("Generating login: firstName={}, lastName={}, login={}", new Object[] {account.getFirstName(), account.getLastName(), login.toLowerCase()});
        return login.toLowerCase();
    }

}

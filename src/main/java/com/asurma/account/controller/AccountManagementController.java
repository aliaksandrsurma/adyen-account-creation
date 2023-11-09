package com.asurma.account.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.asurma.account.Constants;
import com.asurma.account.WsCallResults;
import com.asurma.account.config.AdyenConfiguration;
import com.asurma.account.model.AdyenAccount;
import com.asurma.account.model.AdyenAccountRepository;
import com.asurma.account.model.TempAccount;
import com.asurma.account.model.TempAccountRepository;
import com.asurma.account.service.AccountManagementService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.hibernate.SessionFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
@EnableTransactionManagement
public class AccountManagementController {

    final Logger log = LoggerFactory.getLogger(AccountManagementController.class);

    @Autowired
    private AccountManagementService accountManagementService;

    @Autowired
    private AdyenAccountRepository adyenAccountRepository;

    @Autowired
    private TempAccountRepository tempAccountRepository;

	private SessionFactory hibernateFactory;

	@Autowired
	public AccountManagementController(EntityManagerFactory factory) {
		if (factory.unwrap(SessionFactory.class) == null) {
			throw new NullPointerException("factory is not a hibernate factory");
		}
		this.hibernateFactory = factory.unwrap(SessionFactory.class);
	}

//    @GetMapping("/update-user-test")
//    public String updateUserTest(HttpServletRequest request) {
//        try {
//            accountManagementService.updateUser(request, "surmaale_testidm");
//        } catch (IOException | ServletException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//            return "failed";
//        }
//
//        return "Success!";
//    }

    // @GetMapping(path="/get-list")
    // public @ResponseBody Iterable<AdyenAccount>
    // getUsers(@RequestParam(value="fileName",required=true) String fileName, Model
    // model) {
    // // This returns a JSON or XML with the users
    // return adyenAccountRepository.findByFileName(fileName);
    // }

    @GetMapping("/get-list")
    public String getUsers(@RequestParam(value = "fileName", required = true) String fileName, Model model) {
    	
    	model.addAttribute("environment", AdyenConfiguration.getEnvironment());
    	 
        model.addAttribute("fileName", fileName);
        model.addAttribute("accounts", adyenAccountRepository.findByFileName(fileName));
        
        EntityManager entityManager = hibernateFactory.createEntityManager();

        String jpql = "SELECT e.fileName FROM AdyenAccount e";
        List propertyValues = entityManager.createQuery(jpql).getResultList();

        List<String> fileNamesFromList = (List<String>)propertyValues;
        log.info("list:"+fileNamesFromList);
        model.addAttribute("files", fileNamesFromList);
        
        return "list-accounts-from-file";
    }

    @GetMapping("/create-accounts-in-adyen")
    public String createAccountsInAdyen(@RequestParam(value = "fileName", required = true) String fileName,
                                        HttpServletRequest request, Model model) {
        List result = new ArrayList<String>();
        int successCount = 0, failedCount = 0;
        
        List<AdyenAccount> accountsFromFile = adyenAccountRepository.findByFileName(fileName);

        for (AdyenAccount account : accountsFromFile) {
            Constants.WsCallStatus status = accountManagementService.addUser(request, account);
            
            if (Constants.WsCallStatus.SUCCESS.equals(status)) {
                account.setStatus(Constants.AccountRecordStatus.ADYEN_CREATED_SUCCESS.getValue());
                
                if (Constants.TYPE_CUSTOMER_SERVICE.equals(account.getUserGroup())) {
                    //remove extra role that Adyen adds for some reason
                    accountManagementService.fixCSUser(request, account.getLogin());
                }
                
                
                successCount++;
            } else {
                account.setStatus(Constants.AccountRecordStatus.ADYEN_CREATED_FAILED.getValue());
                failedCount++;
            }
            account.setLastUpdateDate(new Date());
            adyenAccountRepository.save(account);
            result.add(account.getEmail() + " : " + status);
        }
        model.addAttribute("totalCount", accountsFromFile.size());
        model.addAttribute("successCount", successCount);
        model.addAttribute("failedCount", failedCount);
        
        model.addAttribute("apiCallResults", result);
        
        return "create-results";
    }

    @GetMapping(path = "/get-all")
    public @ResponseBody Iterable<AdyenAccount> getAllUsers() {
        // This returns a JSON or XML with the users
        return adyenAccountRepository.findAll();
    }

    
    @GetMapping(path = "/print-json")
    public @ResponseBody Iterable<AdyenAccount> getAllByFileName(@RequestParam(value = "fileName", required = true) String fileName) {
        // This returns a JSON or XML with the users
        return adyenAccountRepository.findByFileName(fileName);
    }

    
    @PostMapping("/update-accounts")
    public String importAccountsFromExcel(@RequestParam("file") MultipartFile reapExcelDataFile, HttpServletRequest request, Model model)
            throws IOException {
    	int successCount = 0, failedCount = 0;
    	List result = new ArrayList<String>();
        String fileName = reapExcelDataFile.getOriginalFilename();

        log.info("Updating from file " + fileName);

        Workbook workbook = new XSSFWorkbook(reapExcelDataFile.getInputStream());
        Sheet worksheet = workbook.getSheetAt(0);

        for (int i = 1; i < worksheet.getPhysicalNumberOfRows(); i++) {
            Row row = worksheet.getRow(i);
            String accountName = row.getCell(0).getStringCellValue();
            String addRoles = "", removeRoles = "", addMerchants = "" , removeMerchants = "";
             
            Cell cellAddRoles = row.getCell(1);
            if (cellAddRoles != null) {
                addRoles = cellAddRoles.getStringCellValue();
            }
            Cell cellRemoveRoles = row.getCell(2);
            if (cellRemoveRoles != null) {
                removeRoles = cellRemoveRoles.getStringCellValue();
            }

            Cell cellAddMerchants = row.getCell(3);
            if (cellAddMerchants != null) {
                addMerchants = cellAddMerchants.getStringCellValue();
            }

            Cell cellRemoveMerchants = row.getCell(4);
            if (cellRemoveMerchants != null) {
                removeMerchants = cellRemoveMerchants.getStringCellValue();
            }

            boolean isActive = row.getCell(5).getBooleanCellValue();

            WsCallResults updateUserResult = accountManagementService.updateUser(request, accountName, addRoles,
                    removeRoles, addMerchants, removeMerchants, isActive);
            result.add(accountName + " : " + updateUserResult.getWsCallstatus() + " "
                    + updateUserResult.getDetailedMessage());

            if (Constants.WsCallStatus.SUCCESS.equals(updateUserResult.getWsCallstatus())) {
                successCount++;
            } else {
                failedCount++;
            }

        }

        model.addAttribute("totalCount", worksheet.getPhysicalNumberOfRows() - 1);
        model.addAttribute("successCount", successCount);
        model.addAttribute("failedCount", failedCount);
        
        model.addAttribute("apiCallResults", result);
        
        return "update-results";
    }
    
    
    
    @GetMapping("/retrieve-data")
    public String getWebUsers(HttpServletRequest request, Model model) {
        List result = new ArrayList<String>();
        int successCount = 0, failedCount = 0;
        
        Iterable<TempAccount> accountsFromDB = tempAccountRepository.findAll();

        for (TempAccount account : accountsFromDB) {
            if (account.getLogin().startsWith("anapereira") || account.getLogin().startsWith("anapereira")) {
                Constants.WsCallStatus status = accountManagementService.getWebUser(request, account);
                
                if (Constants.WsCallStatus.SUCCESS.equals(status)) {

                    successCount++;
                } else {
                    
                    failedCount++;
                }
                account.setLastUpdateDate(new Date());
                tempAccountRepository.save(account);
                result.add(account.getEmail() + " : " + status);
            } else {
                log.info("Skipping login=" + account.getLogin());
            }
        }
        model.addAttribute("successCount", successCount);
        model.addAttribute("failedCount", failedCount);
        
        model.addAttribute("apiCallResults", result);
        
        return "update-results";
    }
    
}

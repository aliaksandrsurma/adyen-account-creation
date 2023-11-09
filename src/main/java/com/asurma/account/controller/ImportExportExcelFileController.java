package com.asurma.account.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


import com.asurma.account.model.AdyenAccount;
import com.asurma.account.model.AdyenAccountRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import org.hibernate.SessionFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.asurma.account.service.ExcelFileService;

@Controller
public class ImportExportExcelFileController {

    final Logger log = LoggerFactory.getLogger(ImportExportExcelFileController.class);

    @Autowired
    private AdyenAccountRepository adyenAccountRepository;

    @Autowired
    private ExcelFileService excelFileService;

    
	private SessionFactory hibernateFactory;

	@Autowired
	public ImportExportExcelFileController(EntityManagerFactory factory) {
		if (factory.unwrap(SessionFactory.class) == null) {
			throw new NullPointerException("factory is not a hibernate factory");
		}
		this.hibernateFactory = factory.unwrap(SessionFactory.class);
	}
	
    @GetMapping("/export-select-file")
    public String exportIndex(Model model) {

        EntityManager entityManager = hibernateFactory.createEntityManager();

        String jpql = "SELECT e.fileName FROM AdyenAccount e";
        List propertyValues = entityManager.createQuery(jpql).getResultList();

        List<String> fileNamesFromList = (List<String>)propertyValues;
        log.info("list:"+fileNamesFromList);
        model.addAttribute("files", fileNamesFromList);
        
        return "export-select-file";
    }

    @PostMapping("/import")
    public ModelAndView importAccountsFromExcel(@RequestParam("file") MultipartFile reapExcelDataFile)
            throws IOException {
        String fileName = reapExcelDataFile.getOriginalFilename();

        log.info("Importing file " + fileName);

        excelFileService.accountsFromExcel(reapExcelDataFile.getInputStream(), fileName);

        return new ModelAndView("redirect:/get-list" + "?fileName=" + fileName);
    }

    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> excelAccountsReport(@RequestParam(value="fileName", required = true) String fileName, @RequestParam(value="status", required = true) String status)
            throws IOException {
        List<AdyenAccount> accounts = new ArrayList<AdyenAccount>();
        
		accounts = (List<AdyenAccount>) adyenAccountRepository.findByFileNameAndStatus(fileName, status);
            
     

//      List<AdyenAccount> accounts = (List<AdyenAccount>) adyenAccountRepository.findByFileName(fileName);

        ByteArrayInputStream in = excelFileService.accountsToExcel(accounts);

        
        String newFileName = fileName.replaceAll(".xlsx", "");

        Calendar calendar = Calendar.getInstance();
        String suffix = String.valueOf(calendar.get(Calendar.YEAR)) + String.valueOf(calendar.get(Calendar.MONTH + 1))
                + String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=" + newFileName + "-CREATED.xlsx");

        return ResponseEntity.ok().headers(headers).body(new InputStreamResource(in));
    }

}

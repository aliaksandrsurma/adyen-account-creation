package com.asurma.account;

public class Constants {

	public enum WsCallStatus {
		SUCCESS, FAILED;
	}

  
  public enum AccountRecordStatus {
    FILE_IMPORTED("file_imported"),
    ADYEN_CREATED_SUCCESS("adyen_created_success"),
    ADYEN_CREATED_FAILED("adyen_created_failed"),
    ADYEN_UPDATED_SUCCESS("adyen_updated_success"),
    ADYEN_UPDATED_FAILED("adyen_updated_failed");
    private String value;

    AccountRecordStatus (String status) { this.value = status; }

    public String getValue() { return this.value; }
  }
  
  public final static String TYPE_CUSTOMER_SERVICE = "Customer Service";
  public final static String TYPE_FINANCE = "Finance";

  public final static String MARKET_US = "US";
  public final static String MARKET_FSS = "FSS";
  public final static String MARKET_WE = "WE";
  public final static String MARKET_WE_ES_IT_PL = "WE_ESITPL";

  public final static String CUSTOMER_SERVICE_PREFIX = "cs_";
  public final static String CUSTOMER_FINANCE = "fss_";

//  static String STATUS_IMPORTED = "file_imported";
//  static String STATUS_ADYEN_CREATED = "adyen_created";
//  static String STATUS_ADYEN_UPDATED = "adyen_updated";


}

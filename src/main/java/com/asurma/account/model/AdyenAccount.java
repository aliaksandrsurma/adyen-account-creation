package com.asurma.account.model;

import jakarta.persistence.*;

import java.util.Date;

@Entity
public class AdyenAccount {

  @Id
  @GeneratedValue(strategy=GenerationType.AUTO)
  private Integer id;

  @Column(name = "transaction_id")
  private Integer transactionId;

  @Column(unique = true)
  private String email;

  @Column(name = "first_name")
  private String firstName;

  @Column(name = "last_name")
  private String lastName;

  // @Column(name="login")
  private String login;

  // @Column(name="login")
  private String password;

  private boolean active;

  @Column(name="user_group")
  private String userGroup;

  private String market;

  @Column(name = "full_name")
  private String full_name;

  // @Column(name="status")
  private String status;
  private String error;

  @Column(name = "psp_reference")
  private String pspReference;

  private String environment;
  
  @Column(name = "create_date")
  private Date createDate;

  @Column(name = "file_name")
  private String fileName;

  @Column(name = "last_update_date")
  private Date lastUpdateDate;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Integer getTransactionId() {
    return transactionId;
  }

  public void setTransactionId(Integer transactionId) {
    this.transactionId = transactionId;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getLogin() {
    return login;
  }

  public void setLogin(String login) {
    this.login = login;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getFull_name() {
    return full_name;
  }

  public void setFull_name(String full_name) {
    this.full_name = full_name;
  }


  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Date getCreateDate() {
    return createDate;
  }

  public void setCreateDate(Date createDate) {
    this.createDate = createDate;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public Date getLastUpdateDate() {
    return lastUpdateDate;
  }

  public void setLastUpdateDate(Date lastUpdateDate) {
    this.lastUpdateDate = lastUpdateDate;
  }

  
  public String getPspReference() {
    return pspReference;
  }

  public void setPspReference(String pspReference) {
    this.pspReference = pspReference;
  }

  
  public boolean isActive() {
    return active;
}

public void setActive(boolean active) {
    this.active = active;
}

public String getUserGroup() {
    return userGroup;
}

public void setUserGroup(String userGroup) {
    this.userGroup = userGroup;
}

public String getMarket() {
    return market;
}

public void setMarket(String market) {
    this.market = market;
}



public String getError() {
	return error;
}

public void setError(String error) {
	this.error = error;
}

public String getEnvironment() {
	return environment;
}

public void setEnvironment(String environment) {
	this.environment = environment;
}

@Override
  public String toString() {

    return getFirstName() + " " + getLastName() + " | " + getEmail();
  }

}

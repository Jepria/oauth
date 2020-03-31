package org.jepria.oauth.key.dto;

import org.jepria.server.data.PrimaryKey;

import java.util.Date;

public class KeyDto {
  @PrimaryKey
  private String keyId;
  private String publicKey;
  private String privateKey;
  private Date dateIns;
  private Boolean isActual;
  
  public String getKeyId(){
    return keyId;
  }
  
  public void setKeyId(String keyId) {
    this.keyId = keyId;
  }
  
  public String getPublicKey() {
    return publicKey;
  }
  
  public void setPublicKey(String publicKey) {
    this.publicKey = publicKey;
  }
  
  public String getPrivateKey() {
    return privateKey;
  }
  
  public void setPrivateKey(String privateKey) {
    this.privateKey = privateKey;
  }
  
  public Date getDateIns() {
    return dateIns;
  }
  
  public void setDateIns(Date dateIns) {
    this.dateIns = dateIns;
  }
  
  public Boolean getActual() {
    return isActual;
  }
  
  public void setActual(Boolean actual) {
    isActual = actual;
  }
}
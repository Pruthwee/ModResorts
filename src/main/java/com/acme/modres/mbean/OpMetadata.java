package com.acme.modres.mbean;

public class OpMetadata {

  public OpMetadata() {
  }

  public OpMetadata(String name, String description, String type, int impact) {
    this.name = name;
    this.description = description;
    this.type = type;
    this.impact = impact;
  }

  private String name;
  private String signature;
  }

  public String getDescription() {
    return description;
  }

  public String getType() {
    return type;
  }
  public String getSignature() {
    return signature;
  }

    this.name = name;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setType(String type) {
  public void setSignature(String signature) {
    this.signature = signature;
  }

}

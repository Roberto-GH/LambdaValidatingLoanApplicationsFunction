package com.crediya;

import java.math.BigDecimal;
import java.util.List;

public class LoanValidationRequest {

  private String applicantEmail;
  private BigDecimal applicantBaseSalary;
  private List<Loan> activeLoans;
  private Loan newLoan;

  private LoanValidationRequest(Builder builder) {
    this.applicantEmail = builder.applicantEmail;
    this.applicantBaseSalary = builder.applicantBaseSalary;
    this.activeLoans = builder.activeLoans;
    this.newLoan = builder.newLoan;
  }

  public static Builder builder() {
    return new Builder();
  }

  public void setApplicantEmail(String applicantEmail) {
    this.applicantEmail = applicantEmail;
  }

  public void setApplicantBaseSalary(BigDecimal applicantBaseSalary) {
    this.applicantBaseSalary = applicantBaseSalary;
  }

  public void setActiveLoans(List<Loan> activeLoans) {
    this.activeLoans = activeLoans;
  }

  public void setNewLoan(Loan newLoan) {
    this.newLoan = newLoan;
  }

  public String getApplicantEmail() {
    return applicantEmail;
  }

  public BigDecimal getApplicantBaseSalary() {
    return applicantBaseSalary;
  }

  public List<Loan> getActiveLoans() {
    return activeLoans;
  }

  public Loan getNewLoan() {
    return newLoan;
  }

  public static class Builder {

    private String applicantEmail;
    private BigDecimal applicantBaseSalary;
    private List<Loan> activeLoans;
    private Loan newLoan;

    public Builder applicantEmail(String applicantEmail) {
      this.applicantEmail = applicantEmail;
      return this;
    }

    public Builder applicantBaseSalary(BigDecimal applicantBaseSalary) {
      this.applicantBaseSalary = applicantBaseSalary;
      return this;
    }

    public Builder activeLoans(List<Loan> activeLoans) {
      this.activeLoans = activeLoans;
      return this;
    }

    public Builder newLoan(Loan newLoan) {
      this.newLoan = newLoan;
      return this;
    }

    public LoanValidationRequest build() {
      return new LoanValidationRequest(this);
    }

  }

}

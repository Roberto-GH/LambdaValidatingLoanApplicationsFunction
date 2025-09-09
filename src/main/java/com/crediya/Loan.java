package com.crediya;

import java.math.BigDecimal;

public class Loan {

  private BigDecimal requestedAmount;
  private Integer term;
  private BigDecimal monthlyInterestRate;
  private Integer requestedTermMonths;

  private Loan(Builder builder) {
    this.requestedAmount = builder.requestedAmount;
    this.term = builder.term;
    this.monthlyInterestRate = builder.monthlyInterestRate;
    this.requestedTermMonths = builder.requestedTermMonths;
  }

  public static Builder builder() {
    return new Builder();
  }

  public BigDecimal getRequestedAmount() {
    return requestedAmount;
  }

  public Integer getTerm() {
    return term;
  }

  public BigDecimal getMonthlyInterestRate() {
    return monthlyInterestRate;
  }

  public Integer getRequestedTermMonths() {
    return requestedTermMonths;
  }

  public void setRequestedAmount(BigDecimal requestedAmount) {
    this.requestedAmount = requestedAmount;
  }

  public void setTerm(Integer term) {
    this.term = term;
  }

  public void setMonthlyInterestRate(BigDecimal monthlyInterestRate) {
    this.monthlyInterestRate = monthlyInterestRate;
  }

  public void setRequestedTermMonths(Integer requestedTermMonths) {
    this.requestedTermMonths = requestedTermMonths;
  }

  public static class Builder {

    private BigDecimal requestedAmount;
    private Integer term;
    private BigDecimal monthlyInterestRate;
    private Integer requestedTermMonths;

    public Builder requestedAmount(BigDecimal requestedAmount) {
      this.requestedAmount = requestedAmount;
      return this;
    }

    public Builder term(Integer term) {
      this.term = term;
      return this;
    }

    public Builder monthlyInterestRate(BigDecimal monthlyInterestRate) {
      this.monthlyInterestRate = monthlyInterestRate;
      return this;
    }

    public Builder requestedTermMonths(Integer requestedTermMonths) {
      this.requestedTermMonths = requestedTermMonths;
      return this;
    }

    public Loan build() {
      return new Loan(this);
    }

  }

}

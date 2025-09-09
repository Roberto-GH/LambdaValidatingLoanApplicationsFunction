package com.crediya;

public record ValidationResult(String applicantEmail, String status, String reason, Integer statusId) {
}
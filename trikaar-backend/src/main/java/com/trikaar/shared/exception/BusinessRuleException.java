package com.trikaar.shared.exception;

/**
 * Thrown when a business rule is violated.
 */
public class BusinessRuleException extends RuntimeException {

    private final String ruleCode;

    public BusinessRuleException(String message) {
        super(message);
        this.ruleCode = "BUSINESS_RULE_VIOLATION";
    }

    public BusinessRuleException(String ruleCode, String message) {
        super(message);
        this.ruleCode = ruleCode;
    }

    public String getRuleCode() {
        return ruleCode;
    }
}

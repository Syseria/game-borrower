package org.lgp.Validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.lgp.Entity.Loan;

import java.util.Date;

public class LoanDurationValidator implements ConstraintValidator<LoanDuration, Loan.CreateLoanRequestDTO> {
    @Override
    public boolean isValid(Loan.CreateLoanRequestDTO dto, ConstraintValidatorContext context) {
        if (dto.dueDate() == null) return true;
        return dto.dueDate().after(new Date());
    }
}
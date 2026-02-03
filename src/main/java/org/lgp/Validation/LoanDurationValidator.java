package org.lgp.Validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.lgp.DTO.CreateLoanRequestDTO;

import java.util.Date;

public class LoanDurationValidator implements ConstraintValidator<LoanDuration, CreateLoanRequestDTO> {
    @Override
    public boolean isValid(CreateLoanRequestDTO dto, ConstraintValidatorContext context) {
        if (dto.dueDate() == null) return true;
        return dto.dueDate().after(new Date());
    }
}
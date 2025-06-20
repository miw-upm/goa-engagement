package es.upm.api.domain.model.validations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public class ListNotEmptyValidator implements ConstraintValidator<ListNotEmpty, List<?>> {

    @Override
    public void initialize(ListNotEmpty constraint) {
        // Empty, not operation
    }

    @Override
    public boolean isValid(List<?> list, ConstraintValidatorContext context) {
        return list != null && !list.isEmpty();
    }

}

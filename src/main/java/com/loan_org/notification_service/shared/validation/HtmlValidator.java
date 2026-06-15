package com.loan_org.notification_service.shared.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;

public class HtmlValidator implements ConstraintValidator<ValidHtml, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }

        try {
            Parser parser = Parser.htmlParser().setTrackErrors(10);
            Jsoup.parse(value, "", parser);

            return parser.getErrors().isEmpty();
        } catch (Exception _) {
            return false;
        }
    }
}
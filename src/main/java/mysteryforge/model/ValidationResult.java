package mysteryforge.model;

import java.util.ArrayList;
import java.util.List;

public class ValidationResult {

    private final List<String> errors;
    private final List<String> warnings;

    public ValidationResult() {

        errors = new ArrayList<>();
        warnings = new ArrayList<>();
    }

    public void addError(String message) {
        errors.add(message);
    }

    public void addWarning(String message) {
        warnings.add(message);
    }

    public boolean isValid() {
        return errors.isEmpty();
    }

    public List<String> getErrors() {
        return List.copyOf(errors);
    }

    public List<String> getWarnings() {
        return List.copyOf(warnings);
    }

    public int getErrorCount() {
        return errors.size();
    }

    public int getWarningCount() {
        return warnings.size();
    }

    public String getSummary() {

        if (isValid()) {
            return "Validation successful. Warnings: "
                    + getWarningCount();
        }

        return "Validation failed. Errors: "
                + getErrorCount()
                + " | Warnings: "
                + getWarningCount();
    }
}
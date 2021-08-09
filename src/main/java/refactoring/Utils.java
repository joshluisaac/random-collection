package refactoring;

public class Utils {


    public static RuntimeException fieldNotFoundException(
            String propertyPath, ValidatedField validatedField) {
        throw new IllegalArgumentException();
    }

    public static RuntimeException cannotQueryPastFieldException(
            String propertyPath, String fieldName, ValidatedField validatedField) {
        throw new IllegalArgumentException();
    }

    public static RuntimeException fieldNotFoundException(
            String propertyPath, String fieldName, MappedClass mappedClass) {
        throw new IllegalArgumentException();
    }



}

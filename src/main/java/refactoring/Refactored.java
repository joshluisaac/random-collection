package refactoring;

import org.javatuples.Pair;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;


public class Refactored {

    static ValidatedField validateQuery(
            final Class<FakeType> clazz,
            final Mapper mapper,
            final String propertyPath,
            final boolean validateNames) {
        final ValidatedField validatedField = new ValidatedField(propertyPath);

        if (!propertyPath.startsWith("$")) {
            final List<String> databasePathElements = List.of(propertyPath.split("\\."));
            System.out.println(databasePathElements.toString());
            if (Objects.isNull(clazz)) return validatedField;
            validatedField.mappedClass = mapper.getMappedClass(clazz);
            List<MappedField> mappedFields = getMappedFields(validatedField, getStandardFields(databasePathElements));
            if (validateNames) mappedFields.forEach(mappedField -> validateFieldName(propertyPath, validatedField, mappedField));
            validateOtherFields(propertyPath, validatedField, mappedFields);
        }
        return validatedField;
    }

    private static List<MappedField> getMappedFields(ValidatedField validatedField, List<String> standardFields) {
        return standardFields.stream()
                .map(fieldName -> validatedField.mappedClass.getMappedField(fieldName)
                        .orElse(validatedField.mappedClass.getMappedFieldByJavaField(fieldName)
                                .orElse(null))).toList();
    }

    private static List<String> getStandardFields(List<String> databasePathElements) {
        return databasePathElements.stream()
                .filter(fieldName -> !fieldName.equals("$")).toList();
    }

    private static void validateOtherFields(String propertyPath, ValidatedField validatedField, List<MappedField> mappedFields) {
        for (MappedField mappedField : mappedFields) {
            if (Objects.isNull(mappedField) && mappedClassIsInterface(validatedField)) break;
            if (Objects.isNull(mappedField)) throw fieldNotFoundException(propertyPath, validatedField);
        }
    }

    private static boolean mappedClassIsInterface(ValidatedField validatedField) {
        return validatedField.mappedClass.isInterface();
    }

    private static void validateFieldName(String propertyPath, ValidatedField validatedField, MappedField mappedField) {
        var pairs = List.of(mappedFieldIsNull(propertyPath, validatedField),mappedFieldIsReference(propertyPath, validatedField), mappedFieldIsSerializedAnnotated(propertyPath, validatedField));
        pairs.stream()
                .filter(pair -> pair.getValue0().test(mappedField))
                .findFirst()
                .ifPresent(pair -> pair.getValue1().accept(mappedField));
    }

    private static Pair<Predicate<MappedField>, Consumer<MappedField>> mappedFieldIsNull(String propertyPath, ValidatedField validatedField){
        return Pair.with(Objects::isNull, mappedField ->  fieldNotFoundException(propertyPath, "_fieldName_", validatedField.mappedClass));
    }

    private static Pair<Predicate<MappedField>, Consumer<MappedField>> mappedFieldIsReference(String propertyPath, ValidatedField validatedField){
        return Pair.with(MappedField::isReference, mappedField ->  cannotQueryPastFieldException(propertyPath, "_fieldName_", validatedField));
    }

    private static Pair<Predicate<MappedField>, Consumer<MappedField>> mappedFieldIsSerializedAnnotated(String propertyPath, ValidatedField validatedField){
        return Pair.with(mappedField -> mappedField.hasAnnotation(Serialized.class), mappedField ->  cannotQueryPastFieldException(propertyPath, "_fieldName_", validatedField));
    }

    private static RuntimeException fieldNotFoundException(
            String propertyPath, ValidatedField validatedField) {
        throw new IllegalArgumentException();
    }

    private static RuntimeException cannotQueryPastFieldException(
            String propertyPath, String fieldName, ValidatedField validatedField) {
        throw new IllegalArgumentException();
    }

    private static RuntimeException fieldNotFoundException(
            String propertyPath, String fieldName, MappedClass mappedClass) {
        throw new IllegalArgumentException();
    }
}

package refactoring;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Refactored {

    static ValidatedField validateQuery2(
            final Class clazz,
            final Mapper mapper,
            final String propertyPath,
            final boolean validateNames) {
        final ValidatedField validatedField = new ValidatedField(propertyPath);

        if (!propertyPath.startsWith("$")) {
            final String[] pathElements = propertyPath.split("\\.");
            final List<String> databasePathElements = List.of(pathElements);
            if (clazz == null) {
                return validatedField;
            }
            validatedField.mappedClass = mapper.getMappedClass(clazz);

            List<String> normalFields = databasePathElements.stream()
                    .filter(fieldName -> !fieldName.equals("$")).toList();

            List<MappedField> mappedFields = normalFields.stream()
                    .map(fieldName -> validatedField.mappedClass.getMappedField(fieldName)
                            .orElse(validatedField.mappedClass.getMappedFieldByJavaField(fieldName)
                                    .orElse(null))).toList();

            if (validateNames) validateFieldName(propertyPath, validatedField, mappedFields);
            validateOtherFields(propertyPath, validatedField, mappedFields);


        }




        if (!propertyPath.startsWith("$")) {
            final String[] pathElements = propertyPath.split("\\.");
            final List<String> databasePathElements = new ArrayList<>(asList(pathElements));
            if (clazz == null) {
                return validatedField;
            }

            validatedField.mappedClass = mapper.getMappedClass(clazz);

            for (int i = 0; ; ) {
                final String fieldName = pathElements[i];
                final boolean fieldIsArrayOperator = fieldName.equals("$");

                Optional<MappedField> maybeMappedField = validatedField.mappedClass.getMappedField(fieldName);

                // translate from java field name to stored field name
                if (!maybeMappedField.isPresent() && !fieldIsArrayOperator) {
                    maybeMappedField = validatedField.mappedClass.getMappedFieldByJavaField(fieldName);
                    if (validateNames && !maybeMappedField.isPresent()) {
                        throw fieldNotFoundException(propertyPath, fieldName, validatedField.mappedClass);
                    }
                    if (maybeMappedField.isPresent()) {
                        databasePathElements.set(i, maybeMappedField.get().getNameToStore());
                    }
                }
                validatedField.mappedField = maybeMappedField;

                i++;
                if (maybeMappedField.isPresent() && maybeMappedField.get().isMap()) {
                    // skip the map key validation, and move to the next fieldName
                    i++;
                }

                if (i >= pathElements.length) {
                    break;
                }

                if (!fieldIsArrayOperator && validateNames && (maybeMappedField.get().isReference() || maybeMappedField.get().hasAnnotation(Serialized.class))) {
                    throw cannotQueryPastFieldException(propertyPath, fieldName, validatedField);
                }

                if (!fieldIsArrayOperator && !maybeMappedField.isPresent() && validatedField.mappedClass.isInterface()) {
                    break;
                }

                if (!fieldIsArrayOperator && !maybeMappedField.isPresent()) {
                    throw fieldNotFoundException(propertyPath, validatedField);
                }

                if (!fieldIsArrayOperator) {
                    MappedField mappedField = maybeMappedField.get();
                    validatedField.mappedClass =
                            mapper.getMappedClass(
                                    (mappedField.isSingleValue())
                                            ? mappedField.getType()
                                            : mappedField.getSubClass());
                }
            }
            validatedField.databasePath = databasePathElements.stream().collect(joining("."));
        }
        return validatedField;
    }

    private static void validateOtherFields(String propertyPath, ValidatedField validatedField, List<MappedField> mappedFields) {
        for (MappedField mappedField : mappedFields) {
            if (isNull(mappedField) && mappedClassIsInterface(validatedField)) break;
            if (isNull(mappedField)) throw fieldNotFoundException(propertyPath, validatedField);
        }
    }

    private static boolean mappedClassIsInterface(ValidatedField validatedField) {
        return validatedField.mappedClass.isInterface();
    }

    private static boolean isNull(MappedField mappedField) {
        return mappedField == null;
    }

    private static void validateFieldName(String propertyPath, ValidatedField validatedField, List<MappedField> mappedFields) {
        mappedFields.stream()
                .filter(Objects::isNull)
                .findAny()
                .ifPresent(mappedField -> {
            throw fieldNotFoundException(propertyPath, "_fieldName_", validatedField.mappedClass);
        });

        mappedFields.stream()
                .filter(mappedField -> (mappedField.isReference() || mappedField.hasAnnotation(Serialized.class)))
                .findAny()
                .ifPresent(mappedField -> {
                    throw cannotQueryPastFieldException(propertyPath, "_fieldName_", validatedField);
                });
    }


    static ValidatedField validateQuery(
            final Class clazz,
            final Mapper mapper,
            final String propertyPath,
            final boolean validateNames) {
        final ValidatedField validatedField = new ValidatedField(propertyPath);

        if (!propertyPath.startsWith("$")) {
            final String[] pathElements = propertyPath.split("\\.");
            final List<String> databasePathElements = new ArrayList<>(asList(pathElements));
            if (clazz == null) {
                return validatedField;
            }

            validatedField.mappedClass = mapper.getMappedClass(clazz);

            for (int i = 0; ; ) {
                final String fieldName = pathElements[i];
                final boolean fieldIsArrayOperator = fieldName.equals("$");

                Optional<MappedField> maybeMappedField = validatedField.mappedClass.getMappedField(fieldName);

                // translate from java field name to stored field name
                if (!maybeMappedField.isPresent() && !fieldIsArrayOperator) {
                    maybeMappedField = validatedField.mappedClass.getMappedFieldByJavaField(fieldName);
                    if (validateNames && !maybeMappedField.isPresent()) {
                        throw fieldNotFoundException(propertyPath, fieldName, validatedField.mappedClass);
                    }
                    if (maybeMappedField.isPresent()) {
                        databasePathElements.set(i, maybeMappedField.get().getNameToStore());
                    }
                }
                validatedField.mappedField = maybeMappedField;

                i++;
                if (maybeMappedField.isPresent() && maybeMappedField.get().isMap()) {
                    // skip the map key validation, and move to the next fieldName
                    i++;
                }

                if (i >= pathElements.length) {
                    break;
                }

                if (!fieldIsArrayOperator) {
                    // catch people trying to search/update into @Reference/@Serialized fields
                    if (validateNames
                            && (maybeMappedField.get().isReference() || maybeMappedField.get().hasAnnotation(Serialized.class))) {
                        throw cannotQueryPastFieldException(propertyPath, fieldName, validatedField);
                    }

                    if (!maybeMappedField.isPresent() && validatedField.mappedClass.isInterface()) {
                        break;
                    } else if (!maybeMappedField.isPresent()) {
                        throw fieldNotFoundException(propertyPath, validatedField);
                    }
                    // get the next MappedClass for the next field validation
                    MappedField mappedField = maybeMappedField.get();
                    validatedField.mappedClass =
                            mapper.getMappedClass(
                                    (mappedField.isSingleValue())
                                            ? mappedField.getType()
                                            : mappedField.getSubClass());
                }
            }
            validatedField.databasePath = databasePathElements.stream().collect(joining("."));
        }
        return validatedField;
    }

    private static RuntimeException fieldNotFoundException(
            String propertyPath, ValidatedField validatedField) {
        return new IllegalArgumentException();
    }

    private static RuntimeException cannotQueryPastFieldException(
            String propertyPath, String fieldName, ValidatedField validatedField) {
        return new IllegalArgumentException();
    }

    private static RuntimeException fieldNotFoundException(
            String propertyPath, String fieldName, MappedClass mappedClass) {
        return new IllegalArgumentException();
    }
}

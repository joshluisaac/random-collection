package refactoring;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static refactoring.Utils.cannotQueryPastFieldException;
import static refactoring.Utils.fieldNotFoundException;

public class TrishaGeeOriginalUpdated {


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






}

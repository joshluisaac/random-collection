package refactoring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static refactoring.Utils.cannotQueryPastFieldException;
import static refactoring.Utils.fieldNotFoundException;

public class TrishaGeeOriginal {

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
          System.out.println(Arrays.toString(pathElements));
          System.out.println(fieldName);
        final boolean fieldIsArrayOperator = fieldName.equals("$");

        Optional<MappedField> mf = validatedField.mappedClass.getMappedField(fieldName);

        // translate from java field name to stored field name
        if (!mf.isPresent() && !fieldIsArrayOperator) {
          mf = validatedField.mappedClass.getMappedFieldByJavaField(fieldName);
          if (validateNames && !mf.isPresent()) {
            throw fieldNotFoundException(propertyPath, fieldName, validatedField.mappedClass);
          }
          if (mf.isPresent()) {
            databasePathElements.set(i, mf.get().getNameToStore());
          }
        }
        validatedField.mappedField = mf;

        i++;
        if (mf.isPresent() && mf.get().isMap()) {
          // skip the map key validation, and move to the next fieldName
          i++;
        }

        if (i >= pathElements.length) {
          break;
        }

        if (!fieldIsArrayOperator) {
          // catch people trying to search/update into @Reference/@Serialized fields
          if (validateNames
              && (mf.get().isReference() || mf.get().hasAnnotation(Serialized.class))) {
            throw cannotQueryPastFieldException(propertyPath, fieldName, validatedField);
          }

          if (!mf.isPresent() && validatedField.mappedClass.isInterface()) {
            break;
          } else if (!mf.isPresent()) {
            throw fieldNotFoundException(propertyPath, validatedField);
          }
          // get the next MappedClass for the next field validation
          MappedField mappedField = mf.get();
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

package refactoring;

import java.util.Optional;

class ValidatedField {

  final String propertyPath;
  public Optional<MappedField> mappedField;
  public String databasePath;
  MappedClass mappedClass;

  public ValidatedField(String propertyPath) {
    this.propertyPath = propertyPath;
  }
}

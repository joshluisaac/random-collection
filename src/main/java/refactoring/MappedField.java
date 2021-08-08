package refactoring;

class MappedField {
  public String getNameToStore() {
    return "";
  }

  public boolean isMap() {
    return true;
  }

  public boolean isReference() {
    return false;
  }

  public boolean hasAnnotation(Class<Serialized> serializedClass) {
    return false;
  }

  public boolean isSingleValue() {
    return true;
  }

  public Class getType() {
    return MappedField.class;
  }

  public Class getSubClass() {
    return MappedField.class;
  }
}

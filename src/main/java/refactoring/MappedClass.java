package refactoring;

import java.util.Optional;

class MappedClass {

    public Optional<MappedField> getMappedField(String fieldName) {
        return Optional.of(new MappedField());
    }

    public Optional<MappedField> getMappedFieldByJavaField(String fieldName) {
        return Optional.of(new MappedField());
    }

    public boolean isInterface() {
        return true;
    }
}

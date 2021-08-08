package refactoring;

public class Main {

    public static void main(String[] args) {
        ValidatedField validatedFieldRefac = Refactored.validateQuery(FakeType.class,new Mapper(),"x.x.y.8", true);
        ValidatedField validatedField = Unrefactored.validateQuery(FakeType.class,new Mapper(),"x.x.y.8", true);
        System.out.println(validatedFieldRefac.toString());
        System.out.println(validatedField.toString());
    }

}

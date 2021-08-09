package refactoring;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;


class TrishaGeeOriginalTest {


    @Test
    void shouldReturn_DatabasePath(){
        ValidatedField validatedField = TrishaGeeOriginal.validateQuery(FakeType.class,new Mapper(),"x.x.y.8", true);
        assertEquals("x.x.y.8",validatedField.databasePath);
        assertNotNull(validatedField.mappedClass);
    }


    @Test
    void shouldReturnNull_WhenPropertyPath_IsDollarSignedPrefixed(){
        ValidatedField validatedField = TrishaGeeOriginal.validateQuery(FakeType.class,new Mapper(),"$.x.x.y.8", true);
        assertNull(validatedField.databasePath);
        assertNull(validatedField.mappedClass);
    }

//    @Test
//    void shouldReturnNull_WhenPropertyPath_IsDollarSignedPrefixed2(){
//        ValidatedField validatedField = TrishaGeeOriginal.validateQuery(FakeType.class,new Mapper(),"x.$", true);
//        assertNull(validatedField.databasePath);
//        assertNull(validatedField.mappedClass);
//    }

}
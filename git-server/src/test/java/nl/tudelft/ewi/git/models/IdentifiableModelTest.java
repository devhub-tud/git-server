package nl.tudelft.ewi.git.models;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public class IdentifiableModelTest {

    @Test
    public void assertUserEqualsIdentifiable() {
        String name = "name";
        String path = "path/to/name";

        IdentifiableModel a = new IdentifiableModel();
        a.setName(name);

        UserModel b = new UserModel();
        b.setName(name);

        assertEquals(a, b);
        assertEquals(b, a);
    }

    @Test
    public void assertGroupEqualsIdentifiable() {
        String name = "name";
        String path = "path/to/name";

        IdentifiableModel a = new IdentifiableModel();
        a.setName(name);

        GroupModel b = new GroupModel();
        b.setName(name);

        assertEquals(a, b);
        assertEquals(b, a);
    }

}

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
        a.setPath(path);

        UserModel b = new UserModel();
        b.setName(name);
        b.setPath(path);

        assertEquals(a, b);
        assertEquals(b, a);
    }

    @Test
    public void assertGroupEqualsIdentifiable() {
        String name = "name";
        String path = "path/to/name";

        IdentifiableModel a = new IdentifiableModel();
        a.setName(name);
        a.setPath(path);

        GroupModel b = new GroupModel();
        b.setName(name);
        b.setPath(path);

        assertEquals(a, b);
        assertEquals(b, a);
    }

}

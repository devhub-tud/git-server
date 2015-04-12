package nl.tudelft.ewi.git.models;

import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * Created by jgmeligmeyling on 12/04/15.
 */
@Data
public class Person {

    @NotEmpty
    private String name;


    @NotEmpty
    private String email;

}

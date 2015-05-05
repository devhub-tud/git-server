package nl.tudelft.ewi.git.web;

import lombok.RequiredArgsConstructor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;

import static nl.tudelft.ewi.git.web.UsersApi.validateKeyContents;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@RequiredArgsConstructor
@RunWith(Parameterized.class)
public class FailingKeyTest {

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {"ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQCftV0wGyhXU2aorR2WmJZCCEbzACRxPpGiI5csVz25uEfQ+QpBsTj43rEDkAQPvO6p+$"},
            {"ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQCX3E4ePEeuzuJwDB408dkS7gXgfjSWhom03zn2oAz$"},
            {"ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQC5nnzP2T+jLLNg/S6ejEx2+Js0PpgdedZzcNpdwMbVMNIOBrtIkyyxPP+ckQd5Ush8D$"},
            {"ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDJdlQfcU90mzmVIG4R8QeiqrEI0Kx3KVfM2ux7j/KsN83Pw7BrFvKrlkhfwRjTiQYiwRtVzIVrOlBopel$"},
            {"ssh-rsa AAABAAQus1/EbQoJ+3X2gqLdEl+P2o1+Vz3ktV5wtRTAIyWvMIS1UY79RluJM/xylR1b4LroYGXwqCQwX36ep8oP" +
                    "p/taDorawA31Ufe1laC90ji7y213sVVshvgc4Z0kY+8YpAv+1rk8sMD4FG2Cxz3lg8XumLAd4sMNKP4n3B6T4mqrJ" +
                    "/pgNVfRCLYdY24igAbIOOLHLd+TGlPB9yB8fWvlpIcFS1/SENA1hCWuEH/ot/SAypIcEIYcvDia6QcEnSJkQslnr0" +
                    "dFJ62wUFlR2Gqg92XNYe3AsNDuGk0DqwW0AhuMMSGyiBUlUH8xpTi1ttyEAepYNRZsarJGB4GAE22nADUAAACBAPw" +
                    "m9vmk9uO+HxSSNrWPDtiGaqgkr+jQdneDCki/n6Vcr62rbj3sJVhNMPQ4M1NfVX62arSDZby4MjAStAhc/8ORhCiz" +
                    "Q+66R025I5yHtpijXcMrMhzEs4n+hfimPSKpxpCdn1wWCMNKW7MWNTnoYd+X9HQB3/ZKdfAf24REz+E7AAAAgQCdH" +
                    "HTe+FEsnZgKPpXRquIGirCUihKAqPPewctEvP/soGEu8Z92fWYy7eEJXOX0btiyP70Ot4DGg2u3tAxfUb7+HMpc/u" +
                    "A8OMEf9QHTiJxxsphdmSHzHDUDaClvrIZi27SgAfIjrLKlKB0rIiJRSkhpLnpRZtOk6KyD662e5wqY5QAAAIBfJTn" +
                    "Y39D5Am9VwrSRPp/jGW/5EchqMeTq1AFcGCgt3Q9RhWM3KvCRT+bM3qY8ZJHbZ32epG6V43ueKXADBNb5CXM2PoyJ" +
                    "mQlYxVwtwAf2vx54NpmeCkdi8CV9walId8/DMCFI+Pa5NWn5XKIWB+g9Cf41JCMLHoc1AM8g4JHyPA=="},
            {"ssh-rsa AAAAB3NzaC1yc2EAAAABJQAAAQEAmr/s12PCc3FYDKDhifOnz8qWc0Kb8g42pkor/8UUclIDLjTJqpsrOtSDfI"},
            {"ssh-rsa 2048 b2:08:34:36:20:65:a6:64:9c:f5:cc:10:08:68:6d:8f"}
        });
    }

    private final String key;

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidKey() {
        validateKeyContents(key);
    }

}

package fi.solita.adele;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

public class CommonTestUtil {

    public static <T> void assertContainsAll(Collection<T> all, Collection<T> match) {
        assertTrue("Collection " + all + " does not contain all of " + match, all.containsAll(match));
    }

    public static <T> void assertNotContainsAny(Collection<T> all, Collection<T> noMatch) {
        List<T> match = noMatch.stream().filter(item -> all.contains(item)).collect(Collectors.toList());
        assertTrue("Collection " + all + " contains " + match, Collections.disjoint(all, noMatch));
    }
}

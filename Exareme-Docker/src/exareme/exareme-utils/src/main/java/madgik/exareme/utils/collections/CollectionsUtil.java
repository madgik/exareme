package madgik.exareme.utils.collections;

import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * Utils for working with collections.
 *
 * @author konikos
 */
public class CollectionsUtil {

    /**
     * Chooses a random item from a collection. Note that it traverses the
     * collection in order to finds the needed item.
     *
     * @param collection The collection to choose from.
     * @param rand       A Random object.
     * @return A random item from collection.
     */
    public static <E> E randomChoice(Collection<E> collection, Random rand) {
        int randomIndex = new Random().nextInt(collection.size());
        for (E e : collection) {
            if (randomIndex == 0) {
                return e;
            }
            randomIndex--;
        }

        // should never reach this point
        return null;
    }

    /**
     * Equal to randomChoice(collection, new Random()).
     *
     * @param collection The collection to choose from.
     * @return A random item from collection.
     */
    public static <E> E randomChoice(Collection<E> collection) {
        return randomChoice(collection, new Random());
    }

    /**
     * Chooses a random item from a list. Similar to randomChoice(Collection<E>),
     * but it uses the List get method to avoid traversing the list.
     *
     * @param list The list to choose the item from.
     * @return A random item from the list.
     */
    public static <E> E randomChoice(List<E> list, Random rand) {
        return list.get(rand.nextInt(list.size()));
    }

    /**
     * Equal to randomChoice(list, new Random()).
     *
     * @param list The list to choose the item from.
     * @return A random item from the list.
     */
    public static <E> E randomChoice(List<E> list) {
        return randomChoice(list, new Random());
    }
}

package util;

import java.util.Locale;
import java.util.Random;

public class Utils {

    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = UPPER.toLowerCase(Locale.ROOT);
    private static final String DIGITS = "0123456789";
    private static final String SYMBOLS = "!%^&*()";
    private static final String ALLCHARS = UPPER + LOWER + DIGITS + SYMBOLS;

    /**
     * Gets a number of random printable characters
     *
     * @param characterCount The number of random characters to return
     * @return A string with the specified amount of random characters
     */
    public static String getNRandomCharacters(int characterCount) {

        final StringBuilder randString = new StringBuilder();
        final Random rand = new Random();

        for (int i = 0; i < characterCount; i++) {
            randString.append(ALLCHARS.charAt(rand.nextInt(ALLCHARS.length())));
        }
        return randString.toString();
    }

    /**
     * Gets a number of printable characters with the prefix specified
     * There is no degree of randomness imposed in this, and characters
     * will probably be ordered.
     *
     * @param characterCount The number of characters to return
     * @param startWith      The prefix to start with
     * @return The prefix and the number of characters specified, concatenated to one string.
     */
    public static String getNCharacters(int characterCount, String startWith) {

        final StringBuilder nonRandString = new StringBuilder();
        nonRandString.append(startWith);

        for (int i = 0; nonRandString.length() < characterCount; i++) {
            nonRandString.append(ALLCHARS.charAt(i % ALLCHARS.length()));
        }
        return nonRandString.substring(0, characterCount);
    }

    /**
     * Returns a number of characters with no prefix. No randomness enforced
     *
     * @param characterCount the number of characters to return
     * @return A string with the specified number of characters.
     */
    public static String getNCharacters(int characterCount) {
        return getNCharacters(characterCount, "");
    }
}

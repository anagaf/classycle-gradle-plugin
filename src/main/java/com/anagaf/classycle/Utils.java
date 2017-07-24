package com.anagaf.classycle;

final class Utils {
    /**
     * Capitalizes string first letter.
     *
     * @param original original string
     * @return string with the first letter capitalized
     */
    static String capitalizeFirstLetter(String original) {
        if (original == null || original.isEmpty()) {
            return original;
        }
        return original.substring(0, 1).toUpperCase() + original.substring(1);
    }
}

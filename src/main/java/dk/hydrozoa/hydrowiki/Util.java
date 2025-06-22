package dk.hydrozoa.hydrowiki;

import com.google.common.base.CharMatcher;

public class Util {

    public static String removeLeadingSlashes(String s) {
        return CharMatcher.is('/').trimLeadingFrom(s);
    }

    public static String removeTrailingSlashes(String s) {
        return CharMatcher.is('/').trimTrailingFrom(s);
    }

}

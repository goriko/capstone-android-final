package com.example.dar.share;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TelephoneNumberCanonicalizer {
    private static final Pattern EUROPEAN_DIALING_PLAN = Pattern.compile("^\\+|(00)|(0)");
    private final String countryCode;

    public TelephoneNumberCanonicalizer(String countryCode) {
        this.countryCode = countryCode;
    }

    public String canonicalize(String number) {
        // Remove all weird characters such as /, -, ...
        number = number.replaceAll("[^+0-9]", "");

        Matcher match = EUROPEAN_DIALING_PLAN.matcher(number);
        if (!match.find()) {
            throw new IllegalArgumentException(number);
        } else if (match.group(1) != null) {     // Starts with "00"
            return match.replaceFirst("+");
        } else if (match.group(2) != null) {     // Starts with "0"
            return match.replaceFirst("+" + this.countryCode);
        } else {                                 // Starts with "+"
            return number;
        }
    }
}

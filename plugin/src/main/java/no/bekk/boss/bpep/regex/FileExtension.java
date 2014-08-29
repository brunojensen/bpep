package no.bekk.boss.bpep.regex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum FileExtension {
    JAVA_EXTENSION(".java$");

    private Pattern pattern;

    private FileExtension(final String regex) {
        pattern = Pattern.compile(regex);
    }

    public String removedFrom(final String str) {
        final Matcher matcher = pattern.matcher(str);
        if (matcher.find())
            return matcher.replaceAll("");
        return str;
    }

}

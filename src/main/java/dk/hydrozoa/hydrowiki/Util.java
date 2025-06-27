package dk.hydrozoa.hydrowiki;

import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.Patch;
import com.google.common.base.CharMatcher;

import java.util.Arrays;
import java.util.List;

public class Util {

    public static String removeLeadingSlashes(String s) {
        return CharMatcher.is('/').trimLeadingFrom(s);
    }

    public static String removeTrailingSlashes(String s) {
        return CharMatcher.is('/').trimTrailingFrom(s);
    }

    /**
     * @return  UnifiedDiff to get from orig to revised
     */
    public String generateDiffs(String title, String origLines, String revisedLines) {
        List<String> oldLines = Arrays.asList(origLines.split("\n"));
        List<String> newLines = Arrays.asList(revisedLines.split("\n"));

        Patch<String> patch = DiffUtils.diff(oldLines, newLines);

        List<String> unifiedDiff = UnifiedDiffUtils.generateUnifiedDiff(
                title,
                title,
                oldLines,
                patch,
                0);

        unifiedDiff.forEach(System.out::println);

        StringBuilder result = new StringBuilder();
        unifiedDiff.forEach(line -> {
            result.append(line);
            result.append("\n");
        });
        return result.toString();
    }
}

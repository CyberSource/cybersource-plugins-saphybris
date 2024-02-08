package isv.sap.payment.addon.utils;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public final class ListUtils
{
    private ListUtils()
    {
        // EMPTY
    }

    public static String toString(final Collection<String> values, final Collection<String> excludedValues)
    {
        final List<String> fieldsToSign = values.stream()
                .filter(value -> !excludedValues.contains(value))
                .collect(Collectors.toList());

        return ListUtils.toString(fieldsToSign);
    }

    public static String toString(final Collection<String> values)
    {
        return String.join(",", values);
    }
}

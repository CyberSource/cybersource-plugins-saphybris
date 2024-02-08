package isv.sap.payment.utils;

import java.util.function.Supplier;

/**
 * Utility class for asserting expression
 */
public final class Assert
{
    private Assert()
    {
        // EMPTY
    }

    /**
     * Check if expression is valid, otherwise throw an exception
     *
     * @param expression value to check
     * @param supplier an exception provider in case of invalid expression
     */
    public static void isTrue(final boolean expression, final Supplier<RuntimeException> supplier)
    {
        if (!expression)
        {
            throw supplier.get();
        }
    }
}

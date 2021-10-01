package isv.sap.payment.addon.utils;

import java.util.Collections;
import java.util.Map;

import com.google.common.collect.Maps;

/**
 * A generic container for returning any data for Ajax calls.
 */
public final class AjaxResponse
{
    private boolean success = false; // NOPMD

    private final Map<String, Object> data = Maps.newHashMap();

    private AjaxResponse(final boolean success)
    {
        this.success = success;
    }

    public static AjaxResponse success()
    {
        return new AjaxResponse(true);
    }

    public static AjaxResponse fail()
    {
        return new AjaxResponse(false);
    }

    public AjaxResponse put(final String key, final Object value)
    {
        data.put(key, value);
        return this;
    }

    public boolean isSuccess()
    {
        return success;
    }

    public Map<String, Object> getData()
    {
        return Collections.unmodifiableMap(data);
    }
}

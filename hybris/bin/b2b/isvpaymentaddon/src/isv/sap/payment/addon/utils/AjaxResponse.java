package isv.sap.payment.addon.utils;

import java.util.Collections;
import java.util.Map;

import com.google.common.collect.Maps;

/**
 * A generic container for returning any data for Ajax calls.
 */
public final class AjaxResponse
{
    private final boolean successFlag;

    private final Map<String, Object> data = Maps.newHashMap();

    private AjaxResponse(final boolean success)
    {
        this.successFlag = success;
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
        return successFlag;
    }

    public Map<String, Object> getData()
    {
        return Collections.unmodifiableMap(data);
    }
}

package com.epicnicity322.epicpluginapi.config;

import com.sun.istack.internal.Nullable;

/**
 * This class will help output both {@link LoadResult} and Exceptions.
 * <p>
 * With this class you can get the Exceptions generated by the load methods on
 * {@link ConfigManager}. You may use the results and exceptions to output or
 * log into a file.
 * If you want a constructor of this class you can get it by the method
 * {@link LoadOutput#output(LoadResult, Exception)} just to spare you from
 * using "new LoadOutput...". Exceptions can be null if your thing didn't
 * generated one. In this case the method {@link LoadOutput#output(LoadResult)}
 * is preferred, so unnecessary checks won't be made.
 */
public class LoadOutput
{
    public enum LoadResult
    {
        SUCCESS, RESTORED_OLD, ERROR_EXTRACTION, ERROR_LOAD
    }

    private LoadResult result;
    private Exception exception;
    private boolean hasException = true;

    private LoadOutput(LoadResult result, @Nullable Exception exception)
    {
        this.result = result;
        this.exception = exception;

        if (exception == null) {
            hasException = false;
        }
    }

    private LoadOutput(LoadResult result)
    {
        this.result = result;
        hasException = false;
    }

    /**
     * @param exception Exceptions can be null because they will depend on the {@link LoadResult}.
     * @return A constructed {@link LoadOutput}.
     */
    public static LoadOutput output(LoadResult result, @Nullable Exception exception)
    {
        return new LoadOutput(result, exception);
    }

    /**
     * @param result The result of the load of something.
     * @return A constructed {@link LoadOutput} without unnecessary checks.
     */
    public static LoadOutput output(LoadResult result)
    {
        return new LoadOutput(result);
    }

    /**
     * Allows you to know if the output has generated an exception.
     *
     * @return If the exception equals null.
     */
    public boolean hasException()
    {
        return hasException;
    }

    public LoadResult getResult()
    {
        return result;
    }

    /**
     * With this you can get the exception to log into a file or do whatever.
     *
     * @return The exceptions generated by the load output.
     * @throws NullPointerException If the {@link LoadResult} isn't an error.
     */
    public Exception getException() throws NullPointerException
    {
        return exception;
    }
}
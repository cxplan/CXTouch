package com.cxplan.projection.i18n;

import java.net.URL;
import java.net.URLClassLoader;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

/**
 * This class defines i18nized strings. These strings are stored in a file
 * with a base name I18NStrings.properties in each package directory.
 * @author Kenny Liu
 *
 * 2018-12-11 create
 */
public class StringManager {

	/** Logger for this class. */
	private static Logger s_log = Logger.getLogger(StringManager.class);

	/** Contains the localised strings. */
	private ResourceBundle _rsrcBundle;
	private String _bundleBaseName;
	private URL[] _bundleLoaderUrLs = new URL[0];

	/**
	 * Ctor specifying the package name. Attempt to load a resource bundle
	 * from the package directory.
	 *
	 * @param	packageName	Name of package
	 * @param	loader	Class loader to use
	 */
	StringManager(String packageName, ClassLoader loader)
	{
		super();
		_bundleBaseName = packageName + ".I18NStrings";
		_rsrcBundle = ResourceBundle.getBundle(_bundleBaseName, Locale.getDefault(), loader);

		if(loader instanceof URLClassLoader)
		{
			_bundleLoaderUrLs = ((URLClassLoader) loader).getURLs();
		}


	}

	/**
	 * Retrieve the localized string for the passed key. If it isn't found
	 * an error message is returned instead.
	 *
	 * @param	key		Key to retrieve string for.
	 *
	 * @return	Localized string or error message.
	 *
	 * @throws	IllegalArgumentException
	 * 			Thrown if <TT>null</TT> <TT>key</TT> passed.
	 */
	public String getString(String key)
	{
		if (key == null)
		{
			throw new IllegalArgumentException("key == null");
		}

		try
		{
			return _rsrcBundle.getString(key);
		}
		catch (MissingResourceException ex)
		{
			if(s_log.isDebugEnabled())
			{
				StringBuffer sb = new StringBuffer();
				sb.append("No resource string found for key '" + key + "' in bundle " + _bundleBaseName + "\n\n");
	
				if(0 < _bundleLoaderUrLs.length)
				{
					sb.append("The following classpath entries are available to the bundle loader:\n");
					for (int i = 0; i < _bundleLoaderUrLs.length; i++)
					{
						sb.append(_bundleLoaderUrLs[i]).append("\n");
					}
				}
				s_log.error(sb.toString());
			}
			return null;
		}
	}

    /**
     * Retrieve the localized string for the passed key and format it with the
     * passed arguments.
     *
     * @param   key     Key to retrieve string for.
     * @param   args    Any string arguments that should be used as values to 
     *                  parameters found in the localized string.
     *                   
     * @return  Localized string or error message.
     *
     * @throws  IllegalArgumentException
     *          Thrown if <TT>null</TT> <TT>key</TT> passed.
     */    
    public String getString(String key, String[] args) 
    {
        return getString(key, (Object[])args);
    }
    
	/**
	 * Retrieve the localized string for the passed key and format it with the
	 * passed arguments.
	 *
	 * @param	key		Key to retrieve string for.
     * @param   args    Any string arguments that should be used as values to 
     *                  parameters found in the localized string. 
	 *
	 * @return	Localized string or error message.
	 *
	 * @throws	IllegalArgumentException
	 * 			Thrown if <TT>null</TT> <TT>key</TT> passed.
	 */
	public String getString(String key, Object... args)
	{
		if (key == null)
		{
			throw new IllegalArgumentException("key == null");
		}

		if (args == null)
		{
			args = new Object[0];
		}

		final String str = getString(key);
		try
		{
			return MessageFormat.format(str, args);
		}
		catch (IllegalArgumentException ex)
		{
			String msg = "Error formatting i18 string. Key is '" + key + "'";
			s_log.error(msg, ex);
			return msg + ": " + ex.toString();
		}
	}
}

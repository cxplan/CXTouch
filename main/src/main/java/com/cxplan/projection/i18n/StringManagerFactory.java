package com.cxplan.projection.i18n;

import java.util.HashMap;
import java.util.Map;

/**
 * This class manages instances of <TT>StringManager</TT> objects. It keeps a
 * cache of them, one for each package.
 * @author Kenny Liu
 *
 * 2018-12-11 create
 */
public class StringManagerFactory {

	/**
	 * Collection of <TT>StringManager</TT> objects keyed by the Java package
	 * name.
	 */
	private static final Map<String, StringManager> s_mgrs = 
	    new HashMap<String, StringManager>();

	/**
	 * Retrieve an instance of <TT>StringManager</TT> for the passed class.
	 * Currently an instance of <TT>Stringmanager</TT> is stored for each
	 * package.
	 *
	 * @param	clazz	<TT>Class</TT> to retrieve <TT>StringManager</TT> for.
	 *
	 * @return	instance of <TT>StringManager</TT>.
	 *
	 * @throws	IllegalArgumentException
	 * 			Thrown if <TT>null</TT> <TT>clazz</TT> passed.
	 */
	public static synchronized StringManager getStringManager(Class<?> clazz)
	{
		if (clazz == null)
		{
			throw new IllegalArgumentException("clazz == null");
		}

		final String key = getKey(clazz);
		StringManager mgr = s_mgrs.get(key);
		if (mgr == null)
		{
			mgr = new StringManager(key, clazz.getClassLoader());
			s_mgrs.put(key, mgr);
		}
		return mgr;
	}

	/**
	 * Retrieve the key to use to identify the <TT>StringManager</TT> instance
	 * for the passed class. Currently one instance is stored for each package.
	 *
	 * @param	clazz	<TT>Class</TT> to get key for.
	 *
	 * @return	the key to use.
	 *
	 * @throws	IllegalArgumentException
	 * 			Thrown if <TT>null</TT> <TT>clazz</TT> passed.
	 */
	private static String getKey(Class<?> clazz)
	{
		if (clazz == null)
		{
			throw new IllegalArgumentException("clazz == null");
		}

		final String clazzName = clazz.getName();
		return clazzName.substring(0, clazzName.lastIndexOf('.'));
	}
}

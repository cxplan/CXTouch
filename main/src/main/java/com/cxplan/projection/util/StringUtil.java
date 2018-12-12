/**
 * The code is written by ytx, and is confidential.
 * Anybody must not broadcast these files without authorization.
 */
package com.cxplan.projection.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created on 2017/5/17.
 *
 * @author kenny
 */
public class StringUtil {

    public static final ObjectMapper JSON_MAPPER_NOTNULL = new ObjectMapper();
    static {
        JSON_MAPPER_NOTNULL.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        JSON_MAPPER_NOTNULL.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        JSON_MAPPER_NOTNULL.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        JSON_MAPPER_NOTNULL.setLocale(Locale.getDefault());
    }

    /**
     * 从Json字符串转换为object，该方法不支持泛型.
     * 如果有泛型支持需要，请使用api{@link #json2Object(String, TypeReference)}
     *
     * @param content json字符串
     * @param type 对象类型
     * @throws IllegalArgumentException 如果转换失败会抛出该异常。
     */
    public static <T extends Object> T json2Object(String content, Class<T> type) {
        try {
            return JSON_MAPPER_NOTNULL.readValue(content, type);
        } catch (IOException e) {
            throw new IllegalArgumentException("Transforming（json2Object） failed: " + e.getMessage(), e);
        }
    }
    /**
     * 从Json字符串转换为object。
     * 如果有对象类型支持泛型，可以使用该方法。
     * @param content json字符串
     * @param type 对象类型定义
     * @throws IllegalArgumentException 如果转换失败会抛出该异常。
     */
    public static <T extends Object> T json2Object(String content, TypeReference type) {
        if (content == null) {
            return null;
        }
        try {
            return JSON_MAPPER_NOTNULL.readValue(content, type);
        } catch (IOException e) {
            throw new IllegalArgumentException("Transforming（json2Object） failed: " + e.getMessage(), e);
        }
    }

    /**
     * 将业务对象转换为json字符串
     * @param obj 需要被转换成json string的对象。
     * @return json字符串
     */
    public static String toJSONString(Object obj) {
        try{
            return JSON_MAPPER_NOTNULL.writeValueAsString(obj);
        }catch(Exception e){
            throw new IllegalArgumentException("Transforming（toJSONString） failed: " + e.getMessage(), e);
        }
    }
    /**
     * 检查指定内容是否无内容：null 或者空的字符串。
     * 注意空格也是算内容。
     */
    public static boolean isEmpty(Object str) {
        if (str instanceof String) {
            return (str == null || ((String)str).length() == 0);
        } else {
            return str == null;
        }
    }

    public static boolean isNotEmpty(Object str) {
        return !isEmpty(str);
    }

    /**
     * <p>Checks if a String is whitespace, empty ("") or null.</p>
     *
     * <pre>
     * StringUtils.isBlank(null)      = true
     * StringUtils.isBlank("")        = true
     * StringUtils.isBlank(" ")       = true
     * StringUtils.isBlank("bob")     = false
     * StringUtils.isBlank("  bob  ") = false
     * </pre>
     *
     * @param str  the String to check, may be null
     * @return <code>true</code> if the String is null, empty or whitespace
     */
    public static boolean isBlank(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if ((Character.isWhitespace(str.charAt(i)) == false)) {
                return false;
            }
        }
        return true;
    }

    /**
     * <p>Checks if a String is not empty (""), not null and not whitespace only.</p>
     *
     * <pre>
     * StringUtils.isNotBlank(null)      = false
     * StringUtils.isNotBlank("")        = false
     * StringUtils.isNotBlank(" ")       = false
     * StringUtils.isNotBlank("bob")     = true
     * StringUtils.isNotBlank("  bob  ") = true
     * </pre>
     *
     * @param str  the String to check, may be null
     * @return <code>true</code> if the String is
     *  not empty and not null and not whitespace
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    public static final String replace(String haystack, String needle, String replacement)
    {
        if (replacement == null) return haystack;
        if (needle == null) return haystack;
        if (haystack == null) return null;

        int pos = haystack.indexOf(needle);
        if (pos == -1) return haystack;

        StringBuilder temp = replaceToBuffer(null,haystack,needle, replacement);

        return temp.toString();
    }
    /**
     * Replaces all occurances of aValue in aString with aReplacement and appends the resulting
     * String to the passed target.
     * @param target the buffer to append the result to. If target is null a new StringBuilder will be created
     * @param haystack the String in which to search the value
     * @param needle the value to search for
     * @param replacement the value with which needle is replaced
     * @return the resulting buffer
     */
    public static final StringBuilder replaceToBuffer(StringBuilder target, String haystack, String needle,String replacement)
    {
        if (target == null)
        {
            target = new StringBuilder((int)(haystack.length() * 1.1));
        }

        int pos = haystack.indexOf(needle);
        if (pos == -1)
        {
            target.append(haystack);
            return target;
        }

        int lastpos = 0;
        int len = needle.length();
        while (pos != -1)
        {
            target.append(haystack.substring(lastpos, pos));
            if (replacement != null) target.append(replacement);
            lastpos = pos + len;
            pos = haystack.indexOf(needle, lastpos);
        }
        if (lastpos < haystack.length())
        {
            target.append(haystack.substring(lastpos));
        }
        return target;
    }

    /**
     * Create a string with specified char,
     * and the length of string is determined by parameter length.
     */
    public static String createFixedLengthString(String unit, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(unit);
        }
        return sb.toString();
    }

    public static double getDoubleValue(String aValue, double aDefault)
    {
        if (aValue == null) return aDefault;

        double result = aDefault;
        try
        {
            result = Double.parseDouble(aValue.trim());
        }
        catch (NumberFormatException e)
        {
            // Ignore
        }
        return result;
    }
    public static float getFloatValue(String aValue, float aDefault)
    {
        if (aValue == null) return aDefault;

        float result = aDefault;
        try
        {
            result = Float.parseFloat(aValue.trim());
        }
        catch (NumberFormatException e)
        {
            // Ignore
        }
        return result;
    }

    public static int getIntValue(String aValue)
    {
        return getIntValue(aValue, 0);
    }

    public static int getIntValue(String aValue, int aDefault)
    {
        if (aValue == null) return aDefault;

        int result = aDefault;
        try
        {
            result = Integer.parseInt(aValue.trim());
        }
        catch (NumberFormatException e)
        {
            // Ignore
        }
        return result;
    }

    public static long getLongValue(String aValue, long aDefault)
    {
        if (aValue == null) return aDefault;

        long result = aDefault;
        try
        {
            result = Long.parseLong(aValue.trim());
        }
        catch (NumberFormatException e)
        {
            // Ignore
        }
        return result;
    }
    public static boolean stringToBool(String aString)
    {
        if (aString == null) return false;
        return ("true".equalsIgnoreCase(aString) || "1".equals(aString) || "y".equalsIgnoreCase(aString) || "yes".equalsIgnoreCase(aString) );
    }
    public static int getBooleanValue(String aValue, int aDefault)
    {
        if (aValue == null) return aDefault;

        int result = aDefault;
        try
        {
            result = Integer.parseInt(aValue.trim());
        }
        catch (NumberFormatException e)
        {
            // Ignore
        }
        return result;
    }
    public static final boolean equalString(String one, String other)
    {
        return compareStrings(one, other, false) == 0;
    }

    public static int compareStrings(String value1, String value2, boolean ignoreCase)
    {
        if (value1 == null && value2 == null) return 0;
        if (value1 == null) return -1;
        if (value2 == null) return 1;
        if (ignoreCase) return value1.compareToIgnoreCase(value2);
        return value1.compareTo(value2);
    }

    public static String decodeUnicode(String theString)
    {
        if (theString == null) return null;

        char aChar;
        int len = theString.length();
        if (len == 0) return theString;
        StringBuilder outBuffer = new StringBuilder(len);

        for (int x=0; x < len ; )
        {
            aChar = theString.charAt(x++);
            if (aChar == '\\' && x < len)
            {
                aChar = theString.charAt(x++);

                if (aChar == 'u')
                {
                    // Read the xxxx
                    int value = -1;
                    int i=0;
                    for (i=0; i<4; i++)
                    {
                        if ( x + i >= len)
                        {
                            value = -1;
                            break;
                        }

                        aChar = theString.charAt(x + i);
                        switch (aChar)
                        {
                            case '0': case '1': case '2': case '3': case '4':
                            case '5': case '6': case '7': case '8': case '9':
                            value = (value << 4) + aChar - '0';
                            break;
                            case 'a': case 'b': case 'c':
                            case 'd': case 'e': case 'f':
                            value = (value << 4) + 10 + aChar - 'a';
                            break;
                            case 'A': case 'B': case 'C':
                            case 'D': case 'E': case 'F':
                            value = (value << 4) + 10 + aChar - 'A';
                            break;
                            default:
                                // Invalid ecape sequence
                                value = -1;
                                break;
                        }
                        if (value == -1) break;
                    }

                    if ( value != -1)
                    {
                        outBuffer.append((char)value);
                    }
                    else
                    {
                        // Invalid encoded unicode character
                        // do not convert the stuff, but copy the
                        // characters into the result buffer
                        outBuffer.append("\\u");
                        if (i == 0 && x < len)
                        {
                            outBuffer.append(aChar);
                        }
                        else
                        {
                            for (int k=0; k < i; k++) outBuffer.append(theString.charAt(x+k));
                        }
                        x++;
                    }
                    x += i;
                }
                else
                {
                    // The character after the backslash was not a 'u'
                    // so we are not dealing with a uXXXX value
                    // This applies popular "encodings" for non-printable characters
                    if (aChar == 't') aChar = '\t';
                    else if (aChar == 'r') aChar = '\r';
                    else if (aChar == 'n') aChar = '\n';
                    else if (aChar == 'f') aChar = '\f';
                    else if (aChar == '\\') aChar = '\\';
                    else outBuffer.append('\\');
                    outBuffer.append(aChar);
                }
            }
            else
            {
                outBuffer.append(aChar);
            }

        }
        return outBuffer.toString();
    }

    public static String concatIntArray(int[] idArray, String separator) {
        if (idArray == null || idArray.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < idArray.length; i++) {
            sb.append(idArray[i]);
            if (i < idArray.length - 1) {
                sb.append(separator);
            }
        }
        return sb.toString();
    }
    public static int[] split2IntArray(String string, String separator) {
        if (string == null) {
            return new int[0];
        }
        String[] array = string.split(separator);
        int[] ret = new int[array.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = getIntValue(array[i], -1);
        }
        return ret;
    }

    /**
     * Remove the suffix from the passed file name.
     *
     * @param	fileName	File name to remove suffix from.
     *
     * @return	<TT>fileName</TT> without a suffix.
     *
     * @throws	IllegalArgumentException	if <TT>null</TT> file name passed.
     */
    public static String removeFileNameSuffix(String fileName)
    {
        if (fileName == null)
        {
            throw new IllegalArgumentException("file name == null");
        }
        int pos = fileName.lastIndexOf('.');
        if (pos > 0 && pos < fileName.length() - 1)
        {
            return fileName.substring(0, pos);
        }
        return fileName;
    }

    public static String replacePlaceHolder(String origVal, Map<String, String> data)
    {
        if (origVal == null) {
            return origVal;
        }
        StringBuilder sb = new StringBuilder();
        int pos = -1;
        Status status = Status.NONE;
        int length = origVal.length();
        for (int i = 0; i < length; i++)
        {
            char ch = origVal.charAt(i);
            switch (ch)
            {
                case '$':
                    if ((status != Status.NONE) && (pos > -1)) {
                        sb.append(origVal.substring(pos, i));
                    }
                    pos = i;
                    status = Status.FLAG;
                    break;
                case '{':
                    if (status == Status.FLAG) {
                        status = Status.START;
                    } else if (status == Status.NONE) {
                        sb.append(ch);
                    }
                    break;
                case '}':
                    if (status == Status.START)
                    {
                        if (pos < 0) {
                            throw new RuntimeException("Illegal status");
                        }
                        if (i - pos == 2) {
                            sb.append("${}");
                        }
                        String key = origVal.substring(pos + 2, i);
                        String value = data.get(key);
                        if (value == null) {
                            sb.append("${").append(key).append("}");
                        } else {
                            sb.append(value);
                        }
                        pos = -1;
                        status = Status.NONE;
                    }
                    break;
                default:
                    if (status == Status.FLAG)
                    {
                        status = Status.NONE;
                        sb.append('$').append(ch);
                        pos = -1;
                    }
                    else if (status == Status.NONE)
                    {
                        sb.append(ch);
                    }
            }
        }
        if ((status == Status.FLAG) || (status == Status.START)) {
            sb.append(origVal.substring(pos));
        }
        return sb.toString();
    }

    private enum Status
    {
        NONE,  FLAG,  START,  END

    }

    public static void main(String[] args) {
        String s = "a${12}b$${cd}a${dd}d${}";
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("cd", "1111");
        paramMap.put("12", "33");

        System.out.println(replacePlaceHolder(s, paramMap));

    }
}

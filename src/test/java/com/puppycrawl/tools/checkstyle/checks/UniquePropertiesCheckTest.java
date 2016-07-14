////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2016 the original author or authors.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
////////////////////////////////////////////////////////////////////////////////

package com.puppycrawl.tools.checkstyle.checks;

import static com.puppycrawl.tools.checkstyle.checks.UniquePropertiesCheck.MSG_IO_EXCEPTION_KEY;
import static com.puppycrawl.tools.checkstyle.checks.UniquePropertiesCheck.MSG_KEY;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.junit.Before;
import org.junit.Test;

import com.puppycrawl.tools.checkstyle.BaseFileSetCheckTestSupport;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.api.LocalizedMessage;

/**
 * JUnit tests for Unique Properties check.
 */
public class UniquePropertiesCheckTest extends BaseFileSetCheckTestSupport {

    private DefaultConfiguration checkConfig;

    @Before
    public void setUp() {
        checkConfig = createCheckConfig(UniquePropertiesCheck.class);
    }

    @Override
    protected String getPath(String filename) throws IOException {
        return super.getPath("checks" + File.separator + filename);
    }

    /* Additional test for jacoco, since valueOf()
     * is generated by javac and jacoco reports that
     * valueOf() is uncovered.
     */
    @Test
    public void testLineSeparatorOptionValueOf() {
        final LineSeparatorOption option = LineSeparatorOption.valueOf("CR");
        assertEquals(LineSeparatorOption.CR, option);
    }

    /**
     * Tests the ordinal work of a check.
     */
    @Test
    public void testDefault() throws Exception {
        final String[] expected = {
            "3: " + getCheckMessage(MSG_KEY, "general.exception", 2),
            "5: " + getCheckMessage(MSG_KEY, "DefaultLogger.auditStarted", 2),
            "11: " + getCheckMessage(MSG_KEY, "onlineManual", 3),
            "22: " + getCheckMessage(MSG_KEY, "time stamp", 3),
            "28: " + getCheckMessage(MSG_KEY, "Support Link ", 2),
            "34: " + getCheckMessage(MSG_KEY, "failed", 2),
        };
        verify(checkConfig, getPath("InputUniqueProperties.properties"), expected);
    }

    /**
     * Tests the {@link UniquePropertiesCheck#getLineNumber(List, String)}
     * method return value.
     */
    @Test
    public void testNotFoundKey() {
        final List<String> testStrings = new ArrayList<String>(3);
        testStrings.add("");
        testStrings.add("0 = 0");
        testStrings.add("445");
        final int stringNumber =
                UniquePropertiesCheck.getLineNumber(testStrings,
                        "some key");
        assertEquals(0, stringNumber);
    }

    /**
     * Tests IO exception, that can occur during reading of properties file.
     */
    @Test
    public void testIoException() throws Exception {
        final UniquePropertiesCheck check = new UniquePropertiesCheck();
        check.configure(checkConfig);
        final String fileName =
                getPath("InputUniquePropertiesCheckNotExisting.properties");
        final File file = new File(fileName);
        final SortedSet<LocalizedMessage> messages =
                check.process(file, Collections.<String>emptyList());
        assertEquals("Wrong messages count: " + messages.size(),
                1, messages.size());
        final LocalizedMessage message = messages.iterator().next();
        final String retrievedMessage = messages.iterator().next().getKey();
        assertEquals("Message key '" + retrievedMessage
                        + "' is not valid", "unable.open.cause",
                retrievedMessage);
        assertEquals("Message '" + message.getMessage()
                        + "' is not valid", message.getMessage(),
                getCheckMessage(MSG_IO_EXCEPTION_KEY, fileName, getFileNotFoundDetail(file)));
    }

    @Test
    public void testWrongKeyTypeInProperties() throws Exception {
        final Class<?> uniquePropertiesClass = Class
                .forName("com.puppycrawl.tools.checkstyle.checks."
                    + "UniquePropertiesCheck$UniqueProperties");
        final Constructor<?> constructor = uniquePropertiesClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        final Object uniqueProperties = constructor.newInstance();
        final Method method = uniqueProperties.getClass().getDeclaredMethod("put", Object.class,
                Object.class);
        final Object result = method.invoke(uniqueProperties, 1, "value");
        final Map<Object, Object> table = new HashMap<Object, Object>();
        final Object expected = table.put(1, "value");
        assertEquals(expected, result);
        final Object result2 = method.invoke(uniqueProperties, 1, "value");
        final Object expected2 = table.put(1, "value");
        assertEquals(expected2, result2);
    }

    /**
     * Method generates FileNotFound exception details. It tries to open file,
     * that does not exist.
     * @param file to be opened
     * @return detail message of {@link FileNotFoundException}
     */
    private static String getFileNotFoundDetail(File file) throws Exception {
        // Create exception to know detail message we should wait in
        // LocalisedMessage
        try {
            final InputStream stream = new FileInputStream(file);
            stream.close();
            throw new IllegalStateException("File " + file.getPath() + " should not exist");
        }
        catch (FileNotFoundException ex) {
            return ex.getLocalizedMessage();
        }
    }
}

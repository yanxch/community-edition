/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.alfresco.config.ConfigElement;
import org.alfresco.config.ConfigException;
import org.alfresco.web.config.DefaultControlsConfigElement.ControlParam;

/**
 * JUnit tests to exercise the forms-related capabilities in to the web client
 * config service. These tests only include those that require a single config
 * xml file. Override-related tests, which use multiple config xml files, are
 * located in peer classes in this package.
 * 
 * @author Neil McErlean
 */
public class DefaultControlsConfigTest extends AbstractFormConfigTest
{
    @Override
    protected String getConfigXmlFile()
    {
        return "test-config-forms.xml";
    }
    
    @SuppressWarnings("unchecked")
	public void testDefaultControls_MappingNameToTemplate()
    {
        // Test that the default-control types are read from the config file
        Map<String, String> expectedDataMappings = new HashMap<String, String>();
        expectedDataMappings.put("d:text",
                "org/alfresco/forms/controls/textfield.ftl");
        expectedDataMappings.put("d:boolean",
                "org/alfresco/forms/controls/checkbox.ftl");
        expectedDataMappings.put("association",
                "org/alfresco/forms/controls/association-picker.ftl");
        expectedDataMappings.put("abc", "org/alfresco/abc.ftl");

        List<String> actualNames = defltCtrlsConfElement.getItemNames();
        assertEquals("Incorrect name count, expected "
                + expectedDataMappings.size(), expectedDataMappings.size(),
                actualNames.size());

        assertEquals(expectedDataMappings.keySet(), new HashSet(actualNames));
        
        // Test that the datatypes map to the expected template.
        for (String nextKey : expectedDataMappings.keySet())
        {
            String nextExpectedValue = expectedDataMappings.get(nextKey);
            String nextActualValue = defltCtrlsConfElement.getTemplateFor(nextKey);
            assertTrue("Incorrect template for " + nextKey + ": "
                    + nextActualValue, nextExpectedValue
                    .equals(nextActualValue));
        }
    }

    @SuppressWarnings("unchecked")
    public void testControlParams()
    {
        Map<String, List<ControlParam>> expectedControlParams = new HashMap<String, List<ControlParam>>();

        List<ControlParam> textParams = new ArrayList<ControlParam>();
        textParams.add(new ControlParam("size", "50"));

        List<ControlParam> abcParams = new ArrayList<ControlParam>();
        abcParams.add(new ControlParam("a", "1"));
        abcParams.add(new ControlParam("b", "Hello"));
        abcParams.add(new ControlParam("c", "For ever and ever."));
        abcParams.add(new ControlParam("d", ""));

        expectedControlParams.put("d:text", textParams);
        expectedControlParams.put("d:boolean", Collections.EMPTY_LIST);
        expectedControlParams.put("association", Collections.EMPTY_LIST);
        expectedControlParams.put("abc", abcParams);

        for (String name : expectedControlParams.keySet())
        {
            List<ControlParam> actualControlParams = defltCtrlsConfElement
                    .getControlParamsFor(name);
            assertEquals("Incorrect params for " + name, expectedControlParams
                    .get(name), actualControlParams);
        }
    }

    public void testDefaultControlsConfigElementShouldHaveNoChildren()
    {
        try
        {
            defltCtrlsConfElement.getChildren();
            fail("getChildren() did not throw an exception");
        } catch (ConfigException ce)
        {
            // expected exception
        }
    }

    /**
     * Tests the combination of a DefaultControlsConfigElement with another that
     * contains additional data.
     */
    public void testCombineDefaultControlsWithAddedParam()
    {
        DefaultControlsConfigElement basicElement = new DefaultControlsConfigElement();
        basicElement.addDataMapping("text", "path/textbox.ftl", null);

        // This element is the same as the above, but adds a control-param.
        DefaultControlsConfigElement parameterisedElement = new DefaultControlsConfigElement();
        List<ControlParam> testParams = new ArrayList<ControlParam>();
        testParams.add(new ControlParam("A", "1"));
        parameterisedElement.addDataMapping("text", "path/textbox.ftl",
                testParams);

        ConfigElement combinedElem = basicElement.combine(parameterisedElement);
        assertEquals("Combined elem incorrect.", parameterisedElement,
                combinedElem);
    }

    /**
     * Tests the combination of a DefaultControlsConfigElement with another that
     * contains modified data.
     */
    public void testCombineDefaultControlsWithModifiedParam()
    {
        DefaultControlsConfigElement initialElement = new DefaultControlsConfigElement();
        List<ControlParam> testParams = new ArrayList<ControlParam>();
        testParams.add(new ControlParam("A", "1"));
        initialElement.addDataMapping("text", "path/textbox.ftl", testParams);

        // This element is the same as the above, but modifies the
        // control-param.
        DefaultControlsConfigElement modifiedElement = new DefaultControlsConfigElement();
        List<ControlParam> modifiedTestParams = new ArrayList<ControlParam>();
        modifiedTestParams.add(new ControlParam("A", "5"));
        modifiedElement.addDataMapping("text", "path/textbox.ftl",
                modifiedTestParams);

        ConfigElement combinedElem = initialElement.combine(modifiedElement);
        assertEquals("Combined elem incorrect.", modifiedElement, combinedElem);
    }

    /**
     * Tests the combination of a DefaultControlsConfigElement with another that
     * contains deleted data.
     */
    public void testCombineDefaultControlsWithDeletedParam()
    {
        DefaultControlsConfigElement initialElement = new DefaultControlsConfigElement();
        List<ControlParam> testParams = new ArrayList<ControlParam>();
        testParams.add(new ControlParam("A", "1"));
        initialElement.addDataMapping("text", "path/textbox.ftl", testParams);

        // This element is the same as the above, but deletes the
        // control-param.
        DefaultControlsConfigElement modifiedElement = new DefaultControlsConfigElement();
        modifiedElement.addDataMapping("text", "path/textbox.ftl", null);

        ConfigElement combinedElem = initialElement.combine(modifiedElement);
        assertEquals("Combined elem incorrect.", modifiedElement, combinedElem);
    }
}

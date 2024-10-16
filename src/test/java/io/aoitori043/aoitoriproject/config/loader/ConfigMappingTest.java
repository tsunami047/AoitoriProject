package io.aoitori043.aoitoriproject.config.loader;

import java.util.*;
import java.math.*;

import org.bukkit.configuration.ConfigurationSection;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mockStatic;

@Ignore
@RunWith(MockitoJUnitRunner.class)
public class ConfigMappingTest {

    @InjectMocks
    private ConfigMapping configMapping;

    @Before
    public void setUp() {
        // Any setup can be placed here
    }

    @Test
    public void getAllFields_ValidClass_ShouldReturnAllFields() {
        // Arrange
        Class<?> testClass = TestConfig.class;
        Field[] expectedFields = testClass.getDeclaredFields();

        // Act
        Field[] actualFields = ConfigMapping.getAllFields(testClass);

        // Assert
        assertArrayEquals("The fields should match", expectedFields, actualFields);
    }

    @Test
    public void getAllFields_SubClass_ShouldReturnAllFieldsFromSubClassAndSuperClass() {
        // Arrange
        Class<?> testClass = SubTestConfig.class;
        Field[] expectedFields = testClass.getDeclaredFields();
        Field[] superFields = TestConfig.class.getDeclaredFields();
        List<Field> expectedFieldList = new ArrayList<>();
        expectedFieldList.addAll(Arrays.asList(expectedFields));
        expectedFieldList.addAll(Arrays.asList(superFields));

        // Act
        Field[] actualFields = ConfigMapping.getAllFields(testClass);

        // Assert
        assertTrue("The fields should contain all fields from the sub class and super class",
                Arrays.asList(actualFields).containsAll(expectedFieldList));
    }

    static class TestConfig {
        private String string;
        private int integer;
    }

    static class SubTestConfig extends TestConfig {
        private double doubleValue;
    }
}

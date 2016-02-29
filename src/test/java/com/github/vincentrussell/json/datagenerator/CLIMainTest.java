package com.github.vincentrussell.json.datagenerator;

import org.apache.commons.cli.ParseException;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import java.io.*;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class CLIMainTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Rule
    public SystemOutRule systemOutRule = new SystemOutRule().enableLog();

    File sourceFile;
    File destinationFile;


    @Before
    public void before() throws IOException {
        sourceFile = temporaryFolder.newFile();
        destinationFile = temporaryFolder.newFile();
    }


    @Test
    public void missingArgumentsThrowsExceptionAndPrintsHelp() throws IOException, JsonDataGeneratorException, ParseException {
        exception.expect(ParseException.class);
        exception.expectMessage("Missing required options: s, d");
        try {
            CLIMain.main(new String[]{""});
        } finally {
            assertThat(systemOutRule.getLog(), startsWith("usage: " + CLIMain.class.getName()));
        }
    }

    @Test(expected = FileNotFoundException.class)
    public void sourceFileNotFound() throws IOException, JsonDataGeneratorException, ParseException {
        sourceFile.delete();
        CLIMain.main(new String[]{"-s", sourceFile.getAbsolutePath(), "-d", destinationFile.getAbsolutePath()});
    }

    @Test(expected = IOException.class)
    public void destinationExists() throws IOException, JsonDataGeneratorException, ParseException {
        CLIMain.main(new String[]{"-s", sourceFile.getAbsolutePath(), "-d", destinationFile.getAbsolutePath()});
    }

    @Test
    public void successfulRun() throws IOException, JsonDataGeneratorException, ParseException {
        destinationFile.delete();
        try (FileOutputStream fileOutputStream = new FileOutputStream(sourceFile)) {
            IOUtils.write("{\n" +
                    "    \"id\": \"{{uuid()}}\",\n" +
                    "    \"name\": \"A green door\",\n" +
                    "    \"age\": {{integer(1,50)}},\n" +
                    "    \"price\": 12.50,\n" +
                    "    \"tags\": [\"home\", \"green\"]\n" +
                    "}", fileOutputStream);
            CLIMain.main(new String[]{"-s", sourceFile.getAbsolutePath(), "-d", destinationFile.getAbsolutePath()});
            assertTrue(destinationFile.exists());
            try (FileInputStream fileInputStream = new FileInputStream(destinationFile)) {
                List list = IOUtils.readLines(fileInputStream);
                assertEquals(7, list.size());
            }

        }
    }

}
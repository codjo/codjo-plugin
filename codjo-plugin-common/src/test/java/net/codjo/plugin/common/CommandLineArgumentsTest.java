/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.plugin.common;
import net.codjo.test.common.PathUtil;
import java.io.File;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import junit.framework.TestCase;
/**
 * Classe de test de {@link CommandLineArguments}.
 */
public class CommandLineArgumentsTest extends TestCase {
    public void test_noArguments() throws Exception {
        String[] noArgs = new String[]{};
        CommandLineArguments arguments = new CommandLineArguments(noArgs);
        assertEquals(0, arguments.size());
        assertEquals(null, arguments.getArgument("unknownProperty"));
    }


    public void test_aLotOfArguments() throws Exception {
        String[] twoArgs = new String[]{"-logfile", "c:/tmp", "-remote", "yes"};
        CommandLineArguments arguments = new CommandLineArguments(twoArgs);

        assertEquals(2, arguments.size());
        assertEquals("c:/tmp", arguments.getArgument("logfile"));
        assertEquals("yes", arguments.getArgument("remote"));
    }


    public void test_setArguments() throws Exception {
        String[] twoArgs = new String[]{"-logfile", "c:/tmp"};
        CommandLineArguments arguments = new CommandLineArguments(twoArgs);

        arguments.setArgument("logfile", "newValue");
        arguments.setArgument("remote", "no");

        assertEquals(2, arguments.size());
        assertEquals("newValue", arguments.getArgument("logfile"));
        assertEquals("no", arguments.getArgument("remote"));
    }


    public void test_aLotOfArgumentsMergedWithPropertyFile()
          throws Exception {
        String[] twoArgs = new String[]{"-login", "overridedLogin"};
        CommandLineArguments arguments = new CommandLineArguments(twoArgs);

        arguments.loadProperty(getPropertyFile());

        assertEquals(2, arguments.size());
        assertEquals("overridedLogin", arguments.getArgument("login"));
        assertEquals("myvalue", arguments.getArgument("property.defined.in.file"));
    }


    public void test_argumentWithoutValue() throws Exception {
        String[] twoArgs = new String[]{"-logfile", "c:/tmp", "-withoutvalue"};
        try {
            new CommandLineArguments(twoArgs);
            fail();
        }
        catch (IllegalArgumentException ex) {
            assertEquals("L'argument '-withoutvalue' ne possède pas de valeur.",
                         ex.getMessage());
        }
    }


    public void test_getDateArgument() throws Exception {
        String[] twoArgs =
              new String[]{"-aDate", "2006-01-20", "-notADate", "chuipasunedate"};
        CommandLineArguments arguments = new CommandLineArguments(twoArgs);

        assertEquals(java.sql.Date.valueOf("2006-01-20"),
                     arguments.getDateArgument("aDate"));

        try {
            arguments.getDateArgument("notADate");
            fail();
        }
        catch (IllegalArgumentException ex) {
            assertEquals(
                  "L'argument 'notADate' (chuipasunedate) n'est pas une date valide (format yyyy-mm-dd).",
                  ex.getMessage());
        }
    }


    public void test_getFileArgument() throws Exception {
        String[] twoArgs = new String[]{"-importFile", "/opt/file.txt"};
        CommandLineArguments arguments = new CommandLineArguments(twoArgs);

        assertEquals(new File("/opt/file.txt"), arguments.getFileArgument("importFile"));
    }


    public void test_contains() throws Exception {
        String[] twoArgs = new String[]{"-logfile", "c:/tmp"};
        CommandLineArguments arguments = new CommandLineArguments(twoArgs);

        assertTrue(arguments.contains("logfile"));
        assertFalse(arguments.contains("unknown"));
    }


    public void test_assertArgumentExists() throws Exception {
        String[] twoArgs = new String[]{"-logfile", "c:/tmp"};
        CommandLineArguments arguments = new CommandLineArguments(twoArgs);

        arguments.assertArgumentExists("logfile");

        try {
            arguments.assertArgumentExists("unknwonArg");
            fail();
        }
        catch (IllegalArgumentException ex) {
            assertEquals("L'argument obligatoire 'unknwonArg' est absent.",
                         ex.getMessage());
        }
    }


    public void test_assertFileArgumentExist() throws Exception {
        String[] twoArgs =
              new String[]{
                    "-srcDir",
                    PathUtil.findSrcDirectory(CommandLineArgumentsTest.class).getPath(),
                    "-notReallyAFile", "chuipasunfichier"
              };
        CommandLineArguments arguments = new CommandLineArguments(twoArgs);

        arguments.assertFileArgument("srcDir");

        try {
            arguments.assertFileArgument("notReallyAFile");
            fail();
        }
        catch (IllegalArgumentException ex) {
            assertEquals("L'argument 'notReallyAFile' (chuipasunfichier) n'est pas un fichier accessible.",
                         ex.getMessage());
        }
        try {
            arguments.assertFileArgument("unknwonArg");
            fail();
        }
        catch (IllegalArgumentException ex1) {
            assertEquals("L'argument obligatoire 'unknwonArg' est absent.",
                         ex1.getMessage());
        }
    }


    public void test_assertDateArgument() throws Exception {
        String[] twoArgs =
              new String[]{"-aDate", "2006-01-20", "-notADate", "chuipasunedate"};
        CommandLineArguments arguments = new CommandLineArguments(twoArgs);

        arguments.assertDateArgument("aDate");

        try {
            arguments.assertDateArgument("notADate");
            fail();
        }
        catch (IllegalArgumentException ex) {
            assertEquals(
                  "L'argument 'notADate' (chuipasunedate) n'est pas une date valide (format yyyy-mm-dd).",
                  ex.getMessage());
        }
        try {
            arguments.assertDateArgument("unknwonArg");
            fail();
        }
        catch (IllegalArgumentException ex1) {
            assertEquals("L'argument obligatoire 'unknwonArg' est absent.",
                         ex1.getMessage());
        }
    }


    public void test_getAllArguments() throws Exception {
        String[] args = new String[]{"-aDate", "2006-01-20", "-period", "200601", "-initiator", "badie"};
        CommandLineArguments arguments = new CommandLineArguments(args);
        Iterator iterator = arguments.getAllArguments();
        List<String> resultList = new ArrayList<String>();
        while (iterator.hasNext()) {
            resultList.add((String)iterator.next());
        }
        assertTrue(resultList.contains("aDate"));
        assertTrue(resultList.contains("period"));
        assertTrue(resultList.contains("initiator"));
    }


    public void test_toString() throws Exception {
        String[] twoArgs = new String[]{"-aDate", "2006-01-20"};
        CommandLineArguments arguments = new CommandLineArguments(twoArgs);

        assertEquals("{aDate=2006-01-20}", arguments.toString());
    }


    private File getPropertyFile() {
        URL resource = getClass().getResource("CommandLineArgumentsTest.properties");
        return new File(resource.getFile());
    }
}

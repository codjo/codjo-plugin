/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.plugin.common;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
/**
 * Représente une ligne d'argument.
 *
 * <p> Appel ligne de commande :
 * <pre>
 * java -jar myjar.jar -logfile c:\tmp
 * </pre>
 * </p>
 *
 * <p> Dans le code :
 * <pre>
 * public static void main(String[] args) {
 *   CommandLineArguments commandLineArguments = new CommandLineArguments(args);
 *   commandLineArguments.getArgument("logfile");
 * }
 * </pre>
 * </p>
 */
public class CommandLineArguments {
    private final Properties properties;


    public CommandLineArguments(String[] arguments) {
        properties = create(arguments);
    }


    public int size() {
        return properties.size();
    }


    public void loadProperty(File propertyFile) throws IOException {
        loadProperty(new FileInputStream(propertyFile));
    }


    public void loadProperty(InputStream inputStream) throws IOException {
        Properties fromFile = new Properties();
        try {
            fromFile.load(inputStream);
        }
        finally {
            inputStream.close();
        }

        fromFile.putAll(properties);
        properties.putAll(fromFile);
    }


    public String getArgument(String argumentName) {
        return properties.getProperty(argumentName);
    }


    public Iterator getAllArguments() {
        return properties.keySet().iterator();
    }


    public Date getDateArgument(String argument) {
        assertDateArgument(argument);
        return java.sql.Date.valueOf(getArgument(argument));
    }


    public File getFileArgument(String argument) {
        return new File(getArgument(argument));
    }


    public void setArgument(String argument, String value) {
        properties.setProperty(argument, value);
    }


    public boolean contains(String argument) {
        return properties.containsKey(argument);
    }


    public void assertArgumentExists(String argument) {
        if (!properties.containsKey(argument)) {
            throw new IllegalArgumentException("L'argument obligatoire '" + argument
                                               + "' est absent.");
        }
    }


    public void assertFileArgument(String argument) {
        assertArgumentExists(argument);
        File file = new File(getArgument(argument));
        if (!file.exists()) {
            throw new IllegalArgumentException("L'argument '" + argument + "' ("
                                               + properties.getProperty(argument)
                                               + ") n'est pas un fichier accessible.");
        }
    }


    public void assertDateArgument(String argument) {
        assertArgumentExists(argument);
        try {
            java.sql.Date.valueOf(getArgument(argument));
        }
        catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("L'argument '" + argument + "' ("
                                               + properties.getProperty(argument)
                                               + ") n'est pas une date valide (format yyyy-mm-dd).");
        }
    }


    private static Properties create(final String[] arguments) {
        assertThatParameterHasValue(arguments);

        Properties properties = new Properties();
        for (int i = 0; i + 1 < arguments.length; i += 2) {
            properties.put(arguments[i].substring(1), arguments[i + 1]);
        }
        return properties;
    }


    private static void assertThatParameterHasValue(String[] arguments) {
        if (arguments.length % 2 != 0) {
            throw new IllegalArgumentException("L'argument '"
                                               + arguments[arguments.length - 1]
                                               + "' ne possède pas de valeur.");
        }
    }


    @Override
    public String toString() {
        return properties.toString();
    }
}

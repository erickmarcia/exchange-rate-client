package ni.jug.cli;

import ni.jug.util.Strings;

/**
 *
 * @author Armando Alaniz
 * @version 1.0
 * @since 1.0
 */
public final class CLIHelper {

    private CLIHelper() {
    }

    private static boolean thereIsNoOptionIndicator(String namedArgument) {
        return !(namedArgument.startsWith(Strings.HYPHEN) || namedArgument.startsWith(Strings.DOUBLE_HYPHEN));
    }

    private static boolean thereIsNoAssignment(String argument) {
        return argument.indexOf(Strings.EQUAL) == -1;
    }

    private static void doValidateNamedArgument(String namedArgument) {
        if (thereIsNoOptionIndicator(namedArgument)) {
            throw new IllegalArgumentException("Para extraer el valor de una opción se debe usar guión o dos guiones en el "
                    + "nombre de la opción [" + namedArgument + "]");
        }
    }

    public static String searchValueOf(String namedArgument, String[] args) {
        doValidateNamedArgument(namedArgument);

        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith(namedArgument)) {
                if (thereIsNoAssignment(args[i])) {
                    throw new IllegalArgumentException("Error de sintaxis en parametro " + namedArgument + ", es necesario "
                            + "especificar un valor usando el signo igual (=)");
                }

                return Strings.substringAfter(args[i], Strings.EQUAL);
            }
        }

        return Strings.EMPTY;
    }

    public static boolean containsOption(String namedArgument, String[] args) {
        doValidateNamedArgument(namedArgument);

        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith(namedArgument)) {
                return true;
            }
        }
        return false;
    }

    public static class OptionRangeValue {

        private final String raw;
        private final String from;
        private final String to;

        public OptionRangeValue(String raw) {
            String[] rangeValues = Strings.splitCSVAndGetFirst2Elements(raw);
            this.raw = raw;
            this.from = rangeValues[0];
            this.to = rangeValues[1];
        }

        public String getRaw() {
            return raw;
        }

        public String getFrom() {
            return from;
        }

        public String getTo() {
            return to;
        }

    }

    public static class OptionListValue {

        private final String raw;
        private final Object[] values;
        private final int size;

        public OptionListValue(String raw) {
            this.raw = raw;
            this.values = process(raw);
            this.size = this.values.length;
        }

        private Object[] process(String raw) {
            Object[] result;

            if (Strings.containsComma(raw) || Strings.containsColon(raw)) {
                String[] values = Strings.splitCSV(raw);
                result = new Object[values.length];
                for (int i = 0; i < values.length; i++) {
                    String value = values[i];
                    if (Strings.containsColon(value)) {
                        result[i] = new OptionRangeValue(value);
                    } else {
                        result[i] = value;
                    }
                }
            } else {
                result = new Object[1];
                result[0] = raw;
            }

            return result;
        }

        public String getRaw() {
            return raw;
        }

        public Object[] getValues() {
            return values;
        }

        public int getSize() {
            return size;
        }

    }

}

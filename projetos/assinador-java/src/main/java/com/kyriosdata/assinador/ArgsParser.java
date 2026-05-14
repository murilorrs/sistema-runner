package com.kyriosdata.assinador;

/**
 * Utilitário para parsing de argumentos de linha de comando.
 */
public class ArgsParser {

    private ArgsParser() {}

    /**
     * Extrai o valor de uma flag nos argumentos.
     * @param args array de argumentos
     * @param flag nome da flag (ex: "--content")
     * @return valor após a flag, ou null se não encontrado
     */
    public static String stringArg(String[] args, String flag) {
        for (int i = 0; i < args.length - 1; i++) {
            if (args[i].equals(flag)) return args[i + 1];
        }
        return null;
    }

    /**
     * Extrai o valor numérico de uma flag nos argumentos.
     * @param args array de argumentos
     * @param flag nome da flag
     * @param defaultValue valor padrão se não encontrado ou inválido
     * @return valor parseado ou defaultValue em caso de erro
     */
    public static int intArg(String[] args, String flag, int defaultValue) {
        String val = stringArg(args, flag);
        if (val == null) return defaultValue;
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            System.err.println("Valor inválido para " + flag + ": " + val);
            System.exit(1);
            return defaultValue;
        }
    }

    /**
     * Verifica se uma flag está presente nos argumentos.
     * @param args array de argumentos
     * @param flag nome da flag
     * @return true se a flag está presente
     */
    public static boolean hasFlag(String[] args, String flag) {
        for (String a : args) if (a.equals(flag)) return true;
        return false;
    }
}

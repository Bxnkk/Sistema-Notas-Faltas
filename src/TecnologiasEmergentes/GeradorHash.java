package TecnologiasEmergentes;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class GeradorHash {

    public static String gerarHash(String dados) {

        try {
            MessageDigest digest =
                MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                dados.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                "Erro ao gerar hash", e
            );
        }
    }

    public static void main(String[] args) {

        String registro1 = "Nota cadastrada: 8.50";

        String hash1 = gerarHash(registro1);

        System.out.println("Registro 1: " + registro1);
        System.out.println("Hash 1: " + hash1);

        String registro2 = "Nota alterada: 9.00";

        String hash2 = gerarHash(hash1 + registro2);

        System.out.println();
        System.out.println("Registro 2: " + registro2);
        System.out.println("Hash anterior: " + hash1);
        System.out.println("Hash 2: " + hash2);
    }
}

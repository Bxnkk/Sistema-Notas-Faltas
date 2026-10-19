package TecnologiasEmergentes;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class SenhaSegura {

    private static final int ITERACOES = 600000;

    public static String gerarSalt() {

        byte[] salt = new byte[16];

        SecureRandom random = new SecureRandom();
        random.nextBytes(salt);

        return Base64.getEncoder().encodeToString(salt);
    }

    public static String gerarHash(String senha, String saltBase64)
            throws Exception {

        byte[] salt = Base64.getDecoder().decode(saltBase64);

        PBEKeySpec spec = new PBEKeySpec(
            senha.toCharArray(), salt, ITERACOES, 256
        );

        try {
            SecretKeyFactory fabrica =
                SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

            byte[] hash = fabrica.generateSecret(spec).getEncoded();

            return Base64.getEncoder().encodeToString(hash);

        } finally {
            spec.clearPassword();
        }
    }

    public static boolean verificarSenha(
            String senha, String salt, String hashSalvo)
            throws Exception {

        String hashDigitado = gerarHash(senha, salt);

        byte[] esperado = Base64.getDecoder().decode(hashSalvo);
        byte[] recebido = Base64.getDecoder().decode(hashDigitado);

        return MessageDigest.isEqual(esperado, recebido);
    }

    public static void main(String[] args) {

        try {

            String senhaTeste = "SenhaDeTeste123!";

            String salt = gerarSalt();
            String hash = gerarHash(senhaTeste, salt);

            System.out.println("Salt gerado com sucesso!");
            System.out.println("Hash gerado com sucesso!");

            boolean correta =
                verificarSenha(senhaTeste, salt, hash);

            boolean incorreta =
                verificarSenha("SenhaErrada", salt, hash);

            System.out.println("Senha correta: " + correta);
            System.out.println("Senha incorreta: " + incorreta);

        } catch (Exception e) {
            System.out.println("Erro ao processar senha:");
            e.printStackTrace();
        }
    }
}

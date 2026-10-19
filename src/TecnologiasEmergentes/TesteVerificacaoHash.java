package TecnologiasEmergentes;

public class TesteVerificacaoHash {

    public static void main(String[] args) {

        String registro1 = "Nota cadastrada: 8.50";
        String hash1 = GeradorHash.gerarHash(registro1);

        String registro2 = "Nota alterada: 9.00";
        String hash2 = GeradorHash.gerarHash(hash1 + registro2);

        boolean verificar1 =
            GeradorHash.gerarHash(registro1).equals(hash1);

        boolean verificar2 =
            GeradorHash.gerarHash(hash1 + registro2).equals(hash2);

        if (verificar1 && verificar2) {
            System.out.println("Cadeia de hashes íntegra!");
        } else {
            System.out.println("Alteração detectada!");
        }

        System.out.println();
        System.out.println("Simulando alteração no registro...");

        String registroAlterado = "Nota alterada: 10.00";

        String novoHash =
            GeradorHash.gerarHash(hash1 + registroAlterado);

        if (novoHash.equals(hash2)) {
            System.out.println("Registro íntegro!");
        } else {
            System.out.println(
                "ALERTA: O registro foi modificado!"
            );
        }
    }
}
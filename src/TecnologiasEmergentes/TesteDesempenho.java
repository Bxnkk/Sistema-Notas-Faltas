package TecnologiasEmergentes;

import java.sql.SQLException;

public class TesteDesempenho {

    public static void main(String[] args) {

        int repeticoes = 10;
        long tempoTotal = 0;

        try {

            for (int i = 0; i < 2; i++) {
                SistemaNF.consultarDados("PROFESSOR", 1, null);
            }

            for (int i = 1; i <= repeticoes; i++) {

                long inicio = System.nanoTime();

                int quantidade = SistemaNF.consultarDados(
                    "PROFESSOR", 1, null
                ).size();

                long fim = System.nanoTime();

                double tempoMs = (fim - inicio) / 1_000_000.0;

                tempoTotal += fim - inicio;

                System.out.printf(
                    "Teste %d: %.2f ms - %d aluno(s)%n",
                    i, tempoMs, quantidade
                );
            }

            double media = tempoTotal / 1_000_000.0 / repeticoes;

            System.out.println();
            System.out.printf(
                "Tempo médio das consultas: %.2f ms%n", media
            );

        } catch (SQLException e) {
            System.out.println("Erro durante o teste:");
            e.printStackTrace();
        }
    }
}

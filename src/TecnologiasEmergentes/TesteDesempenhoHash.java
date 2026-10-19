package TecnologiasEmergentes;

import java.sql.*;
import java.time.format.DateTimeFormatter;

public class TesteDesempenhoHash {

    public static int verificar() throws SQLException {

        String sql = "SELECT * FROM historico_alteracoes "
                   + "ORDER BY id_historico ASC";

        String hashEsperado = "0".repeat(64);
        int total = 0;

        try (
            Connection conexao = DriverManager.getConnection(
                SistemaNF.url,
                SistemaNF.usuarioBanco,
                SistemaNF.senhaBanco
            );
            PreparedStatement comando =
                conexao.prepareStatement(sql);
            ResultSet resultado = comando.executeQuery()
        ) {

            while (resultado.next()) {

                String tipo = resultado.getString("tipo_registro");
                int idRegistro = resultado.getInt("id_registro");
                String acao = resultado.getString("acao");
                String descricao = resultado.getString("descricao");

                String data = resultado.getTimestamp("data_registro")
                    .toLocalDateTime()
                    .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

                String anterior = resultado.getString("hash_anterior");
                String atual = resultado.getString("hash_atual");

                if (anterior == null || atual == null) {
                    throw new IllegalStateException("Hash ausente.");
                }

                String dados = tipo + "|" + idRegistro + "|"
                        + acao + "|" + descricao + "|"
                        + data + "|" + anterior;

                String calculado = GeradorHash.gerarHash(dados);

                if (!anterior.equals(hashEsperado)
                        || !atual.equals(calculado)) {
                    throw new IllegalStateException(
                        "Inconsistência no histórico."
                    );
                }

                hashEsperado = atual;
                total++;
            }
        }

        return total;
    }

    public static void main(String[] args) {

        int repeticoes = 10;
        long tempoTotal = 0;

        try {

            for (int i = 0; i < 2; i++) {
                verificar();
            }

            for (int i = 1; i <= repeticoes; i++) {

                long inicio = System.nanoTime();

                int registros = verificar();

                long fim = System.nanoTime();

                double tempoMs = (fim - inicio) / 1_000_000.0;

                tempoTotal += fim - inicio;

                System.out.printf(
                    "Teste %d: %.2f ms - %d registros íntegros%n",
                    i, tempoMs, registros
                );
            }

            double media = tempoTotal / 1_000_000.0 / repeticoes;

            System.out.println();

            System.out.printf(
                "Tempo médio da verificação: %.2f ms%n",
                media
            );

        } catch (SQLException | IllegalStateException e) {
            System.out.println("Erro durante a verificação:");
            System.out.println(e.getMessage());
        }
    }
}

package TecnologiasEmergentes;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

public class TesteVerificarHistoricoBanco {

    public static void main(String[] args) {

        String url = "jdbc:mysql://localhost:3306/sistema_notas_faltas";
        String usuario = System.getenv("MYSQL_APP_USER");
        String senha = System.getenv("MYSQL_APP_PASSWORD");

        String sql = "SELECT * FROM historico_alteracoes "
                   + "ORDER BY id_historico ASC";

        String hashAnteriorEsperado = "0".repeat(64);
        int total = 0;
        boolean integra = true;

        try (
            Connection conexao =
                DriverManager.getConnection(url, usuario, senha);
            PreparedStatement comando =
                conexao.prepareStatement(sql);
            ResultSet resultado = comando.executeQuery()
        ) {

            while (resultado.next()) {

                int id = resultado.getInt("id_historico");
                String tipo = resultado.getString("tipo_registro");
                int idRegistro = resultado.getInt("id_registro");
                String acao = resultado.getString("acao");
                String descricao = resultado.getString("descricao");

                String data = resultado.getTimestamp("data_registro")
                    .toLocalDateTime()
                    .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

                String hashAnterior = resultado.getString("hash_anterior");
                String hashAtual = resultado.getString("hash_atual");

                String dados = tipo + "|" + idRegistro + "|"
                        + acao + "|" + descricao + "|"
                        + data + "|" + hashAnterior;

                String hashCalculado = GeradorHash.gerarHash(dados);

                if (!hashAnterior.equals(hashAnteriorEsperado)
                        || !hashAtual.equals(hashCalculado)) {

                    System.out.println(
                        "ALERTA: Inconsistência no registro " + id
                    );

                    integra = false;
                    break;
                }

                System.out.println("Registro " + id + ": íntegro!");

                hashAnteriorEsperado = hashAtual;
                total++;
            }

            if (total == 0 && integra) {
                System.out.println("Nenhum registro encontrado.");
            } else if (integra) {
                System.out.println();
                System.out.println("Cadeia de hashes íntegra!");
                System.out.println("Registros verificados: " + total);
            }

        } catch (SQLException e) {
            System.out.println("Erro ao verificar histórico:");
            e.printStackTrace();
        }
    }
}
package TecnologiasEmergentes;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class TesteSalvarHistorico {

    public static void main(String[] args) {

        String url = "jdbc:mysql://localhost:3306/sistema_notas_faltas";
        String usuario = System.getenv("MYSQL_APP_USER");
        String senha = System.getenv("MYSQL_APP_PASSWORD");

        String tipo = "NOTA";
        int idRegistro = 1;
        String acao = "CADASTRO";
        String descricao = "Nota cadastrada: 8.50";

        LocalDateTime data = LocalDateTime.now()
                .truncatedTo(ChronoUnit.SECONDS);

        try (Connection conexao =
                DriverManager.getConnection(url, usuario, senha)) {

            String hashAnterior = "0".repeat(64);

            String consulta = "SELECT hash_atual FROM historico_alteracoes "
                    + "ORDER BY id_historico DESC LIMIT 1";

            try (PreparedStatement comando =
                    conexao.prepareStatement(consulta);
                 ResultSet resultado = comando.executeQuery()) {

                if (resultado.next()) {
                    hashAnterior = resultado.getString("hash_atual");
                }
            }

            String dados = tipo + "|" + idRegistro + "|"
                    + acao + "|" + descricao + "|"
                    + data.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    + "|" + hashAnterior;

            String hashAtual = GeradorHash.gerarHash(dados);

            String sql = "INSERT INTO historico_alteracoes "
                    + "(tipo_registro, id_registro, acao, descricao, "
                    + "data_registro, hash_anterior, hash_atual) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement comando =
                    conexao.prepareStatement(sql)) {

                comando.setString(1, tipo);
                comando.setInt(2, idRegistro);
                comando.setString(3, acao);
                comando.setString(4, descricao);
                comando.setTimestamp(5, Timestamp.valueOf(data));
                comando.setString(6, hashAnterior);
                comando.setString(7, hashAtual);

                comando.executeUpdate();

                System.out.println("Histórico salvo com sucesso!");
                System.out.println("Hash anterior: " + hashAnterior);
                System.out.println("Hash atual: " + hashAtual);
            }

        } catch (SQLException e) {
            System.out.println("Erro ao salvar histórico:");
            e.printStackTrace();
        }
    }
}
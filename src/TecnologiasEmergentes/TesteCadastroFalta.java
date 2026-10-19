package TecnologiasEmergentes;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TesteCadastroFalta {

    public static void main(String[] args) {

        String url = "jdbc:mysql://localhost:3306/sistema_notas_faltas";
        String usuario = System.getenv("MYSQL_APP_USER");
        String senha = System.getenv("MYSQL_APP_PASSWORD");

        String sql = "INSERT INTO falta (quantidade, id_aluno, id_disciplina) "
                   + "VALUES (?, ?, ?)";

        try (
            Connection conexao = DriverManager.getConnection(url, usuario, senha);
            PreparedStatement comando = conexao.prepareStatement(sql)
        ) {
            comando.setInt(1, 6);
            comando.setInt(2, 1);
            comando.setInt(3, 1);

            int linhasAfetadas = comando.executeUpdate();

            if (linhasAfetadas > 0) {
                System.out.println("Faltas cadastradas com sucesso!");
            }

        } catch (SQLException e) {
            System.out.println("Erro ao cadastrar as faltas:");
            e.printStackTrace();
        }
    }
}

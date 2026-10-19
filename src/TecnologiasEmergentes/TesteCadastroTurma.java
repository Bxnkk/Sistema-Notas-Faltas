package TecnologiasEmergentes;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TesteCadastroTurma {

    public static void main(String[] args) {

        String url = "jdbc:mysql://localhost:3306/sistema_notas_faltas";
        String usuario = System.getenv("MYSQL_APP_USER");
        String senha = System.getenv("MYSQL_APP_PASSWORD");

        String sql = "INSERT INTO turma (nome, id_professor, id_disciplina) "
                   + "VALUES (?, ?, ?)";

        try (
            Connection conexao = DriverManager.getConnection(url, usuario, senha);
            PreparedStatement comando = conexao.prepareStatement(sql)
        ) {
            comando.setString(1, "Turma A");
            comando.setInt(2, 1);
            comando.setInt(3, 1);

            int linhasAfetadas = comando.executeUpdate();

            if (linhasAfetadas > 0) {
                System.out.println("Turma cadastrada com sucesso!");
            }

        } catch (SQLException e) {
            System.out.println("Erro ao cadastrar a turma:");
            e.printStackTrace();
        }
    }
}
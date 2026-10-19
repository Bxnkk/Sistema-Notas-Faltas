package TecnologiasEmergentes;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TesteCadastroAluno {

    public static void main(String[] args) {

        String url = "jdbc:mysql://localhost:3306/sistema_notas_faltas";
        String usuario = System.getenv("MYSQL_APP_USER");
        String senha = System.getenv("MYSQL_APP_PASSWORD");

        String sql = "INSERT INTO aluno (nome, matricula, id_turma) "
                   + "VALUES (?, ?, ?)";

        try (
            Connection conexao = DriverManager.getConnection(url, usuario, senha);
            PreparedStatement comando = conexao.prepareStatement(sql)
        ) {
            comando.setString(1, "João Carlos");
            comando.setString(2, "5635562");
            comando.setInt(3, 1);

            int linhasAfetadas = comando.executeUpdate();

            if (linhasAfetadas > 0) {
                System.out.println("Aluno cadastrado com sucesso!");
            }

        } catch (SQLException e) {
            System.out.println("Erro ao cadastrar o aluno:");
            e.printStackTrace();
        }
    }
}

package TecnologiasEmergentes;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TesteCadastroDisciplina {

    public static void main(String[] args) {

        String url = "jdbc:mysql://localhost:3306/sistema_notas_faltas";
        String usuario = System.getenv("MYSQL_APP_USER");
        String senha = System.getenv("MYSQL_APP_PASSWORD");

        String sql = "INSERT INTO disciplina (nome) VALUES (?)";

        try (
            Connection conexao = DriverManager.getConnection(url, usuario, senha);
            PreparedStatement comando = conexao.prepareStatement(sql)
        ) {
            comando.setString(1, "Tecnologias Emergentes");

            int linhasAfetadas = comando.executeUpdate();

            if (linhasAfetadas > 0) {
                System.out.println(
                    "Disciplina cadastrada com sucesso!"
                );
            }

        } catch (SQLException e) {
            System.out.println("Erro ao cadastrar a disciplina:");
            e.printStackTrace();
        }
    }
}
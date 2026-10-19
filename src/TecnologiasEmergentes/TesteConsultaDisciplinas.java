package TecnologiasEmergentes;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class TesteConsultaDisciplinas {

    public static void main(String[] args) {

        String url = "jdbc:mysql://localhost:3306/sistema_notas_faltas";
        String usuario = System.getenv("MYSQL_APP_USER");
        String senha = System.getenv("MYSQL_APP_PASSWORD");

        String sql = "SELECT id_disciplina, nome FROM disciplina";

        try (
            Connection conexao = DriverManager.getConnection(url, usuario, senha);
            Statement comando = conexao.createStatement();
            ResultSet resultado = comando.executeQuery(sql)
        ) {
            boolean encontrou = false;

            while (resultado.next()) {
                encontrou = true;

                int id = resultado.getInt("id_disciplina");
                String nome = resultado.getString("nome");

                System.out.println(
                    "ID: " + id + " | Disciplina: " + nome
                );
            }

            if (!encontrou) {
                System.out.println(
                    "Conexão funcionando, mas não há disciplinas cadastradas."
                );
            }

        } catch (SQLException e) {
            System.out.println("Erro ao consultar as disciplinas:");
            e.printStackTrace();
        }
    }
}

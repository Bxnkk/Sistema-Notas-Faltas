package TecnologiasEmergentes;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TesteConexao {

    public static void main(String[] args) {

        String url = "jdbc:mysql://localhost:3306/sistema_notas_faltas";
        String usuario = System.getenv("MYSQL_APP_USER");
        String senha = System.getenv("MYSQL_APP_PASSWORD");

        try (Connection conexao =
                DriverManager.getConnection(url, usuario, senha)) {

            System.out.println(
                "Conexão com o MySQL realizada com sucesso!"
            );

        } catch (SQLException e) {

            System.out.println("Erro ao conectar com o MySQL.");
            e.printStackTrace();
        }
    }
}

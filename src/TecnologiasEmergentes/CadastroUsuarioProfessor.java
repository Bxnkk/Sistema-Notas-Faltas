package TecnologiasEmergentes;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;

public class CadastroUsuarioProfessor {

    public static void main(String[] args) {

        String url = "jdbc:mysql://localhost:3306/sistema_notas_faltas";
        String usuarioBanco = System.getenv("MYSQL_APP_USER");
        String senhaBanco = System.getenv("MYSQL_APP_PASSWORD");

        String sql = "INSERT INTO usuario "
                   + "(login, senha_hash, senha_salt, tipo, id_professor) "
                   + "VALUES (?, ?, ?, 'PROFESSOR', ?)";

        try (Scanner entrada = new Scanner(System.in)) {

            System.out.println("=== CADASTRO DE PROFESSOR ===");

            System.out.print("Digite o login do professor: ");
            String login = entrada.nextLine().trim();

            System.out.print("Crie uma senha de teste: ");
            String senha = entrada.nextLine();

            if (login.isEmpty() || senha.length() < 12) {
                System.out.println(
                    "Informe um login e uma senha com pelo menos 12 caracteres."
                );
                return;
            }

            String salt = SenhaSegura.gerarSalt();
            String hash = SenhaSegura.gerarHash(senha, salt);

            try (
                Connection conexao = DriverManager.getConnection(
                    url, usuarioBanco, senhaBanco
                );
                PreparedStatement comando =
                    conexao.prepareStatement(sql)
            ) {

                comando.setString(1, login);
                comando.setString(2, hash);
                comando.setString(3, salt);
                comando.setInt(4, 1);

                comando.executeUpdate();

                System.out.println();
                System.out.println("Professor cadastrado com sucesso!");
            }

        } catch (SQLException e) {
            System.out.println("Erro ao cadastrar usuário:");
            System.out.println(e.getMessage());

        } catch (Exception e) {
            System.out.println("Erro ao proteger a senha.");
        }
    }
}

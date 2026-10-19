package TecnologiasEmergentes;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Scanner;

public class Autenticacao {

    public static class Usuario {

        public String tipo;
        public int idProfessor;
        public int idAluno;

        public Usuario(String tipo, int idProfessor, int idAluno) {
            this.tipo = tipo;
            this.idProfessor = idProfessor;
            this.idAluno = idAluno;
        }
    }

    public static Usuario fazerLogin(Scanner entrada) {

        String url = "jdbc:mysql://localhost:3306/sistema_notas_faltas";
        String usuarioBanco = System.getenv("MYSQL_APP_USER");
        String senhaBanco = System.getenv("MYSQL_APP_PASSWORD");

        System.out.println("=== LOGIN DO SISTEMA ===");

        System.out.print("Login: ");
        String login = entrada.nextLine();

        System.out.print("Senha: ");
        String senha = entrada.nextLine();

        String sql = "SELECT * FROM usuario WHERE login = ?";

        try (
            Connection conexao = DriverManager.getConnection(
                url, usuarioBanco, senhaBanco
            );
            PreparedStatement comando =
                conexao.prepareStatement(sql)
        ) {

            comando.setString(1, login);

            try (ResultSet resultado = comando.executeQuery()) {

                if (resultado.next()) {

                    boolean senhaCorreta = SenhaSegura.verificarSenha(
                        senha,
                        resultado.getString("senha_salt"),
                        resultado.getString("senha_hash")
                    );

                    if (senhaCorreta) {

                        String tipo = resultado.getString("tipo");

                        int idProfessor = resultado.getInt("id_professor");
                        int idAluno = resultado.getInt("id_aluno");

                        System.out.println("Login realizado com sucesso!");

                        return new Usuario(tipo, idProfessor, idAluno);
                    }
                }
            }

            System.out.println("Login ou senha incorretos.");

        } catch (Exception e) {
            System.out.println("Erro ao realizar login.");
        }

        return null;
    }
}

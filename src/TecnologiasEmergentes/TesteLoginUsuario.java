package TecnologiasEmergentes;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class TesteLoginUsuario {

    public static void main(String[] args) {

        String url = "jdbc:mysql://localhost:3306/sistema_notas_faltas";
        String usuarioBanco = System.getenv("MYSQL_APP_USER");
        String senhaBanco = System.getenv("MYSQL_APP_PASSWORD");

        Scanner entrada = new Scanner(System.in);

        System.out.println("=== LOGIN DO SISTEMA ===");

        System.out.print("Digite seu login: ");
        String login = entrada.nextLine();

        System.out.print("Digite sua senha: ");
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

                    String hashSalvo =
                        resultado.getString("senha_hash");

                    String salt =
                        resultado.getString("senha_salt");

                    boolean senhaCorreta =
                        SenhaSegura.verificarSenha(
                            senha, salt, hashSalvo
                        );

                    if (senhaCorreta) {

                        String tipo =
                            resultado.getString("tipo");

                        System.out.println();
                        System.out.println("Login realizado com sucesso!");
                        System.out.println("Tipo de usuario: " + tipo);

                        if (tipo.equals("PROFESSOR")) {
                            System.out.println("Acesso de professor identificado.");
                        } else if (tipo.equals("ALUNO")) {
                            System.out.println("Acesso de aluno identificado.");
                        }

                    } else {
                        System.out.println("Login ou senha incorretos.");
                    }

                } else {
                    System.out.println("Login ou senha incorretos.");
                }
            }

        } catch (SQLException e) {
            System.out.println("Erro ao conectar com o banco:");
            e.printStackTrace();

        } catch (Exception e) {
            System.out.println("Erro ao verificar a senha.");
        }

        entrada.close();
    }
}

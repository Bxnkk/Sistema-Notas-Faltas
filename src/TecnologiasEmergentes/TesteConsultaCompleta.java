package TecnologiasEmergentes;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TesteConsultaCompleta {

    public static void main(String[] args) {

        String url = "jdbc:mysql://localhost:3306/sistema_notas_faltas";
        String usuario = System.getenv("MYSQL_APP_USER");
        String senha = System.getenv("MYSQL_APP_PASSWORD");

        String sql = """
            SELECT
                a.nome AS aluno,
                a.matricula,
                t.nome AS turma,
                p.nome AS professor,
                d.nome AS disciplina,
                n.valor AS nota,
                f.quantidade AS faltas
            FROM aluno a
            INNER JOIN turma t
                ON a.id_turma = t.id_turma
            INNER JOIN professor p
                ON t.id_professor = p.id_professor
            INNER JOIN disciplina d
                ON t.id_disciplina = d.id_disciplina
            LEFT JOIN nota n
                ON n.id_aluno = a.id_aluno
                AND n.id_disciplina = d.id_disciplina
            LEFT JOIN falta f
                ON f.id_aluno = a.id_aluno
                AND f.id_disciplina = d.id_disciplina
            """;

        try (
            Connection conexao =
                DriverManager.getConnection(url, usuario, senha);
            PreparedStatement comando =
                conexao.prepareStatement(sql);
            ResultSet resultado = comando.executeQuery()
        ) {
            boolean encontrou = false;

            while (resultado.next()) {
                encontrou = true;

                System.out.println("------------------------------");
                System.out.println("Aluno: " + resultado.getString("aluno"));
                System.out.println("Matrícula: " + resultado.getString("matricula"));
                System.out.println("Turma: " + resultado.getString("turma"));
                System.out.println("Professor: " + resultado.getString("professor"));
                System.out.println("Disciplina: " + resultado.getString("disciplina"));
                System.out.println("Nota: " + resultado.getString("nota"));
                System.out.println("Faltas: " + resultado.getString("faltas"));
            }

            if (!encontrou) {
                System.out.println("Nenhum registro encontrado.");
            }

        } catch (SQLException e) {
            System.out.println("Erro ao consultar os dados:");
            e.printStackTrace();
        }
    }
}

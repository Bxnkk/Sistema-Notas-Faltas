package TecnologiasEmergentes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Scanner;

public class AlteracaoNota {

    public static void executar(Scanner entrada, int idProfessor) {

        try {
            System.out.print("Digite o ID do aluno: ");
            int idAluno = Integer.parseInt(entrada.nextLine());

            System.out.print("Digite a nova nota: ");
            String valor = entrada.nextLine().replace(",", ".");

            BigDecimal novaNota = new BigDecimal(valor)
                    .setScale(2, RoundingMode.UNNECESSARY);

            if (novaNota.compareTo(BigDecimal.ZERO) < 0
                    || novaNota.compareTo(new BigDecimal("10")) > 0) {
                System.out.println("A nota deve estar entre 0 e 10.");
                return;
            }

            try (Connection conexao = DriverManager.getConnection(
                    SistemaNF.url,
                    SistemaNF.usuarioBanco,
                    SistemaNF.senhaBanco)) {

                conexao.setAutoCommit(false);

                try {
                    String consulta = """
                        SELECT n.id_nota, n.valor,
                               a.nome AS aluno,
                               d.nome AS disciplina
                        FROM nota n
                        JOIN aluno a ON n.id_aluno = a.id_aluno
                        JOIN turma t ON a.id_turma = t.id_turma
                        JOIN disciplina d
                            ON n.id_disciplina = d.id_disciplina
                        WHERE a.id_aluno = ?
                          AND t.id_professor = ?
                          AND n.id_disciplina = t.id_disciplina
                        ORDER BY n.id_nota DESC
                        LIMIT 1
                        FOR UPDATE
                        """;

                    int idNota;
                    BigDecimal notaAntiga;
                    String nomeAluno;

                    try (PreparedStatement comando =
                            conexao.prepareStatement(consulta)) {

                        comando.setInt(1, idAluno);
                        comando.setInt(2, idProfessor);

                        try (ResultSet resultado = comando.executeQuery()) {

                            if (!resultado.next()) {
                                System.out.println(
                                    "Nota não encontrada ou acesso não permitido."
                                );
                                conexao.rollback();
                                return;
                            }

                            idNota = resultado.getInt("id_nota");
                            notaAntiga = resultado.getBigDecimal("valor");
                            nomeAluno = resultado.getString("aluno");
                        }
                    }

                    if (notaAntiga.compareTo(novaNota) == 0) {
                        System.out.println("A nota já possui esse valor.");
                        conexao.rollback();
                        return;
                    }

                    String hashAnterior = "0".repeat(64);

                    String buscarHash = """
                        SELECT hash_atual
                        FROM historico_alteracoes
                        ORDER BY id_historico DESC
                        LIMIT 1
                        FOR UPDATE
                        """;

                    try (PreparedStatement comando =
                            conexao.prepareStatement(buscarHash);
                         ResultSet resultado = comando.executeQuery()) {

                        if (resultado.next()) {
                            hashAnterior = resultado.getString("hash_atual");
                        }
                    }

                    String atualizar =
                        "UPDATE nota SET valor = ? WHERE id_nota = ?";

                    try (PreparedStatement comando =
                            conexao.prepareStatement(atualizar)) {

                        comando.setBigDecimal(1, novaNota);
                        comando.setInt(2, idNota);
                        comando.executeUpdate();
                    }

                    String tipo = "NOTA";
                    String acao = "ALTERACAO";

                    String descricao = "Nota alterada: "
                            + notaAntiga.toPlainString()
                            + " -> "
                            + novaNota.toPlainString();

                    LocalDateTime data = LocalDateTime.now()
                            .truncatedTo(ChronoUnit.SECONDS);

                    String dados = tipo + "|" + idNota + "|"
                            + acao + "|" + descricao + "|"
                            + data.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                            + "|" + hashAnterior;

                    String hashAtual = GeradorHash.gerarHash(dados);

                    String inserir = """
                        INSERT INTO historico_alteracoes
                        (tipo_registro, id_registro, acao,
                         descricao, data_registro,
                         hash_anterior, hash_atual)
                        VALUES (?, ?, ?, ?, ?, ?, ?)
                        """;

                    try (PreparedStatement comando =
                            conexao.prepareStatement(inserir)) {

                        comando.setString(1, tipo);
                        comando.setInt(2, idNota);
                        comando.setString(3, acao);
                        comando.setString(4, descricao);
                        comando.setTimestamp(5, Timestamp.valueOf(data));
                        comando.setString(6, hashAnterior);
                        comando.setString(7, hashAtual);

                        comando.executeUpdate();
                    }

                    conexao.commit();

                    System.out.println();
                    System.out.println("Nota alterada com sucesso!");
                    System.out.println("Aluno: " + nomeAluno);
                    System.out.println("Nota anterior: " + notaAntiga);
                    System.out.println("Nova nota: " + novaNota);
                    System.out.println("Histórico registrado com SHA-256!");

                } catch (Exception e) {
                    conexao.rollback();
                    throw e;
                }
            }

        } catch (NumberFormatException | ArithmeticException e) {
            System.out.println("Informe um ID e uma nota válidos.");

        } catch (Exception e) {
            System.out.println("Erro ao alterar a nota.");
            System.out.println(e.getMessage());
        }
    }
}

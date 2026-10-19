package TecnologiasEmergentes;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Scanner;

public class AlteracaoFalta {

    public static void executar(Scanner entrada, int idProfessor) {

        try {
            System.out.print("Digite o ID do aluno: ");
            int idAluno = Integer.parseInt(entrada.nextLine());

            System.out.print("Digite a nova quantidade de faltas: ");
            int novaQuantidade = Integer.parseInt(entrada.nextLine());

            if (novaQuantidade < 0) {
                System.out.println("A quantidade não pode ser negativa.");
                return;
            }

            if (novaQuantidade > 120) {
                System.out.println("A quantidade não pode ultrapassar 120 aulas.");
                return;
            }

            try (Connection conexao = DriverManager.getConnection(
                    SistemaNF.url,
                    SistemaNF.usuarioBanco,
                    SistemaNF.senhaBanco)) {

                conexao.setAutoCommit(false);

                try {

                    String consulta = """
                        SELECT f.id_falta, f.quantidade,
                               a.nome AS aluno
                        FROM falta f
                        JOIN aluno a ON f.id_aluno = a.id_aluno
                        JOIN turma t ON a.id_turma = t.id_turma
                        WHERE a.id_aluno = ?
                          AND t.id_professor = ?
                          AND f.id_disciplina = t.id_disciplina
                        ORDER BY f.id_falta DESC
                        LIMIT 1
                        FOR UPDATE
                        """;

                    int idFalta;
                    int quantidadeAntiga;
                    String nomeAluno;

                    try (PreparedStatement comando =
                            conexao.prepareStatement(consulta)) {

                        comando.setInt(1, idAluno);
                        comando.setInt(2, idProfessor);

                        try (ResultSet resultado = comando.executeQuery()) {

                            if (!resultado.next()) {
                                System.out.println(
                                    "Registro não encontrado ou acesso não permitido."
                                );
                                conexao.rollback();
                                return;
                            }

                            idFalta = resultado.getInt("id_falta");
                            quantidadeAntiga = resultado.getInt("quantidade");
                            nomeAluno = resultado.getString("aluno");
                        }
                    }

                    if (quantidadeAntiga == novaQuantidade) {
                        System.out.println(
                            "A quantidade de faltas já possui esse valor."
                        );
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
                        "UPDATE falta SET quantidade = ? WHERE id_falta = ?";

                    try (PreparedStatement comando =
                            conexao.prepareStatement(atualizar)) {

                        comando.setInt(1, novaQuantidade);
                        comando.setInt(2, idFalta);
                        comando.executeUpdate();
                    }

                    String tipo = "FALTA";
                    String acao = "ALTERACAO";

                    String descricao = "Faltas alteradas: "
                            + quantidadeAntiga
                            + " -> "
                            + novaQuantidade;

                    LocalDateTime data = LocalDateTime.now()
                            .truncatedTo(ChronoUnit.SECONDS);

                    String dados = tipo + "|" + idFalta + "|"
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
                        comando.setInt(2, idFalta);
                        comando.setString(3, acao);
                        comando.setString(4, descricao);
                        comando.setTimestamp(5, Timestamp.valueOf(data));
                        comando.setString(6, hashAnterior);
                        comando.setString(7, hashAtual);

                        comando.executeUpdate();
                    }

                    conexao.commit();

                    System.out.println();
                    System.out.println("Faltas alteradas com sucesso!");
                    System.out.println("Aluno: " + nomeAluno);
                    System.out.println(
                        "Quantidade anterior: " + quantidadeAntiga
                    );
                    System.out.println(
                        "Nova quantidade: " + novaQuantidade
                    );
                    System.out.println(
                        "Histórico registrado com SHA-256!"
                    );

                } catch (Exception e) {
                    conexao.rollback();
                    throw e;
                }
            }

        } catch (NumberFormatException e) {
            System.out.println("Informe números inteiros válidos.");

        } catch (Exception e) {
            System.out.println("Erro ao alterar as faltas.");
            System.out.println(e.getMessage());
        }
    }
}

package TecnologiasEmergentes;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Scanner;

public class AlteracaoFalta {

	public static void executar(Scanner entrada, int idProfessor) {

		try {
			System.out.print("Digite a matrícula do aluno: ");
			String matricula = entrada.nextLine().trim();

			if (matricula.isEmpty()) {
				System.out.println("Informe uma matrícula válida.");
				return;
			}

			int idAluno;

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

			try (Connection conexao = DriverManager.getConnection(SistemaNF.url, SistemaNF.usuarioBanco,
					SistemaNF.senhaBanco)) {

				conexao.setAutoCommit(false);

				try {

					String consulta = """
							SELECT f.id_falta, f.quantidade,
							       a.id_aluno AS id_aluno,
							       a.nome AS aluno,
							       t.id_disciplina AS id_disciplina
							FROM aluno a
							JOIN turma t ON a.id_turma = t.id_turma
							LEFT JOIN falta f
							    ON f.id_aluno = a.id_aluno
							    AND f.id_disciplina = t.id_disciplina
							WHERE a.matricula = ?
							  AND t.id_professor = ?
							ORDER BY f.id_falta DESC
							LIMIT 1
							FOR UPDATE
							""";

					Integer idFalta;
					int idDisciplina;
					Integer quantidadeAntiga;
					String nomeAluno;

					try (PreparedStatement comando = conexao.prepareStatement(consulta)) {

						comando.setString(1, matricula);
						comando.setInt(2, idProfessor);

						try (ResultSet resultado = comando.executeQuery()) {

							if (!resultado.next()) {
								System.out.println("Registro não encontrado ou acesso não permitido.");
								conexao.rollback();
								return;
							}

							idAluno = resultado.getInt("id_aluno");
							idFalta = resultado.getObject("id_falta", Integer.class);
							idDisciplina = resultado.getInt("id_disciplina");
							quantidadeAntiga = resultado.getObject("quantidade", Integer.class);
							nomeAluno = resultado.getString("aluno");
						}
					}

					if (idFalta != null && quantidadeAntiga != null && quantidadeAntiga == novaQuantidade) {
						System.out.println("A quantidade de faltas já possui esse valor.");
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

					try (PreparedStatement comando = conexao.prepareStatement(buscarHash);
							ResultSet resultado = comando.executeQuery()) {

						if (resultado.next()) {
							hashAnterior = resultado.getString("hash_atual");
						}
					}

					boolean cadastro = idFalta == null;
					if (cadastro) {

						String inserirFalta = "INSERT INTO falta (quantidade, id_aluno, id_disciplina) VALUES (?, ?, ?)";

						try (PreparedStatement comando = conexao.prepareStatement(inserirFalta,
								Statement.RETURN_GENERATED_KEYS)) {

							comando.setInt(1, novaQuantidade);
							comando.setInt(2, idAluno);
							comando.setInt(3, idDisciplina);
							comando.executeUpdate();

							try (ResultSet chaves = comando.getGeneratedKeys()) {
								if (chaves.next()) {
									idFalta = chaves.getInt(1);
								} else {
									throw new SQLException("Não foi possível obter o ID do novo registro de faltas.");
								}
							}
						}

					} else {

						String atualizar = "UPDATE falta SET quantidade = ? WHERE id_falta = ?";

						try (PreparedStatement comando = conexao.prepareStatement(atualizar)) {

							comando.setInt(1, novaQuantidade);
							comando.setInt(2, idFalta);
							comando.executeUpdate();
						}
					}

					String tipo = "FALTA";
					String acao = cadastro ? "CADASTRO" : "ALTERACAO";

					String descricao;

					if (cadastro) {
						descricao = "Faltas cadastradas: " + novaQuantidade;
					} else {
						descricao = "Faltas alteradas: " + quantidadeAntiga + " -> " + novaQuantidade;
					}

					descricao += " | Professor ID: " + idProfessor;

					LocalDateTime data = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

					String dados = tipo + "|" + idFalta + "|" + acao + "|" + descricao + "|"
							+ data.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "|" + hashAnterior;

					String hashAtual = GeradorHash.gerarHash(dados);

					String inserir = """
							INSERT INTO historico_alteracoes
							(tipo_registro, id_registro, acao,
							 descricao, data_registro,
							 hash_anterior, hash_atual)
							VALUES (?, ?, ?, ?, ?, ?, ?)
							""";

					try (PreparedStatement comando = conexao.prepareStatement(inserir)) {

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

					if (cadastro) {
						System.out.println("Faltas cadastradas com sucesso!");
						System.out.println("Aluno: " + nomeAluno);
						System.out.println("Quantidade cadastrada: " + novaQuantidade);
					} else {
						System.out.println("Faltas alteradas com sucesso!");
						System.out.println("Aluno: " + nomeAluno);
						System.out.println("Quantidade anterior: " + quantidadeAntiga);
						System.out.println("Nova quantidade: " + novaQuantidade);
					}

					System.out.println("Histórico registrado com SHA-256!");

				} catch (Exception e) {
					conexao.rollback();
					throw e;
				}
			}

		} catch (NumberFormatException e) {
			System.out.println("Informe uma quantidade de faltas válida.");

		} catch (Exception e) {
			System.out.println("Erro ao alterar as faltas.");
			System.out.println(e.getMessage());
		}
	}
}
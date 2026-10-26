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
			System.out.print("Digite a matrícula do aluno: ");
			String matricula = entrada.nextLine().trim();

			if (matricula.isEmpty()) {
				System.out.println("Informe uma matrícula válida.");
				return;
			}

			int idAluno;

			System.out.print("Digite a nova nota: ");
			String valor = entrada.nextLine().replace(",", ".");

			BigDecimal novaNota = new BigDecimal(valor).setScale(2, RoundingMode.UNNECESSARY);

			if (novaNota.compareTo(BigDecimal.ZERO) < 0 || novaNota.compareTo(new BigDecimal("10")) > 0) {
				System.out.println("A nota deve estar entre 0 e 10.");
				return;
			}

			try (Connection conexao = DriverManager.getConnection(SistemaNF.url, SistemaNF.usuarioBanco,
					SistemaNF.senhaBanco)) {

				conexao.setAutoCommit(false);

				try {
					String consulta = """
							SELECT n.id_nota, n.valor,
							a.id_aluno AS id_aluno,
							a.nome AS aluno,
							t.id_disciplina AS id_disciplina
							FROM aluno a
							JOIN turma t ON a.id_turma = t.id_turma
							   LEFT JOIN nota n
							    ON n.id_aluno = a.id_aluno
							    AND n.id_disciplina = t.id_disciplina
							WHERE a.matricula = ?
							  AND t.id_professor = ?
							ORDER BY n.id_nota DESC
							LIMIT 1
							FOR UPDATE
							""";

					Integer idNota;
					int idDisciplina;
					BigDecimal notaAntiga;
					String nomeAluno;

					try (PreparedStatement comando = conexao.prepareStatement(consulta)) {

						comando.setString(1, matricula);
						comando.setInt(2, idProfessor);

						try (ResultSet resultado = comando.executeQuery()) {

							if (!resultado.next()) {
								System.out.println("Nota não encontrada ou acesso não permitido.");
								conexao.rollback();
								return;
							}

							idAluno = resultado.getInt("id_aluno");
							idNota = resultado.getObject("id_nota", Integer.class);
							idDisciplina = resultado.getInt("id_disciplina");
							notaAntiga = resultado.getBigDecimal("valor");
							nomeAluno = resultado.getString("aluno");
						}
					}

					if (idNota != null && notaAntiga != null && notaAntiga.compareTo(novaNota) == 0) {
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

					try (PreparedStatement comando = conexao.prepareStatement(buscarHash);
							ResultSet resultado = comando.executeQuery()) {

						if (resultado.next()) {
							hashAnterior = resultado.getString("hash_atual");
						}
					}

					boolean cadastro = idNota == null;
					if (idNota == null) {

						String inserirNota = "INSERT INTO nota (valor, id_aluno, id_disciplina) VALUES (?, ?, ?)";

						try (PreparedStatement comando = conexao.prepareStatement(inserirNota,
								Statement.RETURN_GENERATED_KEYS)) {

							comando.setBigDecimal(1, novaNota);
							comando.setInt(2, idAluno);
							comando.setInt(3, idDisciplina);
							comando.executeUpdate();

							try (ResultSet chaves = comando.getGeneratedKeys()) {
								if (chaves.next()) {
									idNota = chaves.getInt(1);
								} else {
									throw new SQLException("Não foi possível obter o ID da nova nota.");
								}
							}
						}

					} else {

						String atualizar = "UPDATE nota SET valor = ? WHERE id_nota = ?";

						try (PreparedStatement comando = conexao.prepareStatement(atualizar)) {

							comando.setBigDecimal(1, novaNota);
							comando.setInt(2, idNota);
							comando.executeUpdate();
						}
					}

					String tipo = "NOTA";
					String acao = cadastro ? "CADASTRO" : "ALTERACAO";

					String descricao;

					if (cadastro) {
						descricao = "Nota cadastrada: " + novaNota.toPlainString();
					} else {
						descricao = "Nota alterada: " + notaAntiga.toPlainString() + " -> " + novaNota.toPlainString();
					}

					descricao += " | Professor ID: " + idProfessor;

					LocalDateTime data = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

					String dados = tipo + "|" + idNota + "|" + acao + "|" + descricao + "|"
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

					if (cadastro) {
						System.out.println("Nota cadastrada com sucesso!");
						System.out.println("Aluno: " + nomeAluno);
						System.out.println("Nota cadastrada: " + novaNota);
					} else {
						System.out.println("Nota alterada com sucesso!");
						System.out.println("Aluno: " + nomeAluno);
						System.out.println("Nota anterior: " + notaAntiga);
						System.out.println("Nova nota: " + novaNota);
					}

					System.out.println("Histórico registrado com SHA-256!");

				} catch (Exception e) {
					conexao.rollback();
					throw e;
				}
			}

		} catch (NumberFormatException | ArithmeticException e) {
		    System.out.println("Informe uma nota válida, com até duas casas decimais.");

		} catch (Exception e) {
			System.out.println("Erro ao alterar a nota.");
			System.out.println(e.getMessage());
		}
	}
}

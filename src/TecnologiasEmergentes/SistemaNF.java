package TecnologiasEmergentes;

import java.util.logging.Logger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class SistemaNF {
	private static final Logger logger = Logger.getLogger(SistemaNF.class.getName());
	static String url = "jdbc:mysql://localhost:3306/sistema_notas_faltas";
	static String usuarioBanco = System.getenv("MYSQL_APP_USER");
	static String senhaBanco = System.getenv("MYSQL_APP_PASSWORD");

	static class DadosAluno {
		String nome;
		String matricula;
		String turma;
		String disciplina;
		BigDecimal nota;
		int faltas;
	}

	public static List<DadosAluno> consultarDados(String tipo, int idUsuario, String nomeBusca) throws SQLException {

		List<DadosAluno> alunos = new ArrayList<>();

		if (idUsuario <= 0) {
			throw new SQLException("Usuário sem vínculo válido.");
		}

		String sql = """
				        SELECT
				a.nome AS aluno,
				a.matricula AS matricula,
				t.nome AS turma,
				d.nome AS disciplina,

				            (SELECT n.valor
				             FROM nota n
				             WHERE n.id_aluno = a.id_aluno
				               AND n.id_disciplina = t.id_disciplina
				             ORDER BY n.id_nota DESC
				             LIMIT 1) AS nota,

				            COALESCE(
				                (SELECT f.quantidade
				                 FROM falta f
				                 WHERE f.id_aluno = a.id_aluno
				                   AND f.id_disciplina = t.id_disciplina
				                 ORDER BY f.id_falta DESC
				                 LIMIT 1),
				                0
				            ) AS faltas

				        FROM aluno a

				        INNER JOIN turma t
				            ON a.id_turma = t.id_turma

				        INNER JOIN disciplina d
				            ON t.id_disciplina = d.id_disciplina
				        """;

		if (tipo.equals("PROFESSOR")) {
			sql += " WHERE t.id_professor = ?";
		} else if (tipo.equals("ALUNO")) {
			sql += " WHERE a.id_aluno = ?";
		} else {
			throw new SQLException("Tipo de usuário inválido.");
		}

		if (tipo.equals("PROFESSOR") && nomeBusca != null) {
			sql += " AND a.nome LIKE ?";
		}

		sql += " ORDER BY a.nome";

		try (Connection conexao = DriverManager.getConnection(url, usuarioBanco, senhaBanco);
				PreparedStatement comando = conexao.prepareStatement(sql)) {

			comando.setInt(1, idUsuario);

			if (tipo.equals("PROFESSOR") && nomeBusca != null) {
				comando.setString(2, "%" + nomeBusca.trim() + "%");
			}

			try (ResultSet resultado = comando.executeQuery()) {

				while (resultado.next()) {

					DadosAluno aluno = new DadosAluno();

					aluno.nome = resultado.getString("aluno");
					aluno.matricula = resultado.getString("matricula");
					aluno.turma = resultado.getString("turma");
					aluno.disciplina = resultado.getString("disciplina");
					aluno.nota = resultado.getBigDecimal("nota");
					aluno.faltas = resultado.getInt("faltas");

					alunos.add(aluno);
				}
			}
		}

		return alunos;
	}

	public static void mostrarDados(List<DadosAluno> alunos, int opcao) {

		if (alunos.isEmpty()) {
			System.out.println("Nenhum aluno encontrado.");
			return;
		}

		for (DadosAluno aluno : alunos) {

			String nota = aluno.nota == null ? "Não cadastrada" : aluno.nota.toPlainString();

			switch (opcao) {

			case 1:
				System.out.println("Aluno: " + aluno.nome + " | Matrícula: " + aluno.matricula);
				break;

			case 2:
				System.out.println("Aluno encontrado!");
				System.out.println("Nome: " + aluno.nome);
				System.out.println("Turma: " + aluno.turma);
				System.out.println("Disciplina: " + aluno.disciplina);
				break;

			case 3:
				System.out.println("Aluno: " + aluno.nome);
				System.out.println("Nota: " + nota);
				break;

			case 4:
				System.out.println("Aluno: " + aluno.nome);
				System.out.println("Faltas: " + aluno.faltas);
				break;

			case 5:

				System.out.println("\n--- SITUAÇÃO DO ALUNO ---");
				System.out.println("Aluno: " + aluno.nome);
				System.out.println("Nota: " + nota);
				System.out.println("Faltas: " + aluno.faltas);

				int totalAulas = 120;
				double frequencia = 100.0 * (totalAulas - aluno.faltas) / totalAulas;

				System.out.printf("Frequência: %.2f%%%n", frequencia);

				if (aluno.nota == null) {

					System.out.println("Situação: Indefinida");

				} else {

					boolean notaSuficiente = aluno.nota.compareTo(new BigDecimal("6.0")) >= 0;

					boolean faltasSuficientes = aluno.faltas <= 30;

					if (notaSuficiente && faltasSuficientes) {
						System.out.println("Situação: Aprovado");

					} else if (!notaSuficiente && !faltasSuficientes) {
						System.out.println("Situação: Reprovado por nota e faltas");

					} else if (!notaSuficiente) {
						System.out.println("Situação: Reprovado por nota");

					} else {
						System.out.println("Situação: Reprovado por faltas");
					}
				}

				break;
			}
		}
	}

	private static void registrarErroConsulta(SQLException e) {
		System.out.println("Não foi possível consultar os dados. Tente novamente.");

		logger.warning("Erro na consulta ao MySQL. SQLState: " + e.getSQLState() + " | Código: " + e.getErrorCode());
	}

	public static int lerOpcao(Scanner entrada) {
		System.out.print("Digite uma opção: ");

		try {
			return Integer.parseInt(entrada.nextLine().trim());
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	public static void menuProfessor(Scanner entrada, int idProfessor) {

		int opcao = 0;

		do {
			System.out.println("\n================================");
			System.out.println("       MENU DO PROFESSOR");
			System.out.println("================================");
			System.out.println("1 - Listar alunos");
			System.out.println("2 - Buscar aluno");
			System.out.println("3 - Consultar notas");
			System.out.println("4 - Consultar faltas");
			System.out.println("5 - Ver situação do aluno");
			System.out.println("6 - Alterar nota");
			System.out.println("7 - Alterar faltas");
			System.out.println("8 - Verificar integridade do histórico");
			System.out.println("9 - Sair");
			System.out.println("================================");

			opcao = lerOpcao(entrada);

			if (opcao >= 1 && opcao <= 5) {

				String nomeBusca = null;

				if (opcao != 1) {
					System.out.print("Digite o nome do aluno: ");
					nomeBusca = entrada.nextLine().trim();
				}

				try {
					List<DadosAluno> alunos = consultarDados("PROFESSOR", idProfessor, nomeBusca);

					mostrarDados(alunos, opcao);

				} catch (SQLException e) {
					registrarErroConsulta(e);
				}

			} else if (opcao == 6) {

				AlteracaoNota.executar(entrada, idProfessor);

			} else if (opcao == 7) {

				AlteracaoFalta.executar(entrada, idProfessor);

			} else if (opcao == 8) {

				VerificacaoHistorico.executar();

			} else if (opcao == 9) {

				System.out.println("Saindo do sistema...");

			} else {

				System.out.println("Opção inválida.");
			}

		} while (opcao != 9);
	}

	public static void menuAluno(Scanner entrada, int idAluno) {

		int opcao = 0;

		do {
			System.out.println("\n================================");
			System.out.println("         MENU DO ALUNO");
			System.out.println("================================");
			System.out.println("1 - Consultar minhas notas");
			System.out.println("2 - Consultar minhas faltas");
			System.out.println("3 - Ver minha situação");
			System.out.println("4 - Sair");
			System.out.println("================================");

			opcao = lerOpcao(entrada);

			if (opcao >= 1 && opcao <= 3) {

				try {
					List<DadosAluno> alunos = consultarDados("ALUNO", idAluno, null);

					mostrarDados(alunos, opcao + 2);

				} catch (SQLException e) {
					registrarErroConsulta(e);
				}

			} else if (opcao == 4) {
				System.out.println("Saindo do sistema...");
			} else {
				System.out.println("Opção inválida.");
			}

		} while (opcao != 4);
	}

	public static void main(String[] args) {

		Scanner entrada = new Scanner(System.in);

		Autenticacao.Usuario usuario = Autenticacao.fazerLogin(entrada);

		if (usuario == null) {
			System.out.println("Acesso não autorizado.");
			entrada.close();
			return;
		}

		if (usuario.tipo.equals("PROFESSOR") && usuario.idProfessor > 0) {

			menuProfessor(entrada, usuario.idProfessor);

		} else if (usuario.tipo.equals("ALUNO") && usuario.idAluno > 0) {

			menuAluno(entrada, usuario.idAluno);

		} else {
			System.out.println("Usuário sem permissão válida.");
		}

		entrada.close();
	}
}

package TecnologiasEmergentes;

import java.math.BigDecimal;
import java.util.List;

public class TesteLimiteFaltas {

    public static void main(String[] args) {

        SistemaNF.DadosAluno aluno = new SistemaNF.DadosAluno();

        aluno.nome = "Aluno Teste";
        aluno.nota = new BigDecimal("9.00");

        System.out.println("=== TESTE COM 30 FALTAS ===");
        aluno.faltas = 30;
        SistemaNF.mostrarDados(List.of(aluno), 5);

        System.out.println("\n=== TESTE COM 31 FALTAS ===");
        aluno.faltas = 31;
        SistemaNF.mostrarDados(List.of(aluno), 5);

        System.out.println("\n=== TESTE COM NOTA INSUFICIENTE ===");
        aluno.nota = new BigDecimal("5.00");
        aluno.faltas = 16;
        SistemaNF.mostrarDados(List.of(aluno), 5);
    }
}

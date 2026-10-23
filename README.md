# Sistema de Notas e Faltas

Projeto desenvolvido em Java e MySQL para o gerenciamento de notas e faltas de alunos.

## Funcionalidades

- Login de professores e alunos.
- Controle de acesso por tipo de usuário.
- Listagem e busca de alunos pelo professor, incluindo busca por parte do nome.
- Consulta de notas, faltas e situação acadêmica.
- Cadastro da primeira nota de alunos que ainda não possuem nota.
- Cadastro e alteração de notas e faltas pelo professor.
- Verificação da situação do aluno, considerando nota mínima de 6,0 e frequência mínima de 75%, com um total de 120 aulas.
- Histórico de alterações utilizando uma cadeia de hashes SHA-256.
- Identificação do professor responsável nos novos registros de alterações.

## Tecnologias utilizadas

- Java 21
- Eclipse
- MySQL e MySQL Workbench
- MySQL Connector/J
- Git e GitHub
- GitHub Actions

## Segurança

O sistema utiliza PBKDF2 para proteger as senhas dos usuários e uma cadeia de hashes SHA-256 para registrar e verificar alterações de notas e faltas.

A cadeia de hashes é uma implementação local inspirada em Blockchain, sem utilização de uma rede distribuída.

Também utilizamos `PreparedStatement` nas operações com o banco de dados, controle de acesso entre professores e alunos e variáveis de ambiente para configurar as credenciais do MySQL.

## Instalação e execução

Para executar o projeto, é necessário:

1. Instalar o Java 21 e o Eclipse.
2. Instalar e configurar o MySQL Server.
3. Criar o banco de dados `sistema_notas_faltas` e suas tabelas.
4. Importar o projeto Java no Eclipse.
5. Adicionar o MySQL Connector/J às bibliotecas do projeto.
6. Configurar as variáveis de ambiente `MYSQL_APP_USER` e `MYSQL_APP_PASSWORD` com as credenciais de um usuário autorizado do MySQL.
7. Executar a classe `SistemaNF.java`.

O sistema utiliza uma conexão local com o MySQL, pela porta 3306. Para utilizá-lo em outro computador, será necessário preparar o banco de dados e configurar a conexão.

## Testes realizados

Durante o desenvolvimento, realizamos testes de autenticação, permissões dos usuários, consultas, cadastro e alteração de notas, alteração de faltas e regras de aprovação.

Também verificamos a integridade da cadeia de hashes e realizamos testes de desempenho das consultas ao MySQL.

Criamos ainda uma cópia de segurança do banco de dados utilizando o MySQL Workbench.

## Desenvolvimento

O projeto utiliza as branches `main` e `desenvolvimento` para organizar as alterações.

Utilizamos Pull Requests para integrar as melhorias, e o GitHub Actions realiza automaticamente a compilação das classes Java.

Atualmente, o sistema funciona localmente pelo console do Eclipse, utilizando o MySQL para armazenar os dados. Ainda não possui interface web, implantação automática ou testes automatizados no GitHub Actions.

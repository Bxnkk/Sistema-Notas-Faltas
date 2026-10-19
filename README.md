
# Sistema de Notas e Faltas

Projeto desenvolvido em Java e MySQL para o gerenciamento
de notas e faltas de alunos.

## Funcionalidades

- Login de professores e alunos
- Controle de acesso por tipo de usuário
- Consulta de notas e faltas
- Alteração de notas e faltas pelo professor
- Verificação da situação acadêmica do aluno
- Histórico de alterações utilizando hashes SHA-256

## Tecnologias utilizadas

- Java 21
- Eclipse
- MySQL
- MySQL Connector/J
- Git e GitHub
- GitHub Actions

## Segurança

O sistema utiliza PBKDF2 para proteger as senhas dos
usuários e uma cadeia de hashes SHA-256 para registrar
e verificar alterações de notas e faltas.

A cadeia de hashes é uma implementação local inspirada
em Blockchain, sem utilização de uma rede distribuída.

## Desenvolvimento

O projeto utiliza as branches main e desenvolvimento.
O GitHub Actions realiza a compilação automática das
classes Java.

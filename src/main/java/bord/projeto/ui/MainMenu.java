package bord.projeto.ui;

import bord.projeto.persistence.entity.BoardColumnEntity;
import bord.projeto.persistence.entity.BoardColumnKindEnum;
import bord.projeto.persistence.entity.BoardEntity;
import bord.projeto.service.BoardQueryService;
import bord.projeto.service.BoardService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static bord.projeto.persistence.ConnectionConfig.getConnection;
import static bord.projeto.persistence.entity.BoardColumnKindEnum.*;

public class MainMenu {

    private final Scanner scanner = new Scanner(System.in);

    public void execute()throws SQLException {
        System.out.println("Bem vindo ao menu do board, faça uma escolha!");
        var option = -1;
        while (true) {
            System.out.println("1 - Crie uma board");
            System.out.println("2 - Selecione um board criado");
            System.out.println("3 - Exclua um board");
            System.out.println("4 - Finalizar operação");
            option = scanner.nextInt();
            switch (option) {
                case 1 -> createBoard();
                case 2 -> selectBoard();
                case 3 -> deleteBoard();
                case 4 -> System.exit(0);
                default -> System.out.println("Opção inválida, informe outra opção do menu");
            }

        }
    }

    private void createBoard() throws SQLException {
        // Criando uma nova entidade para o board
        var entity = new BoardEntity();

        // Solicitando ao usuário o nome do board
        System.out.println("Informe o nome do seu board:");
        entity.setName(scanner.next());

        // Perguntando ao usuário se deseja adicionar mais colunas além das três padrões
        System.out.println("Seu board terá colunas além das 3 padrões? Se sim, informe quantas, senão digite '0':");
        int additionalColumns = scanner.nextInt();
        scanner.nextLine(); // Consumindo a quebra de linha para evitar problemas na próxima entrada

        // Criando uma lista para armazenar as colunas do board
        List<BoardColumnEntity> columns = new ArrayList<>();

        // Solicitando o nome da coluna inicial do board e adicionando à lista
        System.out.println("Informe o nome da coluna inicial do board:");
        String initialColumnName = scanner.nextLine();
        columns.add(createColumn(initialColumnName, INITIAL, 0));

        // Criando as colunas adicionais definidas pelo usuário
        for (int i = 0; i < additionalColumns; i++) {
            System.out.printf("Informe o nome da %dª coluna de tarefa pendente do board:%n", i + 1);
            String pendingColumnName = scanner.nextLine();
            columns.add(createColumn(pendingColumnName, PENDING, i + 1));
        }

        // Solicitando o nome da coluna final e adicionando à lista
        System.out.println("Informe o nome da coluna final:");
        String finalColumnName = scanner.nextLine();
        columns.add(createColumn(finalColumnName, FINAL, additionalColumns + 1));

        // Solicitando o nome da coluna de cancelamento do board e adicionando à lista
        System.out.println("Informe o nome da coluna de cancelamento do board:");
        String cancelColumnName = scanner.nextLine();
        columns.add(createColumn(cancelColumnName, CANCEL, additionalColumns + 2));

        // Associando a lista de colunas ao board
        entity.setBoardColumns(columns);

        // Obtendo uma conexão com o banco de dados e inserindo o board no banco
        try (var connection = getConnection()) {
            var service = new BoardService(connection);
            service.insert(entity);
        }
    }



    private void selectBoard() throws SQLException {
        // Solicita ao usuário o ID do board a ser selecionado
        System.out.println("Informe o id do board que deseja selecionar:");
        var id = scanner.nextLong();

        // Abre uma conexão com o banco de dados dentro de um bloco try-with-resources
        try (var connection = getConnection()) {
            var queryService = new BoardQueryService(connection);

            // Busca o board pelo ID
            var optional = queryService.findById(id);

            // Se encontrado, abre o menu do board; caso contrário, exibe mensagem de erro
            optional.ifPresentOrElse(
                    b -> new BoardMenu(b).execute(),
                    () -> System.out.printf("Não foi encontrado um board com id %s\n", id)
            );
        }
    }

    private void deleteBoard()throws SQLException {
        // Solicita ao usuário o ID do board a ser excluído
        System.out.println("Informe o ID do board que será excluído:");

        // Verifica se a entrada é um número válido antes de prosseguir
        if (!scanner.hasNextLong()) {
            System.out.println("ID inválido. Digite um número válido.");
            scanner.next(); // Limpa a entrada inválida do scanner
            return; //Sai do método sem tentar excluir um board inexistente
        }
        var id = scanner.nextLong(); // Lê o ID informado pelo usuário

        // Usa try-with-resources para garantir que a conexão será fechada corretamente
        try (var connection = getConnection()) {
            var service = new BoardService(connection);

            // Tenta excluir o board pelo ID informado
            if (service.delete(id)) {
                System.out.printf("O board %d foi excluído com sucesso.\n", id);
            } else {
                System.out.printf("Não foi encontrado um board com ID %d.\n", id);
            }
        } catch (SQLException e) {
            // Captura possíveis erros de banco de dados e exibe uma mensagem de erro
            System.err.println("Erro ao tentar excluir o board: " + e.getMessage());
        }
    }


    private BoardColumnEntity createColumn(final String name, final BoardColumnKindEnum kind, final int order) {
        // Criando uma nova instância da entidade BoardColumnEntity
        var boardColumn = new BoardColumnEntity();

        // Definindo o nome da coluna
        boardColumn.setName(name);

        // Definindo o tipo da coluna
        boardColumn.setKind(kind);

        // Definindo a ordem da coluna no board
        boardColumn.setOrder(order);

        // Retornando a instância configurada
        return boardColumn;
    }



}
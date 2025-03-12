package bord.projeto.ui;

import bord.projeto.dto.BoardColumnInfoDTO;
import bord.projeto.exception.CardBlockedException;
import bord.projeto.exception.CardFinishedException;
import bord.projeto.persistence.entity.BoardColumnEntity;
import bord.projeto.persistence.entity.BoardEntity;
import bord.projeto.persistence.entity.CardEntity;
import bord.projeto.service.BoardColumnQueryService;
import bord.projeto.service.BoardQueryService;
import bord.projeto.service.CardQueryService;
import bord.projeto.service.CardService;
import lombok.AllArgsConstructor;

import java.sql.SQLException;
import java.util.Scanner;

import static bord.projeto.persistence.ConnectionConfig.getConnection;

@AllArgsConstructor
public class BoardMenu {

    private final Scanner scanner = new Scanner(System.in).useDelimiter("\n");

    private final BoardEntity entity;

    public void execute() {
        try {
            System.out.printf("Bem vindo ao board %s, selecione a operação desejada\n", entity.getId());
            var option = -1;
            while (option != 9) {
                System.out.println("1 - Criar");
                System.out.println("2 - Mover");
                System.out.println("3 - Bloquear");
                System.out.println("4 - Desbloquear");
                System.out.println("5 - Cancelar");
                System.out.println("6 - Ver board");
                System.out.println("7 - Ver coluna com cards");
                System.out.println("8 - Ver card");
                System.out.println("9 - Voltar para o menu anterior");
                System.out.println("10 - Sair");
                option = scanner.nextInt();
                switch (option) {
                    case 1 -> createCard();
                    case 2 -> moveCardToNextColumn();
                    case 3 -> blockCard();
                    case 4 -> unblockCard();
                    case 5 -> cancelCard();
                    case 6 -> showBoard();
                    case 7 -> showColumn();
                    case 8 -> showCard();
                    case 9 -> System.out.println("Voltando para o menu anterior");
                    case 10 -> System.exit(0);
                    default -> System.out.println("Opção inválida, informe uma opção do menu");
                }
            }
        }catch (SQLException ex){
            ex.printStackTrace();
            System.exit(0);
        }
    }

    private void createCard() throws SQLException{
        var card = new CardEntity();
        System.out.println("Informe o título do card");
        card.setTitle(scanner.next());
        System.out.println("Informe a descrição do card");
        card.setDescription(scanner.next());
        card.setBoardColumn(entity.getInitialColumn());
        try(var connection = getConnection()){
            new CardService(connection).create(card);
        }
    }

    private void moveCardToNextColumn() throws SQLException {
        System.out.println("Informe o ID do board:");
        var boardId = scanner.nextLong(); // Solicita o ID do board
        System.out.println("Informe o ID do card que deseja mover para a próxima coluna:");
        var cardId = scanner.nextLong();  // Solicita o ID do card

        // Mapeia as colunas do board para a DTO necessária
        var boardColumnsInfo = entity.getBoardColumns().stream()
                .map(bc -> new BoardColumnInfoDTO(bc.getId(), bc.getOrder(), bc.getKind()))
                .toList();

        // Tenta mover o card para a próxima coluna
        try (var connection = getConnection()) {
            var cardService = new CardService(connection);
            cardService.moveToNextColumn(boardId, cardId, boardColumnsInfo);
            System.out.println("Card movido com sucesso!");
        } catch (CardBlockedException | CardFinishedException | IllegalStateException ex) {
            System.out.println("Erro ao mover o card: " + ex.getMessage());
        }
    }


    private void blockCard() {
    }

    private void unblockCard() {
    }

    private void cancelCard() {
    }

    private void showBoard() throws SQLException {
        try(var connection = getConnection()){
            var optional = new BoardQueryService(connection).showBoardDetails(entity.getId());
            optional.ifPresent(b -> {
                System.out.printf("Board [%s,%s]\n", b.id(), b.name());
                b.columns().forEach(c ->
                        System.out.printf("Coluna [%s] tipo: [%s] tem %s cards\n", c.name(), c.kind(), c.cardsAmount())
                );
            });
        }
    }

    private void showColumn() throws SQLException {
        var columnsIds = entity.getBoardColumns().stream().map(BoardColumnEntity::getId).toList();
        var selectedColumnId = -1L;
        while (!columnsIds.contains(selectedColumnId)){
            System.out.printf("Escolha uma coluna do board %s pelo id\n", entity.getName());
            entity.getBoardColumns().forEach(c -> System.out.printf("%s - %s [%s]\n", c.getId(), c.getName(), c.getKind()));
            selectedColumnId = scanner.nextLong();
        }
        try(var connection = getConnection()){
            var column = new BoardColumnQueryService(connection).findById(selectedColumnId);
            column.ifPresent(co -> {
                System.out.printf("Coluna %s tipo %s\n", co.getName(), co.getKind());
                co.getCards().forEach(ca -> System.out.printf("Card %s - %s\nDescrição: %s",
                        ca.getId(), ca.getTitle(), ca.getDescription()));
            });
        }
    }

    private void showCard() throws SQLException {
        // Solicita o id do board e do card que o usuário deseja visualizar
        System.out.println("Informe o id do board:");
        var selectedBoardId = scanner.nextLong();  // Recebe o id do board
        System.out.println("Informe o id do card que deseja visualizar:");
        var selectedCardId = scanner.nextLong();  // Recebe o id do card

        // Tenta estabelecer uma conexão com o banco e consultar os detalhes do card
        try (var connection = getConnection()) {
            // Chama o serviço CardQueryService passando tanto o boardId quanto o cardId
            new CardQueryService(connection).findById(selectedBoardId, selectedCardId)
                    .ifPresentOrElse(
                            c -> {  // Caso o card seja encontrado, exibe as informações do card
                                System.out.printf("Card %s - %s.\n", c.id(), c.title());
                                System.out.printf("Descrição: %s\n", c.description());
                                // Verifica se o card está bloqueado ou não e exibe a mensagem correspondente
                                System.out.println(c.block() ?
                                        "Está bloqueado. Motivo: " + c.blockReason() :
                                        "Não está bloqueado");
                                System.out.printf("Já foi bloqueado %s vezes\n", c.blocksAmount());
                                System.out.printf("Está no momento na coluna %s - %s\n", c.columnId(), c.columnName());
                            },
                            () -> System.out.printf("Não existe um card com o id %s no board %s\n", selectedCardId, selectedBoardId));  // Caso o card não seja encontrado, exibe mensagem de erro
        }
    }

}

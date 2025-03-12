package bord.projeto.service;

import bord.projeto.dto.BoardColumnInfoDTO;
import bord.projeto.dto.CardDetailsDTO;
import bord.projeto.exception.CardBlockedException;
import bord.projeto.exception.CardFinishedException;
import bord.projeto.exception.EntityNotFoundException;
import bord.projeto.persistence.dao.BlockDAO;
import bord.projeto.persistence.dao.CardDAO;
import bord.projeto.persistence.entity.CardEntity;
import lombok.AllArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static bord.projeto.persistence.entity.BoardColumnKindEnum.CANCEL;
import static bord.projeto.persistence.entity.BoardColumnKindEnum.FINAL;

@AllArgsConstructor
public class CardService {

    private final Connection connection; // Conexão com o banco de dados fornecida via injeção de dependência

    /**
     * Cria um novo card e o insere no banco de dados.
     *
     * @param entity O objeto CardEntity contendo as informações do card a ser criado.
     * @return O CardEntity com o ID atribuído após a inserção.
     * @throws SQLException Caso ocorra um erro na inserção no banco de dados.
     */
    public CardEntity create(final CardEntity entity) throws SQLException {
        try {
            var dao = new CardDAO(connection); // Instancia o DAO para interagir com o banco de dados
            dao.insert(entity); // Insere o card no banco de dados

            System.out.println("O card foi criado com sucesso!"); // Mensagem de sucesso

            connection.commit(); // Confirma a transação
            return entity; // Retorna o card inserido com o ID atribuído
        } catch (SQLException ex) {
            connection.rollback(); // Em caso de erro, desfaz a transação
            throw ex; // Relança a exceção
        }
    }

    // Métodos relacionados à movimentação de cards entre colunas

    /**
     * Move o card para a próxima coluna no fluxo de trabalho.
     *
     * @param boardId O ID do board onde o card está localizado.
     * @param cardId O ID do card que será movido.
     * @param boardColumnsInfo A lista de informações sobre as colunas do board.
     * @throws SQLException Caso ocorra um erro na movimentação do card.
     */
    public void moveToNextColumn(final Long boardId, final Long cardId, final List<BoardColumnInfoDTO> boardColumnsInfo) throws SQLException {
        try {
            var dao = new CardDAO(connection); // Instancia o DAO para consultar o card

            // Busca o card pelo ID, garantindo que ele pertence ao board correto
            var dto = dao.findById(boardId, cardId)
                    .orElseThrow(() -> new EntityNotFoundException("O card de id %s não foi encontrado no board %s".formatted(cardId, boardId)));

            // Verifica se o card está bloqueado
            validateCardNotBlocked(dto, cardId);

            // Encontra a coluna atual do card
            var currentColumn = findCurrentColumn(boardColumnsInfo, dto.columnId());

            // Verifica se o card está na última coluna
            validateNotFinalColumn(currentColumn);

            // Encontra a próxima coluna do fluxo
            var nextColumn = findNextColumn(boardColumnsInfo, currentColumn);

            // Move o card para a próxima coluna
            dao.moveToColumn(nextColumn.id(), cardId);

            System.out.println("O card de ID " + cardId + " foi movido com sucesso!"); // Mensagem de sucesso

            connection.commit(); // Confirma a transação
        } catch (SQLException ex) {
            if (connection != null) {
                connection.rollback(); // Em caso de erro, desfaz a transação
            }
            throw ex; // Relança a exceção
        }
    }

    // Verifica se o card está bloqueado
    private void validateCardNotBlocked(CardDetailsDTO dto, Long cardId) {
        if (dto.block()) {
            throw new CardBlockedException("O card %s está bloqueado. É necessário desbloqueá-lo para mover.".formatted(cardId));
        }
    }

    // Encontra a coluna atual do card
    private BoardColumnInfoDTO findCurrentColumn(List<BoardColumnInfoDTO> boardColumnsInfo, Long columnId) {
        return boardColumnsInfo.stream()
                .filter(bc -> bc.id().equals(columnId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("O card informado pertence a outro board"));
    }

    // Verifica se o card já foi finalizado
    private void validateNotFinalColumn(BoardColumnInfoDTO currentColumn) {
        if (currentColumn.kind().equals(FINAL)) {
            throw new CardFinishedException("O card já foi finalizado.");
        }
    }

    // Encontra a próxima coluna do fluxo
    private BoardColumnInfoDTO findNextColumn(List<BoardColumnInfoDTO> boardColumnsInfo, BoardColumnInfoDTO currentColumn) {
        return boardColumnsInfo.stream()
                .filter(bc -> bc.order() == currentColumn.order() + 1)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("O card está cancelado."));
    }

    // Métodos para cancelamento, bloqueio e desbloqueio de cards

    /**
     * Cancela o card e o move para a coluna de cancelamento.
     *
     * @param boardId O ID do board onde o card está localizado.
     * @param cardId O ID do card que será cancelado.
     * @param cancelColumnId O ID da coluna de cancelamento.
     * @param boardColumnsInfo A lista de informações sobre as colunas do board.
     * @throws SQLException Caso ocorra um erro na operação de cancelamento.
     */
    public void cancel(final Long boardId, final Long cardId, final Long cancelColumnId,
                       final List<BoardColumnInfoDTO> boardColumnsInfo) throws SQLException {
        try {
            var dao = new CardDAO(connection);

            // Busca o card pelo ID no banco de dados
            CardDetailsDTO cardDTO = dao.findById(boardId, cardId)
                    .orElseThrow(() -> new EntityNotFoundException("O card de id %s não foi encontrado".formatted(cardId)));

            // Verifica se o card está bloqueado
            if (cardDTO.block()) {
                throw new CardBlockedException("O card %s está bloqueado, é necessário desbloqueá-lo para mover".formatted(cardId));
            }

            // Encontra a coluna atual do card
            BoardColumnInfoDTO currentColumn = boardColumnsInfo.stream()
                    .filter(bc -> bc.id().equals(cardDTO.columnId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("O card informado pertence a outro board"));

            // Verifica se o card foi finalizado
            if (currentColumn.kind().equals(FINAL)) {
                throw new CardFinishedException("O card já foi finalizado");
            }

            // Verifica se o card já está cancelado
            if (boardColumnsInfo.stream().noneMatch(bc -> bc.order() == currentColumn.order() + 1)) {
                throw new IllegalStateException("O card está cancelado");
            }

            // Move o card para a coluna de cancelamento
            dao.moveToColumn(cancelColumnId, cardId);

            connection.commit(); // Confirma a transação
        } catch (SQLException ex) {
            connection.rollback(); // Desfaz a transação em caso de erro
            throw ex;
        }
    }

    /**
     * Bloqueia um card, impedindo movimentações até ser desbloqueado.
     *
     * @param boardId O ID do board onde o card está localizado.
     * @param cardId O ID do card que será bloqueado.
     * @param reason O motivo do bloqueio.
     * @param boardColumnsInfo A lista de informações sobre as colunas do board.
     * @throws SQLException Caso ocorra um erro no bloqueio.
     */
    public void block(final Long boardId, final Long cardId, final String reason, final List<BoardColumnInfoDTO> boardColumnsInfo) throws SQLException {
        try {
            var dao = new CardDAO(connection);
            var optional = dao.findById(boardId, cardId);
            var dto = optional.orElseThrow(
                    () -> new EntityNotFoundException("O card de id %s não foi encontrado".formatted(boardId, cardId))
            );

            if (dto.block()) {
                throw new CardBlockedException("O card %s já está bloqueado".formatted(boardId, cardId));
            }

            var currentColumn = boardColumnsInfo.stream()
                    .filter(bc -> bc.id().equals(dto.columnId()))
                    .findFirst()
                    .orElseThrow();

            if (currentColumn.kind().equals(FINAL) || currentColumn.kind().equals(CANCEL)) {
                throw new IllegalStateException("O card está em uma coluna do tipo %s e não pode ser bloqueado"
                        .formatted(currentColumn.kind()));
            }

            var blockDAO = new BlockDAO(connection);
            blockDAO.block(reason, cardId); // Bloqueia o card

            System.out.println("O card de ID " + cardId + " foi bloqueado com sucesso!");

            connection.commit(); // Confirma a transação
        } catch (SQLException ex) {
            connection.rollback(); // Desfaz a transação em caso de erro
            throw ex;
        }
    }

    /**
     * Desbloqueia um card, permitindo futuras movimentações.
     *
     * @param boardId O ID do board onde o card está localizado.
     * @param cardId O ID do card que será desbloqueado.
     * @param reason O motivo do desbloqueio.
     * @throws SQLException Caso ocorra um erro no desbloqueio.
     */
    public void unblock(final Long boardId, final Long cardId, final String reason) throws SQLException {
        try {
            var dao = new CardDAO(connection);
            var optional = dao.findById(boardId, cardId);
            var dto = optional.orElseThrow(
                    () -> new EntityNotFoundException("O card de id %s não foi encontrado".formatted(cardId))
            );

            if (!dto.block()) {
                throw new CardBlockedException("O card %s não está bloqueado".formatted(cardId));
            }

            var blockDAO = new BlockDAO(connection);
            blockDAO.unblock(reason, cardId); // Desbloqueia o card

            System.out.println("O card de ID " + cardId + " foi desbloqueado com sucesso!");

            connection.commit(); // Confirma a transação
        } catch (SQLException ex) {
            connection.rollback(); // Desfaz a transação em caso de erro
            throw ex; // Relança a exceção
        }
    }
}

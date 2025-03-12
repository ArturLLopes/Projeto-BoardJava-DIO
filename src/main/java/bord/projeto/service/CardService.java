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

    private final Connection connection;

    public CardEntity create(final CardEntity entity) throws SQLException {
        try {
            var dao = new CardDAO(connection);
            dao.insert(entity);

            System.out.println("O card de foi criado com sucesso!");

            connection.commit();
            return entity;
        } catch (SQLException ex){
            connection.rollback();
            throw ex;
        }
    }

    public void moveToNextColumn(final Long boardId, final Long cardId, final List<BoardColumnInfoDTO> boardColumnsInfo) throws SQLException {
        try {
            var dao = new CardDAO(connection);

            // Busca o card garantindo que pertence ao board correto
            var dto = dao.findById(boardId, cardId)
                    .orElseThrow(() -> new EntityNotFoundException("O card de id %s não foi encontrado no board %s".formatted(cardId, boardId)));

            // Verifica se o card está bloqueado
            validateCardNotBlocked(dto, cardId);

            // Encontra a coluna atual do card
            var currentColumn = findCurrentColumn(boardColumnsInfo, dto.columnId());

            // Verifica se o card já está na última coluna
            validateNotFinalColumn(currentColumn);

            // Determina a próxima coluna do fluxo
            var nextColumn = findNextColumn(boardColumnsInfo, currentColumn);

            // Move o card para a próxima coluna
            dao.moveToColumn(nextColumn.id(), cardId);

            System.out.println("O card de ID " + cardId + " foi movido com sucesso!");

            connection.commit();

        } catch (SQLException ex) {
            if (connection != null) {
                connection.rollback();
            }
            throw ex;
        }
    }

    // Método para verificar se o card está bloqueado
    private void validateCardNotBlocked(CardDetailsDTO dto, Long cardId) {
        if (dto.block()) {
            throw new CardBlockedException("O card %s está bloqueado. É necessário desbloqueá-lo para mover.".formatted(cardId));
        }
    }

    // Método para encontrar a coluna atual do card
    private BoardColumnInfoDTO findCurrentColumn(List<BoardColumnInfoDTO> boardColumnsInfo, Long columnId) {
        return boardColumnsInfo.stream()
                .filter(bc -> bc.id().equals(columnId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("O card informado pertence a outro board"));
    }

    // Método para validar se o card já foi finalizado
    private void validateNotFinalColumn(BoardColumnInfoDTO currentColumn) {
        if (currentColumn.kind().equals(FINAL)) {
            throw new CardFinishedException("O card já foi finalizado.");
        }
    }

    // Método para encontrar a próxima coluna do fluxo
    private BoardColumnInfoDTO findNextColumn(List<BoardColumnInfoDTO> boardColumnsInfo, BoardColumnInfoDTO currentColumn) {
        return boardColumnsInfo.stream()
                .filter(bc -> bc.order() == currentColumn.order() + 1)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("O card está cancelado."));
    }



    public void cancel(final Long boardId, final Long cardId, final Long cancelColumnId,
                       final List<BoardColumnInfoDTO> boardColumnsInfo) throws SQLException {
        try {
            // Cria uma instância do DAO para interagir com o banco de dados
            CardDAO dao = new CardDAO(connection);

            // Busca o card pelo ID no banco de dados
            CardDetailsDTO cardDTO = dao.findById(boardId, cardId)
                    .orElseThrow(() -> new EntityNotFoundException("O card de id %s não foi encontrado".formatted(cardId)));

            // Verifica se o card está bloqueado
            if (cardDTO.block()) {
                throw new CardBlockedException("O card %s está bloqueado, é necesário desbloquea-lo para mover".formatted(cardId));
            }

            // Encontra a coluna atual do card
            BoardColumnInfoDTO currentColumn = boardColumnsInfo.stream()
                    .filter(bc -> bc.id().equals(cardDTO.columnId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("O card informado pertence a outro board"));

            // Verifica se o card já foi finalizado
            if (currentColumn.kind().equals(FINAL)) {
                throw new CardFinishedException("O card já foi finalizado");
            }

            // Verifica se o card já está cancelado (se não existe coluna seguinte)
            if (boardColumnsInfo.stream().noneMatch(bc -> bc.order() == currentColumn.order() + 1)) {
                throw new IllegalStateException("O card está cancelado");
            }

            // Move o card para a coluna de cancelamento
            dao.moveToColumn(cancelColumnId, cardId);

            System.out.println("O card de ID " + cardId + " foi cancelado com sucesso!");

            // Confirma a transação
            connection.commit();
        } catch (SQLException ex) {
            // Em caso de erro, desfaz a transação
            connection.rollback();
            throw ex;
        }
    }



    public void block(final Long boardId, final Long cardId, final String reason, final List<BoardColumnInfoDTO> boardColumnsInfo) throws SQLException {
        try{
            var dao = new CardDAO(connection);
            var optional = dao.findById(boardId, cardId);
            var dto = optional.orElseThrow(
                    () -> new EntityNotFoundException("O card de id %s não foi encontrado".formatted(boardId, cardId))
            );
            if (dto.block()){
                var message = "O card %s já está bloqueado".formatted(boardId, cardId);
                throw new CardBlockedException(message);
            }
            var currentColumn = boardColumnsInfo.stream()
                    .filter(bc -> bc.id().equals(dto.columnId()))
                    .findFirst()
                    .orElseThrow();
            if (currentColumn.kind().equals(FINAL) || currentColumn.kind().equals(CANCEL)){
                var message = "O card está em uma coluna do tipo %s e não pode ser bloqueado"
                        .formatted(currentColumn.kind());
                throw new IllegalStateException(message);
            }
            var blockDAO = new BlockDAO(connection);
            blockDAO.block(reason, cardId);

            System.out.println("O card de ID " + cardId + " foi bloqueado com sucesso!");

            connection.commit();
        }catch (SQLException ex) {
            connection.rollback();
            throw ex;
        }
    }

     /*

    public void unblock(final Long id, final String reason) throws SQLException {
        try{
            var dao = new CardDAO(connection);
            var optional = dao.findById(id);
            var dto = optional.orElseThrow(
                    () -> new EntityNotFoundException("O card de id %s não foi encontrado".formatted(id))
            );
            if (!dto.block()){
                var message = "O card %s não está bloqueado".formatted(id);
                throw new CardBlockException(message);
            }
            var blockDAO = new BlockDAO(connection);
            blockDAO.unblock(reason, id);

            System.out.println("O card de ID " + cardId + " foi desbloqueado com sucesso!");

            connection.commit();
        }catch (SQLException ex) {
            connection.rollback();
            throw ex;
        }
    }*/

}
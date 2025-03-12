package bord.projeto.service;

import bord.projeto.dto.BoardColumnInfoDTO;
import bord.projeto.dto.CardDetailsDTO;
import bord.projeto.exception.CardBlockedException;
import bord.projeto.exception.CardFinishedException;
import bord.projeto.exception.EntityNotFoundException;
import bord.projeto.persistence.dao.CardDAO;
import bord.projeto.persistence.entity.CardEntity;
import lombok.AllArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static bord.projeto.persistence.entity.BoardColumnKindEnum.FINAL;

@AllArgsConstructor
public class CardService {

    private final Connection connection;

    public CardEntity create(final CardEntity entity) throws SQLException {
        try {
            var dao = new CardDAO(connection);
            dao.insert(entity);
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

     /*public void cancel(final Long cardId, final Long cancelColumnId ,
                       final List<BoardColumnInfoDTO> boardColumnsInfo) throws SQLException{
        try{
            var dao = new CardDAO(connection);
            var optional = dao.findById(cardId);
            var dto = optional.orElseThrow(
                    () -> new EntityNotFoundException("O card de id %s não foi encontrado".formatted(cardId))
            );
            if (dto.block()){
                var message = "O card %s está bloqueado, é necesário desbloquea-lo para mover".formatted(cardId);
                throw new CardBlockException(message);
            }
            var currentColumn = boardColumnsInfo.stream()
                    .filter(bc -> bc.id().equals(dto.columnId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("O card informado pertence a outro board"));
            if (currentColumn.kind().equals(FINAL)){
                throw new CardFinishedException("O card já foi finalizado");
            }
            boardColumnsInfo.stream()
                    .filter(bc -> bc.order() == currentColumn.order() + 1)
                    .findFirst().orElseThrow(() -> new IllegalStateException("O card está cancelado"));
            dao.moveToColumn(cancelColumnId, cardId);
            connection.commit();
        }catch (SQLException ex){
            connection.rollback();
            throw ex;
        }
    }

    public void block(final Long id, final String reason, final List<BoardColumnInfoDTO> boardColumnsInfo) throws SQLException {
        try{
            var dao = new CardDAO(connection);
            var optional = dao.findById(id);
            var dto = optional.orElseThrow(
                    () -> new EntityNotFoundException("O card de id %s não foi encontrado".formatted(id))
            );
            if (dto.block()){
                var message = "O card %s já está bloqueado".formatted(id);
                throw new CardBlockException(message);
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
            blockDAO.block(reason, id);
            connection.commit();
        }catch (SQLException ex) {
            connection.rollback();
            throw ex;
        }
    }

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
            connection.commit();
        }catch (SQLException ex) {
            connection.rollback();
            throw ex;
        }
    }*/

}
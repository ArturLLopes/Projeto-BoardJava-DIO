package bord.projeto.persistence.dao;

import bord.projeto.dto.CardDetailsDTO;
import bord.projeto.persistence.entity.CardEntity;
import com.mysql.cj.jdbc.StatementImpl;
import lombok.AllArgsConstructor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import static bord.projeto.persistence.converter.OffsetDateTimeConverter.toOffsetDateTime;
import static java.util.Objects.nonNull;


@AllArgsConstructor
public class CardDAO {

    private Connection connection;

    public CardEntity insert(final CardEntity entity) throws SQLException {
        var sql = "INSERT INTO CARDS (title, description, board_column_id) values (?, ?, ?);";
        try(var statement = connection.prepareStatement(sql)){
            var i = 1;
            statement.setString(i ++, entity.getTitle());
            statement.setString(i ++, entity.getDescription());
            statement.setLong(i, entity.getBoardColumn().getId());
            statement.executeUpdate();
            if (statement instanceof StatementImpl impl){
                entity.setId(impl.getLastInsertID());
            }
        }
        return entity;
    }

    public void moveToColumn(final Long columnId, final Long cardId) throws SQLException{
        var sql = "UPDATE CARDS SET board_column_id = ? WHERE id = ?;";
        try(var statement = connection.prepareStatement(sql)){
            var i = 1;
            statement.setLong(i ++, columnId);
            statement.setLong(i, cardId);
            statement.executeUpdate();
        }
    }

    public Optional<CardDetailsDTO> findById(final Long boardId, final Long cardId) throws SQLException {
        // SQL query para obter os detalhes do card, filtrando também pelo boardId
        String sql = """
        SELECT c.id,                         -- Seleciona o ID do card
               c.title,                      -- Seleciona o título do card
               c.description,                -- Seleciona a descrição do card
               b.block_at,                  -- Seleciona a data de bloqueio do card
               b.block_reason,              -- Seleciona o motivo de bloqueio do card
               c.board_column_id,           -- Seleciona o ID da coluna do board onde o card está
               bc.name,                      -- Seleciona o nome da coluna do board
               (SELECT COUNT(sub_b.id)      -- Conta a quantidade de bloqueios para o card
                   FROM BLOCKS sub_b
                  WHERE sub_b.card_id = c.id) AS blocks_amount
          FROM CARDS c                         -- Tabela de cards
          LEFT JOIN BLOCKS b                   -- Junção à tabela de BLOCKS para obter detalhes sobre bloqueios
            ON c.id = b.card_id
           AND b.unblock_at IS NULL           -- Apenas considera os bloqueios não desbloqueados
         INNER JOIN BOARDS_COLUMNS bc          -- Junção à tabela de colunas do board para obter o nome da coluna
            ON bc.id = c.board_column_id
         INNER JOIN BOARDS board               -- Junção à tabela de boards para garantir que o card pertence ao board correto
            ON board.id = bc.board_id
          WHERE c.id = ?                       -- Filtra pelo ID do card
            AND board.id = ?;                  -- Filtra pelo ID do board, garantindo que o card pertence ao board correto
    """;

        try (var statement = connection.prepareStatement(sql)) {
            // Define o valor do parâmetro do ID do card na consulta SQL
            statement.setLong(1, cardId);
            // Define o valor do parâmetro do ID do board na consulta SQL
            statement.setLong(2, boardId);

            // Executa a consulta e obtém os resultados
            try (var resultSet = statement.executeQuery()) {
                // Se houver um resultado para a consulta
                if (resultSet.next()) {
                    // Mapeia o resultado para um DTO (Data Transfer Object)
                    CardDetailsDTO dto = mapToCardDetailsDTO(resultSet);
                    // Retorna o DTO como um Optional, indicando que um card foi encontrado
                    return Optional.of(dto);
                }
            }
        }
        // Retorna um Optional vazio se nenhum card for encontrado
        return Optional.empty();
    }


    private CardDetailsDTO mapToCardDetailsDTO(ResultSet resultSet) throws SQLException {
        return new CardDetailsDTO(
                resultSet.getLong("c.id"),
                resultSet.getString("c.title"),
                resultSet.getString("c.description"),
                nonNull(resultSet.getString("b.block_reason")),
                toOffsetDateTime(resultSet.getTimestamp("b.block_at")),
                resultSet.getString("b.block_reason"),
                resultSet.getInt("blocks_amount"),
                resultSet.getLong("c.board_column_id"),
                resultSet.getString("bc.name")
        );
    }


}
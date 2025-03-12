package bord.projeto.persistence.dao;

import bord.projeto.dto.BoardColumnDTO;
import bord.projeto.persistence.entity.BoardColumnEntity;
import bord.projeto.persistence.entity.BoardColumnKindEnum;
import bord.projeto.persistence.entity.CardEntity;

import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;



@RequiredArgsConstructor
public class BoardColumnDAO {

    private final Connection connection;

    // Método para inserir uma nova coluna no banco de dados
    public BoardColumnEntity insert(final BoardColumnEntity entity) throws SQLException {
        var sql = "INSERT INTO BOARDS_COLUMNS (name, `order`, kind, board_id) VALUES (?, ?, ?, ?);";
        try (var statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // Definindo os parâmetros da consulta
            statement.setString(1, entity.getName());  // Nome da coluna
            statement.setInt(2, entity.getOrder());    // Ordem da coluna
            statement.setString(3, entity.getKind().name());  // Tipo (kind) da coluna
            statement.setLong(4, entity.getBoard().getId());  // ID do board relacionado à coluna

            // Executando a consulta de inserção
            statement.executeUpdate();

            // Obtém o ID da última inserção e define no objeto 'entity'
            try (var resultSet = statement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    entity.setId(resultSet.getLong(1));
                }
            }
            return entity;
        }
    }

    // Método para buscar todas as colunas de um board específico, ordenadas pela ordem
    public List<BoardColumnEntity> findByBoardId(final long boardId) throws SQLException {
        var sql = "SELECT id, name, `order`, kind FROM BOARDS_COLUMNS WHERE board_id = ? ORDER BY `order`";
        try (var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, boardId);
            try (var resultSet = statement.executeQuery()) {
                List<BoardColumnEntity> entities = new ArrayList<>();
                while (resultSet.next()) {
                    var entity = new BoardColumnEntity();
                    entity.setId(resultSet.getLong("id"));
                    entity.setName(resultSet.getString("name"));
                    entity.setOrder(resultSet.getInt("order"));
                    entity.setKind(findByName(resultSet.getString("kind")));  // Converte o tipo da coluna (kind)
                    entities.add(entity);
                }
                return entities;
            }
        }
    }

    // Método para buscar colunas com mais detalhes, incluindo a quantidade de cards em cada coluna
    public List<BoardColumnDTO> findByBoardIdWithDetails(final Long boardId) throws SQLException {
        var sql =
                """
                SELECT bc.id,
                       bc.name,
                       bc.kind,
                       (SELECT COUNT(c.id)
                               FROM CARDS c
                              WHERE c.board_column_id = bc.id) cards_amount
                  FROM BOARDS_COLUMNS bc
                 WHERE board_id = ?
                 ORDER BY `order`;
                """;
        try (var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, boardId);
            try (var resultSet = statement.executeQuery()) {
                List<BoardColumnDTO> dtos = new ArrayList<>();
                while (resultSet.next()) {
                    var dto = new BoardColumnDTO(
                            resultSet.getLong("bc.id"),
                            resultSet.getString("bc.name"),
                            findByName(resultSet.getString("bc.kind")),
                            resultSet.getInt("cards_amount")  // Obtendo o número de cards na coluna
                    );
                    dtos.add(dto);
                }
                return dtos;
            }
        }
    }

    // Método para buscar uma coluna por seu ID, incluindo os cards associados
    public Optional<BoardColumnEntity> findById(final Long boardColumnId) throws SQLException {
        var sql =
                """
                SELECT bc.name,
                       bc.kind,
                       c.id,
                       c.title,
                       c.description
                  FROM BOARDS_COLUMNS bc
                  LEFT JOIN CARDS c
                    ON c.board_column_id = bc.id
                 WHERE bc.id = ?;
                """;
        try (var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, boardColumnId);
            try (var resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    var entity = new BoardColumnEntity();
                    entity.setName(resultSet.getString("bc.name"));
                    entity.setKind(findByName(resultSet.getString("bc.kind")));

                    // Iterando sobre os cards associados à coluna
                    do {
                        var card = new CardEntity();
                        if (resultSet.getString("c.title") != null) {
                            card.setId(resultSet.getLong("c.id"));
                            card.setTitle(resultSet.getString("c.title"));
                            card.setDescription(resultSet.getString("c.description"));
                            entity.getCards().add(card);
                        }
                    } while (resultSet.next());

                    return Optional.of(entity);
                }
            }
        }
        return Optional.empty();
    }

    // Método auxiliar para converter o tipo (kind) da coluna
    private BoardColumnKindEnum findByName(String name) {
        return BoardColumnKindEnum.valueOf(name);
    }
}


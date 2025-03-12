package bord.projeto.service;

import bord.projeto.persistence.dao.BoardColumnDAO;
import bord.projeto.persistence.dao.BoardDAO;
import bord.projeto.persistence.entity.BoardEntity;
import lombok.AllArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.stream.Collectors;

@AllArgsConstructor
public class BoardService {

    private final Connection connection; // Conexão com o banco de dados

    /**
     * Insere um novo board e suas colunas associadas no banco de dados.
     *
     * @param entity O objeto BoardEntity a ser inserido.
     * @return O objeto BoardEntity inserido, com seu ID atualizado.
     * @throws SQLException Caso ocorra um erro durante a operação de inserção.
     */
    public BoardEntity insert(final BoardEntity entity) throws SQLException {
        var boardDAO = new BoardDAO(connection);            // DAO para inserir board
        var boardColumnDAO = new BoardColumnDAO(connection); // DAO para inserir as colunas do board

        try {
            // Inicia uma transação para garantir que tanto o board quanto suas colunas sejam inseridos ou nenhum deles
            connection.setAutoCommit(false); // Desabilita o commit automático

            // Insere o board e, após isso, associa as colunas a ele
            boardDAO.insert(entity);
            var columns = entity.getBoardColumns().stream()
                    .map(column -> {
                        column.setBoard(entity);  // Associa cada coluna ao board
                        return column;
                    }).collect(Collectors.toList());

            // Insere as colunas associadas ao board
            for (var column : columns) {
                boardColumnDAO.insert(column);
            }

            // Comita a transação, garantindo que as inserções foram bem-sucedidas
            connection.commit();
        } catch (SQLException e) {
            connection.rollback(); // Caso ocorra um erro, desfaz as operações
            throw e; // Relança a exceção para o controlador tratar
        } finally {
            connection.setAutoCommit(true); // Restaura o commit automático após a transação
        }

        return entity; // Retorna o board inserido com o ID atualizado
    }

    /**
     * Exclui um board pelo seu ID. Se o board não existir, retorna false.
     *
     * @param id O ID do board a ser excluído.
     * @return true se o board foi excluído com sucesso, false caso o board não exista.
     * @throws SQLException Caso ocorra um erro durante a operação de exclusão.
     */
    public boolean delete(final Long id) throws SQLException {
        var boardDAO = new BoardDAO(connection); // DAO para consultar e excluir o board

        try {
            if (!boardDAO.exists(id)) {
                return false; // Retorna false se o board não existir
            }

            // Exclui o board
            boardDAO.delete(id);
            connection.commit(); // Comita a transação
            return true;
        } catch (SQLException e) {
            connection.rollback(); // Em caso de erro, desfaz as operações
            throw e; // Relança a exceção
        }
    }
}


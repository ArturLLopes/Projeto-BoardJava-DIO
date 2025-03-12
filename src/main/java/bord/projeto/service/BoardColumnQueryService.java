package bord.projeto.service;

import bord.projeto.persistence.dao.BoardColumnDAO;
import bord.projeto.persistence.entity.BoardColumnEntity;
import lombok.AllArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

@AllArgsConstructor
public class BoardColumnQueryService {

    private final Connection connection;  // Conexão com o banco de dados

    /**
     * Encontra uma coluna de board no banco de dados pelo ID fornecido.
     *
     * @param id O ID da coluna do board a ser encontrado.
     * @return Um Optional contendo a entidade de coluna do board, caso encontrada.
     * @throws SQLException Caso ocorra um erro durante a consulta no banco de dados.
     */
    public Optional<BoardColumnEntity> findById(final Long id) throws SQLException {
        // Cria uma instância do DAO para interação com o banco de dados
        var dao = new BoardColumnDAO(connection);

        // Chama o método do DAO para encontrar a coluna de board pelo ID
        return dao.findById(id);
    }
}

package bord.projeto.service;


import bord.projeto.dto.BoardDetailsDTO;
import bord.projeto.persistence.dao.BoardColumnDAO;
import bord.projeto.persistence.dao.BoardDAO;
import bord.projeto.persistence.entity.BoardEntity;
import lombok.AllArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

@AllArgsConstructor
public class BoardQueryService {

    private final Connection connection;  // Conexão com o banco de dados

    /**
     * Encontra um board pelo ID e também carrega suas colunas associadas.
     *
     * @param id O ID do board a ser encontrado.
     * @return Um Optional contendo a entidade de board com suas colunas, caso encontrada.
     * @throws SQLException Caso ocorra um erro durante a consulta ao banco de dados.
     */
    public Optional<BoardEntity> findById(final Long id) throws SQLException {
        var dao = new BoardDAO(connection);              // DAO para acessar os dados de board
        var boardColumnDAO = new BoardColumnDAO(connection); // DAO para acessar as colunas do board

        // Busca o board no banco de dados
        var optional = dao.findById(id);

        // Se o board for encontrado, carrega suas colunas associadas
        if (optional.isPresent()) {
            var entity = optional.get();
            entity.setBoardColumns(boardColumnDAO.findByBoardId(entity.getId())); // Define as colunas do board
            return Optional.of(entity); // Retorna o board com suas colunas
        }

        return Optional.empty(); // Retorna Optional vazio se o board não for encontrado
    }

    /**
     * Exibe os detalhes completos de um board, incluindo suas colunas e a quantidade de cards.
     *
     * @param id O ID do board cujos detalhes serão exibidos.
     * @return Um Optional contendo um DTO com os detalhes completos do board, caso encontrado.
     * @throws SQLException Caso ocorra um erro durante a consulta ao banco de dados.
     */
    public Optional<BoardDetailsDTO> showBoardDetails(final Long id) throws SQLException {
        var dao = new BoardDAO(connection);              // DAO para acessar os dados do board
        var boardColumnDAO = new BoardColumnDAO(connection); // DAO para acessar as colunas do board

        // Busca o board no banco de dados
        var optional = dao.findById(id);

        // Se o board for encontrado, carrega suas colunas e cria o DTO de detalhes
        if (optional.isPresent()) {
            var entity = optional.get();
            var columns = boardColumnDAO.findByBoardIdWithDetails(entity.getId()); // Busca as colunas com detalhes
            var dto = new BoardDetailsDTO(entity.getId(), entity.getName(), columns); // Cria o DTO com os detalhes
            return Optional.of(dto); // Retorna o DTO com os detalhes do board
        }

        return Optional.empty(); // Retorna Optional vazio se o board não for encontrado
    }
}


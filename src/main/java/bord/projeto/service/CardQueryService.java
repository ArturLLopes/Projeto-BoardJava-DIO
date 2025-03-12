package bord.projeto.service;

import bord.projeto.dto.CardDetailsDTO;
import bord.projeto.persistence.dao.CardDAO;
import lombok.AllArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

@AllArgsConstructor
public class CardQueryService {

    private final Connection connection; // A conexão com o banco de dados fornecida via injeção de dependência

    /**
     * Busca os detalhes de um card específico em um board, utilizando tanto o boardId quanto o cardId.
     *
     * @param boardId O ID do board no qual o card está localizado.
     * @param cardId O ID do card que estamos buscando.
     * @return Um Optional contendo os detalhes do card ou vazio se não encontrado.
     * @throws SQLException Caso ocorra um erro durante a consulta ao banco de dados.
     */
    public Optional<CardDetailsDTO> findById(final Long boardId, final Long cardId) throws SQLException {
        var dao = new CardDAO(connection); // Instancia o CardDAO, que irá interagir com o banco de dados

        // Chama o método do DAO passando o boardId e cardId, garantindo que a consulta seja filtrada corretamente
        return dao.findById(boardId, cardId); // Retorna um Optional que pode conter o CardDetailsDTO
    }
}

package bord.projeto.service;

import bord.projeto.dto.CardDetailsDTO;
import bord.projeto.persistence.dao.CardDAO;
import lombok.AllArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

@AllArgsConstructor
public class CardQueryService {

    private final Connection connection;

    // Método que agora recebe tanto o boardId quanto o cardId como parâmetros
    // O boardId é usado para garantir que o card pertence ao board correto
    public Optional<CardDetailsDTO> findById(final Long boardId, final Long cardId) throws SQLException {
        // Instancia o DAO, passando a conexão que foi fornecida no construtor
        var dao = new CardDAO(connection);

        // Chama o método findById no DAO, passando ambos os parâmetros
        // O DAO vai usar esses parâmetros para filtrar corretamente o card no banco de dados
        return dao.findById(boardId, cardId);  // Passa boardId e cardId para o DAO
    }
}

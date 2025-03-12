package bord.projeto.persistence.dao;

import lombok.AllArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.OffsetDateTime;

import static bord.projeto.persistence.converter.OffsetDateTimeConverter.toTimestamp;

@AllArgsConstructor
public class BlockDAO {

    private final Connection connection;

    // Método para bloquear um card
    public void block(final String reason, final Long cardId) throws SQLException {
        // Comando SQL para inserir um novo bloqueio no banco de dados
        var sql = "INSERT INTO BLOCKS (block_at, block_reason, card_id) VALUES (?, ?, ?);";

        try (var statement = connection.prepareStatement(sql)) {
            // Prepara os parâmetros da consulta
            int i = 1;
            statement.setTimestamp(i++, toTimestamp(OffsetDateTime.now())); // Define o timestamp atual para o bloqueio
            statement.setString(i++, reason); // Define o motivo do bloqueio
            statement.setLong(i, cardId); // Define o ID do card a ser bloqueado

            // Executa a consulta e verifica quantas linhas foram afetadas
            int rowsAffected = statement.executeUpdate();

            // Se nenhuma linha for afetada, lança uma exceção informando o erro
            if (rowsAffected == 0) {
                throw new SQLException("Falha ao bloquear o card: Nenhuma linha foi afetada.");
            }
        }
    }

    // Método para desbloquear um card
    public void unblock(final String reason, final Long cardId) throws SQLException {
        // Comando SQL para atualizar o registro do bloqueio e marcar como desbloqueado
        var sql = "UPDATE BLOCKS SET unblock_at = ?, unblock_reason = ? WHERE card_id = ? AND unblock_reason IS NULL;";

        try (var statement = connection.prepareStatement(sql)) {
            // Prepara os parâmetros da consulta
            int i = 1;
            statement.setTimestamp(i++, toTimestamp(OffsetDateTime.now())); // Define o timestamp do desbloqueio
            statement.setString(i++, reason); // Define o motivo do desbloqueio
            statement.setLong(i, cardId); // Define o ID do card a ser desbloqueado

            // Executa a consulta e verifica quantas linhas foram afetadas
            int rowsAffected = statement.executeUpdate();

            // Se nenhuma linha for afetada, lança uma exceção informando o erro
            if (rowsAffected == 0) {
                throw new SQLException("Falha ao desbloquear o card: Nenhuma linha foi afetada.");
            }
        }
    }


}
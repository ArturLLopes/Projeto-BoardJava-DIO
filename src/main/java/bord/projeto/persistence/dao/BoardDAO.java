package bord.projeto.persistence.dao;

import bord.projeto.persistence.entity.BoardEntity;
import com.mysql.cj.jdbc.StatementImpl;
import lombok.AllArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

@AllArgsConstructor
public class BoardDAO {

    private final Connection connection;

    /**
     * Insere um novo board na base de dados.
     *
     * @param entity A entidade do board a ser inserida.
     * @return A entidade do board com o ID atribuído após a inserção.
     * @throws SQLException Caso ocorra um erro ao acessar o banco de dados.
     */
    public BoardEntity insert(final BoardEntity entity) throws SQLException {
        var sql = "INSERT INTO BOARDS (name) VALUES (?)";  // Comando SQL para inserção de um novo board.
        try (var statement = connection.prepareStatement(sql)) {
            // Atribui o nome do board à consulta.
            statement.setString(1, entity.getName());
            // Executa a inserção.
            statement.executeUpdate();

            // Obtém o ID gerado para a entidade e a define.
            if (statement instanceof StatementImpl impl) {
                entity.setId(impl.getLastInsertID());
            }
        }
        return entity;  // Retorna a entidade inserida com o ID gerado.
    }

    /**
     * Deleta um board baseado no seu ID.
     *
     * @param id O ID do board a ser deletado.
     * @throws SQLException Caso ocorra um erro ao acessar o banco de dados.
     */
    public void delete(final Long id) throws SQLException {
        var sql = "DELETE FROM BOARDS WHERE id = ?";  // Comando SQL para excluir o board.
        try (var statement = connection.prepareStatement(sql)) {
            // Define o ID do board a ser excluído na consulta.
            statement.setLong(1, id);
            // Executa a exclusão.
            statement.executeUpdate();
        }
    }

    /**
     * Busca um board pelo seu ID.
     *
     * @param id O ID do board a ser encontrado.
     * @return Um Optional contendo o board encontrado ou vazio se não encontrado.
     * @throws SQLException Caso ocorra um erro ao acessar o banco de dados.
     */
    public Optional<BoardEntity> findById(final Long id) throws SQLException {
        var sql = "SELECT id, name FROM BOARDS WHERE id = ?";  // Comando SQL para buscar um board por ID.
        try (var statement = connection.prepareStatement(sql)) {
            // Define o ID na consulta.
            statement.setLong(1, id);
            // Executa a consulta.
            statement.executeQuery();
            var resultSet = statement.getResultSet();

            // Verifica se um resultado foi retornado.
            if (resultSet.next()) {
                var entity = new BoardEntity();
                entity.setId(resultSet.getLong("id"));  // Define o ID do board encontrado.
                entity.setName(resultSet.getString("name"));  // Define o nome do board encontrado.
                return Optional.of(entity);  // Retorna o board encontrado.
            }
        }
        return Optional.empty();  // Retorna Optional vazio se o board não for encontrado.
    }

    /**
     * Verifica se um board existe na base de dados com base no seu ID.
     *
     * @param id O ID do board a ser verificado.
     * @return True se o board existir, false caso contrário.
     * @throws SQLException Caso ocorra um erro ao acessar o banco de dados.
     */
    public boolean exists(final Long id) throws SQLException {
        var sql = "SELECT 1 FROM BOARDS WHERE id = ?";  // Comando SQL para verificar a existência do board.
        try (var statement = connection.prepareStatement(sql)) {
            // Define o ID na consulta.
            statement.setLong(1, id);
            // Executa a consulta.
            statement.executeQuery();
            // Retorna true se um resultado for encontrado.
            return statement.getResultSet().next();
        }
    }
}


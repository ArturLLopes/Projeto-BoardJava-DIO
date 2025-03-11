package bord.projeto.persistence.config.dao;

import bord.projeto.persistence.config.entity.BoardEntity;
import com.mysql.cj.jdbc.StatementImpl;
import lombok.AllArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

@AllArgsConstructor
public class BoardDAO {

    private final Connection connection;

    private BoardEntity insert (final BoardEntity entity)throws SQLException{
        var sql = "INSERT INTO BOARDS (name) values (?)";
        try(var statement = connection.prepareStatement(sql)){
            statement.setString(1,entity.getName());
            statement.executeUpdate();
            if (statement instanceof StatementImpl impl){
                entity.setId(impl.getLastInsertID());
            }
    }
        return entity;
    }

    private  void delete(final Long id)throws SQLException{
        var sql = "DELETE FROM BOARDS WHERE id = ?";
        try(var statement = connection.prepareStatement(sql)){
            statement.setLong(1,id);
            statement.executeUpdate();
        }

    }

    private Optional<BoardEntity> findById (final  Long id)throws SQLException{
        var sql = "SELECT id, name FROM BOARDS WHERE id = ?";
        try(var statement = connection.prepareStatement(sql)){
            statement.setLong(1,id);
            statement.executeQuery();
            var resultSet = statement.getResultSet();
            if(resultSet.next()){
                var entity = new BoardEntity();
                entity.setId(resultSet.getLong("id"));
                entity.setName(resultSet.getString("name"));
                return Optional.of(entity);
            }

        }
            return Optional.empty();

    }

    private boolean exists(final Long id) throws SQLException{
        var sql = "SELECT 1 FROM  BOARDS WHERE id = ?";
        try(var statement = connection.prepareStatement(sql)){
            statement.setLong(1,id);
            statement.executeQuery();
            return statement.getResultSet().next();

        }

    }

}

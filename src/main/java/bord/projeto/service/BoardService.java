package bord.projeto.service;

import bord.projeto.persistence.config.dao.BoardColumnDAO;
import bord.projeto.persistence.config.dao.BoardDAO;
import bord.projeto.persistence.config.entity.BoardEntity;
import lombok.AllArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;

@AllArgsConstructor
public class BoardService {

    private final Connection connection;



//------inset
    public BoardEntity insert(final BoardEntity entity) throws SQLException{
        var doa = new BoardDAO(connection);
        var boardColumnDAO = new BoardColumnDAO(connection);
        try {
            doa.insert(entity);
            var columns = entity.getBoardColumn().stream().map(c ->{
                c.setBoard(entity);
                return c;
            }).toList();
            for (var column : columns){
                boardColumnDAO.insert(column);
            }
            connection.commit();
        }catch (SQLException e){
            connection.rollback();;
            throw e;
        }
        return entity;
    }

//-------delete
    public boolean  delete(final Long id) throws SQLException{
        var doa = new BoardDAO(connection);
        try {
            if (!doa.exists(id)){
                return false;
            }
            doa.delete(id);
            connection.commit();
            return true;
        }catch (SQLException e){
            connection.rollback();;
            throw e;
        }
    }

}

package bord.projeto.service;


import bord.projeto.persistence.config.dao.BoardColumnDAO;
import bord.projeto.persistence.config.dao.BoardDAO;
import bord.projeto.persistence.config.entity.BoardEntity;
import lombok.AllArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

@AllArgsConstructor
public class BoardQueryService {

    private final Connection connection;

    public Optional<BoardEntity> findById(final Long id) throws SQLException{
        var dao = new BoardDAO(connection);
        var bordColumnDAO = new BoardColumnDAO(connection);
        var optinal = dao.findById(id);
        if(optinal.isPresent()){
            var entity = optinal.get();
            entity.setBoardColumn(bordColumnDAO.findByBoardId(entity.getId()));
            return Optional.of(entity);
        }
        return Optional.empty();
    }
}
